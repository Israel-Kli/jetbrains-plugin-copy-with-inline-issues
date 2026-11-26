package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CancellationTest : BasePlatformTestCase() {

    fun testCancelledIndicatorStopsProblemDetection() {
        val psiFile = myFixture.configureByText("broken.xml", "<root>\n  <unclosed>\n</root>")
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val indicator = EmptyProgressIndicator()
        indicator.cancel()

        var cancelled = false
        try {
            ProgressManager.getInstance().runProcess({
                ProblemDetectionService().findProblemsForFile(psiFile, document, 0, document.textLength)
            }, indicator)
        } catch (_: ProcessCanceledException) {
            cancelled = true
        }

        assertTrue("Problem detection must stop when its progress indicator is cancelled", cancelled)
    }
}
