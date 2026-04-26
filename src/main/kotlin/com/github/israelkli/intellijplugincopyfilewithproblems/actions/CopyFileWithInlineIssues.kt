package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager

class CopyFileWithInlineIssues : BaseFileAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return

        if (ApplicationManager.getApplication().isUnitTestMode) {
            val result = buildFileContentWithInlineIssues(psiFile, document, project, virtualFile)
            copyToClipboard(result)
            return
        }

        object : Task.Backgroundable(project, "Analyzing file with inline issues", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Running IDE inspections..."

                val result = ApplicationManager.getApplication().runReadAction(Computable {
                    buildFileContentWithInlineIssues(psiFile, document, project, virtualFile)
                })

                ApplicationManager.getApplication().invokeLater {
                    copyToClipboard(result)
                }
            }
        }.queue()
    }

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = e.getData(CommonDataKeys.PROJECT)

        val isEnabled = (virtualFile != null) &&
                       (project != null) &&
                       (!virtualFile.isDirectory)

        e.presentation.isEnabledAndVisible = isEnabled
        e.presentation.text = "Copy File with Inline Issues"
    }
}
