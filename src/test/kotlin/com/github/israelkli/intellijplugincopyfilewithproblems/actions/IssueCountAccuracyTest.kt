package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * `buildContentWithProblems` and `buildFileContentWithInlineIssues` are
 * `protected`; this subclass exists only to reach them from a test.
 */
private class ProbeAction : BaseFileAction() {
    override fun actionPerformed(e: AnActionEvent) {}

    fun buildWholeFile(psiFile: PsiFile, document: Document) =
        buildFileContentWithInlineIssues(psiFile, document, psiFile.project, psiFile.virtualFile!!)

    fun buildRange(psiFile: PsiFile, document: Document, lineStart: Int, lineEnd: Int) =
        buildContentWithProblems(psiFile, document, lineStart, lineEnd) { it }
}

class IssueCountAccuracyTest : BasePlatformTestCase() {

    private val probe = ProbeAction()

    fun testDecorativeSeverityTextIsNotCountedAsAnIssue() {
        // Plain text has no grammar, no registered inspections, and no daemon
        // highlights, so the real, filtered issue count here is unambiguously
        // zero. The body still contains "ERROR: " and "WARNING: " as literal
        // text, which is exactly what inflated the old regex-based count.
        val text = """
            Deploy log:
            ERROR: disk full on host web-3
            WARNING: retrying in 30s
            everything recovered after that
        """.trimIndent()

        val psiFile = myFixture.configureByText("evidence.txt", text)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val wholeFile = probe.buildWholeFile(psiFile, document)
        assertEquals(
            "Decorative text containing 'ERROR:'/'WARNING:' must not be counted as an issue",
            0, wholeFile.issueCount
        )

        val range = probe.buildRange(psiFile, document, 0, document.lineCount - 1)
        assertEquals(
            "Decorative text containing 'ERROR:'/'WARNING:' must not be counted as an issue",
            0, range.issueCount
        )
    }

    fun testRealIssueIsCountedAccurately() {
        val xmlCode = "<root>\n    <unclosed>\n</root>"
        val psiFile = myFixture.configureByText("real.xml", xmlCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val wholeFile = probe.buildWholeFile(psiFile, document)
        assertEquals("Whole-file copy must count the one real issue", 1, wholeFile.issueCount)
        assertTrue(
            "The comment for the real issue must still be present",
            wholeFile.content.contains("not closed")
        )

        val range = probe.buildRange(psiFile, document, 0, document.lineCount - 1)
        assertEquals("A selection covering the issue must count it too", 1, range.issueCount)
    }

    fun testCountSumsAcrossDistinctFindingsOnTheSameLine() {
        // Several distinct findings land on the same line here (see
        // ProblemDetectionServiceTest.testDuplicateFindingsAreCollapsedPerLine).
        val xmlCode = "<root>\n\n</root>\n<<<"
        val psiFile = myFixture.configureByText("multi.xml", xmlCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val wholeFile = probe.buildWholeFile(psiFile, document)
        assertEquals(
            "Both distinct findings on the shared line must be counted",
            2, wholeFile.issueCount
        )
    }
}
