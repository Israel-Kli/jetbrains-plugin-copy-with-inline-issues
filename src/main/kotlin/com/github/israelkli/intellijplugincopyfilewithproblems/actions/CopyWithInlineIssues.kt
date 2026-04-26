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

        if (ApplicationManager.getApplication().isUnitTestMode) {
            val result = buildContentWithProblems(psiFile, document, startLine, endLine) { fileName -> fileName }
            copyToClipboard(result)
            return
        }

        object : Task.Backgroundable(project, "Analyzing with inline issues", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Running IDE inspections..."

                val result = ApplicationManager.getApplication().runReadAction(Computable {
                    buildContentWithProblems(psiFile, document, startLine, endLine) { fileName -> fileName }
                })

                ApplicationManager.getApplication().invokeLater {
                    copyToClipboard(result)
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
