package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class ProblemDetectionService {

    data class IssueInfo(val severity: String, val message: String, val startOffset: Int, val endOffset: Int)

    /**
     * A half-open intersection test can never match a zero-width element, which
     * is how parsers report "expected X" errors, so such an error was dropped
     * whenever it sat exactly on the boundary of the requested range. Those are
     * matched by position instead. For every element that spans at least one
     * character the test is unchanged.
     */
    private fun overlapsRange(elementStart: Int, elementEnd: Int, startOffset: Int, endOffset: Int): Boolean =
        if (elementStart == elementEnd) {
            elementStart in startOffset..endOffset
        } else {
            elementStart < endOffset && elementEnd > startOffset
        }

    private fun createIssueFromPsiError(errorElement: PsiErrorElement): IssueInfo? {
        val errorMessage = errorElement.errorDescription
        return if (errorMessage.isNotBlank()) {
            IssueInfo(
                severity = "ERROR",
                message = errorMessage,
                startOffset = errorElement.textRange.startOffset,
                endOffset = errorElement.textRange.endOffset
            )
        } else null
    }

    fun findProblems(psiFile: PsiFile, startOffset: Int, endOffset: Int): List<IssueInfo> {
        val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return emptyList()
        return findProblemsForFile(psiFile, document, startOffset, endOffset)
            .values
            .flatten()
    }

    fun findProblemsForFile(
        psiFile: PsiFile,
        document: Document,
        startOffset: Int,
        endOffset: Int
    ): Map<Int, List<IssueInfo>> {
        val allIssues = mutableListOf<IssueInfo>()

        try {
            val highlights = getHighlightsForRange(psiFile, startOffset, endOffset)
            for (highlight in highlights) {
                if (highlight.description != null && highlight.description.isNotBlank()) {
                    val severity = when (highlight.severity) {
                        HighlightSeverity.ERROR -> "ERROR"
                        HighlightSeverity.WARNING -> "WARNING"
                        HighlightSeverity.WEAK_WARNING -> "WEAK_WARNING"
                        HighlightSeverity.TEXT_ATTRIBUTES -> "INFO"
                        else -> "INFO"
                    }
                    allIssues.add(IssueInfo(
                        severity = severity,
                        message = highlight.description,
                        startOffset = highlight.startOffset,
                        endOffset = highlight.endOffset
                    ))
                }
            }

            // Only fall back to expensive programmatic inspections if the Daemon
            // hasn't produced any highlights for the requested range. This keeps
            // resource usage close to the v1.1.x baseline when the file has
            // already been analyzed by the IDE.
            if (allIssues.isEmpty()) {
                val inspectionIssues = runInspectionsOnRange(psiFile, startOffset, endOffset)
                allIssues.addAll(inspectionIssues)
            }

            val psiProblems = findPsiProblems(psiFile, startOffset, endOffset)
            allIssues.addAll(psiProblems)

        } catch (_: Exception) {
            allIssues.addAll(findPsiProblems(psiFile, startOffset, endOffset))
        }

        // Highlighter and PSI offsets can outlive the text they were computed
        // against, so an offset may point past the end of the current document.
        // getLineNumber throws on those, which would abort the entire copy.
        return allIssues
            .groupBy { document.getLineNumber(it.startOffset.coerceIn(0, document.textLength)) }
            .mapValues { (_, issues) ->
                issues.distinctBy { "${it.severity}:${it.message}" }
            }
    }

    private fun getHighlightsForRange(psiFile: PsiFile, startOffset: Int, endOffset: Int): List<HighlightInfo> {
        val project = psiFile.project
        val highlights = mutableListOf<HighlightInfo>()

        try {
            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return highlights
            val markupModel = DocumentMarkupModel.forDocument(document, project, true)
            val allHighlighters = markupModel.allHighlighters

            for (highlighter in allHighlighters) {
                if (overlapsRange(highlighter.startOffset, highlighter.endOffset, startOffset, endOffset)) {
                    val tooltip = highlighter.errorStripeTooltip
                    if (tooltip is HighlightInfo) {
                        highlights.add(tooltip)
                    }
                }
            }
        } catch (_: Exception) {
        }

        return highlights
    }

    private fun runInspectionsOnRange(psiFile: PsiFile, startOffset: Int, endOffset: Int): List<IssueInfo> {
        val problems = mutableListOf<IssueInfo>()
        val project = psiFile.project

        try {
            val inspectionProfile = InspectionProjectProfileManager.getInstance(project).currentProfile
            val enabledInspections = inspectionProfile.getInspectionTools(psiFile)

            for (toolWrapper in enabledInspections.take(5)) {
                try {
                    if (toolWrapper.tool is LocalInspectionTool) {
                        val inspectionTool = toolWrapper.tool as LocalInspectionTool
                        val inspectionManager = InspectionManager.getInstance(project)

                        val descriptors = ApplicationManager.getApplication()
                            .runReadAction<Array<ProblemDescriptor>> {
                                inspectionTool.checkFile(psiFile, inspectionManager, false)
                                    ?: emptyArray<ProblemDescriptor>()
                            }

                        for (descriptor in descriptors) {
                            val element = descriptor.psiElement
                            if (element != null) {
                                val elementStart = element.textRange.startOffset
                                val elementEnd = element.textRange.endOffset

                                if (overlapsRange(elementStart, elementEnd, startOffset, endOffset)) {
                                    problems.add(IssueInfo(
                                        severity = "INSPECTION",
                                        message = descriptor.descriptionTemplate,
                                        startOffset = elementStart,
                                        endOffset = elementEnd
                                    ))
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    continue
                }
            }
        } catch (_: Exception) {
        }

        return problems
    }

    private fun findPsiProblems(psiFile: PsiFile, startOffset: Int, endOffset: Int): List<IssueInfo> {
        val problems = mutableListOf<IssueInfo>()

        try {
            // Walk the file directly. Deriving the root through findElementAt
            // skipped this scan entirely whenever it returned null, which is what
            // it does at and past the end of the file.
            val errorElements = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java)
            for (errorElement in errorElements) {
                val errorStart = errorElement.textRange.startOffset
                val errorEnd = errorElement.textRange.endOffset

                if (overlapsRange(errorStart, errorEnd, startOffset, endOffset)) {
                    createIssueFromPsiError(errorElement)?.let { problems.add(it) }
                }
            }

            // Retained as a safety net: an element spanning at least one character
            // still cannot match an empty range (a selection covering a single
            // blank line). It only contributes when a parser exposes the error
            // element itself as the leaf at this offset, which the bundled XML and
            // JSON parsers never do.
            val elementAtStart = psiFile.findElementAt(startOffset)
            if (elementAtStart is PsiErrorElement) {
                createIssueFromPsiError(elementAtStart)?.let { problems.add(it) }
            }
        } catch (_: Exception) {
        }

        return problems
    }
}