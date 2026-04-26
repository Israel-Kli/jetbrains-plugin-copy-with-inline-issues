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

        return allIssues
            .groupBy { document.getLineNumber(it.startOffset) }
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
                if (highlighter.startOffset < endOffset && highlighter.endOffset > startOffset) {
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

                                if (elementStart < endOffset && elementEnd > startOffset) {
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
            val rootElement = psiFile.findElementAt(startOffset)?.containingFile
            if (rootElement != null) {
                val errorElements = PsiTreeUtil.findChildrenOfType(rootElement, PsiErrorElement::class.java)
                for (errorElement in errorElements) {
                    val errorStart = errorElement.textRange.startOffset
                    val errorEnd = errorElement.textRange.endOffset

                    if (errorStart < endOffset && errorEnd > startOffset) {
                        createIssueFromPsiError(errorElement)?.let { problems.add(it) }
                    }
                }
            }

            val elementAtStart = psiFile.findElementAt(startOffset)
            if (elementAtStart is PsiErrorElement) {
                createIssueFromPsiError(elementAtStart)?.let { problems.add(it) }
            }

            var offset = startOffset
            var elementCount = 0
            while (offset < endOffset && elementCount < 10) {
                val element = psiFile.findElementAt(offset)
                if (element is PsiErrorElement) {
                    createIssueFromPsiError(element)?.let { problems.add(it) }
                }
                offset += maxOf(1, element?.textLength ?: 1)
                elementCount++
            }

        } catch (_: Exception) {
        }

        return problems
    }
}