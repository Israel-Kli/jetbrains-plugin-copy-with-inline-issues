package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CancellationTest : BasePlatformTestCase() {

    /** Runs [action] under an indicator that is cancelled once the action has started, as the Cancel button does. */
    private fun isCancelled(action: () -> Unit): Boolean {
        val indicator = EmptyProgressIndicator()
        return try {
            ProgressManager.getInstance().runProcess({
                indicator.cancel()
                action()
            }, indicator)
            false
        } catch (_: ProcessCanceledException) {
            true
        }
    }

    // Guards the harness itself: if this fails, the test below proves nothing.
    fun testHarnessDeliversCancellation() {
        assertTrue(isCancelled { ProgressManager.checkCanceled() })
    }

    fun testCancelledIndicatorStopsProblemDetection() {
        val psiFile = myFixture.configureByText("broken.xml", "<root>\n  <unclosed>\n</root>")
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val cancelled = isCancelled {
            ProblemDetectionService().findProblemsForFile(psiFile, document, 0, document.textLength)
        }

        assertTrue("Problem detection must stop when its progress indicator is cancelled", cancelled)
    }
}
