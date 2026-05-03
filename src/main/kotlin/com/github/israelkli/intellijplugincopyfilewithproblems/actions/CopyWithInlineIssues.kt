package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.Computable

class CopyWithInlineIssues : BaseFileAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return

        val document = editor.document
        val startLine = document.getLineNumber(selectionModel.selectionStart)
        val endLine = document.getLineNumber(selectionModel.selectionEnd)
        val virtualFile = psiFile.virtualFile

        if (ApplicationManager.getApplication().isUnitTestMode) {
            val result = buildContentWithProblems(psiFile, document, startLine, endLine) { fileName -> fileName }
            copyToClipboard(result)
            notifyCopyResult(project, endLine - startLine + 1, 0)
            return
        }

        if (!confirmLargeFileCopy(endLine - startLine + 1, virtualFile?.name)) return

        object : Task.Backgroundable(project, "Analyzing with inline issues", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Running IDE inspections..."

                val pair = ApplicationManager.getApplication().runReadAction(Computable {
                    val content = buildContentWithProblems(psiFile, document, startLine, endLine) { fileName -> fileName }
                    val count = countIssueMarkers(content)
                    content to count
                })

                ApplicationManager.getApplication().invokeLater {
                    copyToClipboard(pair.first)
                    notifyCopyResult(project, endLine - startLine + 1, pair.second)
                }
            }
        }.queue()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        val isEnabled = (project != null) &&
                       (editor != null) &&
                       (psiFile != null) &&
                       (editor.selectionModel.hasSelection())

        e.presentation.isEnabledAndVisible = isEnabled
        e.presentation.text = "Copy with Inline Issues"
    }
}
