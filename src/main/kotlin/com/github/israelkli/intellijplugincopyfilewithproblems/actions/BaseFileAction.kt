package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.github.israelkli.intellijplugincopyfilewithproblems.services.ProblemDetectionService
import com.github.israelkli.intellijplugincopyfilewithproblems.settings.PluginSettings
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import java.awt.datatransfer.StringSelection

abstract class BaseFileAction : AnAction() {

    protected val problemDetectionService = ProblemDetectionService()

    companion object {
        const val LARGE_FILE_THRESHOLD = 500
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    
    protected fun copyToClipboard(content: String) {
        val selection = StringSelection(content)
        CopyPasteManager.getInstance().setContents(selection)
    }
    
    fun getCommentPrefix(psiFile: PsiFile): String {
        val language = psiFile.language
        val languageId = language.id.lowercase()
        val extension = psiFile.virtualFile?.extension?.lowercase() ?: ""

        // Layer 1: exact language ID match
        val exactMatch = LANGUAGE_EXACT[languageId]
        if (exactMatch != null) return exactMatch

        // Layer 2: file extension match
        val extensionMatch = LANGUAGE_BY_EXTENSION[extension]
        if (extensionMatch != null) return extensionMatch

        // Layer 3: language ID substring check (restricted to safe identifiers
        // that can't produce false positives, e.g. "cpp" matching "objective-cpp")
        for ((key, prefix) in LANGUAGE_SUBSTRING) {
            if (languageId.contains(key)) return prefix
        }

        // Layer 4: default C-style
        return "// "
    }

    private val LANGUAGE_EXACT = mapOf(
        "python"        to "# ",
        "ruby"          to "# ",
        "shell"         to "# ",
        "bash"          to "# ",
        "yaml"          to "# ",
        "toml"          to "# ",
        "dockerfile"    to "# ",
        "makefile"      to "# ",
        "properties"    to "# ",
        "ini"           to "; ",
        "sql"           to "-- ",
        "lua"           to "-- ",
        "haskell"       to "-- ",
        "html"          to "<!-- ",
        "xml"           to "<!-- ",
        "css"           to "/* ",
        "scss"          to "// ",
        "sass"          to "// ",
        "less"          to "// "
    )

    // File extensions that differ from language ID. Extensions that already
    // match an exact-language ID (e.g. ".py" → "python") are omitted since
    // Layer 1 handles those via the language object.
    private val LANGUAGE_BY_EXTENSION = mapOf(
        // Languages where IntelliJ may not report a precise language ID
        "sh"            to "# ",
        "bash"          to "# ",
        "zsh"           to "# ",
        "yaml"          to "# ",
        "yml"           to "# ",
        "toml"          to "# ",
        "properties"    to "# ",
        "ini"           to "; ",
        "conf"          to "# ",
        "cfg"           to "# ",
        "tf"            to "# ",
        "hcl"           to "# ",
        "graphql"       to "# ",
        "gql"           to "# ",
        "proto"         to "// ",
        "gradle"        to "// ",
        "groovy"        to "// ",
        "dart"          to "// ",
        "swift"         to "// ",
        "rs"            to "// ",
        "go"            to "// ",
        "zig"           to "// ",
        "v"             to "// ",
        "vue"           to "// ",
        "svelte"        to "// ",
        "astro"         to "// ",
        "blade"         to "// ",
        "twig"          to "{# ",
        "jinja"         to "{# ",
        "j2"            to "{# ",
        "hbs"           to "{{! ",
        "erb"           to "<%# ",
        "ejs"           to "<%# ",
        "md"            to "<!-- ",
        "markdown"      to "<!-- ",
        "svg"           to "<!-- ",
        "cs"            to "// ",
        "fs"            to "// ",
        "fsx"           to "// ",
        "ex"            to "# ",
        "exs"           to "# ",
        "erl"           to "% ",
        "hrl"           to "% ",
        "clj"           to "; ",
        "cljs"          to "; ",
        "lisp"          to "; ",
        "el"            to "; ",
        "r"             to "# ",
        "R"             to "# ",
        "jl"            to "# ",
        "m"             to "// ",
        "mm"            to "// ",
        "ml"            to "(* ",
        "mli"           to "(* ",
        "hs"            to "-- ",
        "lhs"           to "-- ",
        "adb"           to "-- ",
        "ads"           to "-- ",
        "pas"           to "// ",
        "pp"            to "// ",
        "pl"            to "# ",
        "pm"            to "# ",
        "t"             to "# ",
        "raku"          to "# ",
        "rakumod"       to "# ",
        "nim"           to "# ",
        "nimble"        to "# ",
        "vhd"           to "-- ",
        "vhdl"          to "-- ",
        "vhd"           to "-- ",
        "v"             to "// ",
        "sv"            to "// ",
        "svh"           to "// ",
        "ahk"           to "; ",
        "au3"           to "; ",
        "ps1"           to "# ",
        "psm1"          to "# ",
        "psd1"          to "# "
    )

    private val LANGUAGE_SUBSTRING = mapOf(
        "kotlin"        to "// ",
        "typescript"    to "// ",
        "javascript"    to "// ",
        "php"           to "// ",
        "csharp"        to "// ",
        "scala"         to "// ",
        "groovy"        to "// ",
        "objectivecpp"  to "// ",
        "ruby"          to "# ",
        "python"        to "# ",
        "perl"          to "# ",
        "coffeescript"  to "# ",
        "terraform"     to "# "
    )
    
    fun getCommentSuffix(psiFile: PsiFile): String {
        val language = psiFile.language
        val languageId = language.id.lowercase()
        
        return when {
            languageId.contains("html") -> " -->"
            languageId.contains("xml") -> " -->"
            languageId.contains("css") -> " */"
            else -> ""
        }
    }
    
    protected fun formatComment(psiFile: PsiFile, severityPrefix: String, message: String): String {
        val prefix = getCommentPrefix(psiFile)
        val suffix = getCommentSuffix(psiFile)
        return "$prefix$severityPrefix: $message$suffix"
    }
    
    private fun filterIssues(issues: List<ProblemDetectionService.IssueInfo>): List<ProblemDetectionService.IssueInfo> {
        val settings = PluginSettings.getInstance().state
        return issues.filter { issue ->
            when (issue.severity) {
                "ERROR" -> settings.severityFilterErrors
                "WARNING" -> settings.severityFilterWarnings
                "WEAK_WARNING" -> settings.severityFilterWeakWarnings
                "INFO" -> settings.severityFilterInfo
                "INSPECTION" -> settings.severityFilterWarnings
                else -> true
            }
        }
    }

    private fun appendIssueComments(
        builder: StringBuilder,
        psiFile: PsiFile,
        issues: List<ProblemDetectionService.IssueInfo>
    ) {
        for (issue in filterIssues(issues)) {
            builder.appendLine()
            val severityPrefix = when (issue.severity) {
                "ERROR" -> "ERROR"
                "WARNING" -> "WARNING"
                "WEAK_WARNING" -> "WEAK_WARNING"
                "INFO" -> "INFO"
                "INSPECTION" -> "INSPECTION"
                else -> issue.severity
            }
            builder.append(formatComment(psiFile, severityPrefix, issue.message))
        }
    }
    
    protected fun buildContentWithProblems(
        psiFile: PsiFile,
        document: com.intellij.openapi.editor.Document,
        lineStart: Int,
        lineEnd: Int,
        headerProvider: (String) -> String
    ): String {
        val issuesByLine = problemDetectionService.findProblemsForFile(
            psiFile, document,
            document.getLineStartOffset(lineStart),
            document.getLineEndOffset(lineEnd)
        )

        return buildString {
            val virtualFile = psiFile.virtualFile
            if (virtualFile != null) {
                val headerComment = formatComment(psiFile, "File", headerProvider(virtualFile.name))
                appendLine(headerComment)
                appendLine()
            }

            for (lineNumber in lineStart..lineEnd) {
                val lineStartOffset = document.getLineStartOffset(lineNumber)
                val lineEndOffset = document.getLineEndOffset(lineNumber)
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))

                append(lineText)
                appendIssueComments(this, psiFile, issuesByLine[lineNumber].orEmpty())
                appendLine()
            }
        }
    }

    protected fun buildFileContentWithInlineIssues(
        psiFile: PsiFile,
        document: com.intellij.openapi.editor.Document,
        project: com.intellij.openapi.project.Project,
        virtualFile: com.intellij.openapi.vfs.VirtualFile
    ): String {
        val issuesByLine = problemDetectionService.findProblemsForFile(
            psiFile, document,
            0, document.textLength
        )

        return buildString {
            val projectBasePath = project.basePath
            val relativePath = if (projectBasePath != null && virtualFile.path.startsWith(projectBasePath)) {
                virtualFile.path.substring(projectBasePath.length).removePrefix("/")
            } else {
                virtualFile.path
            }

            val headerComment = formatComment(psiFile, "File", relativePath)
            appendLine(headerComment)
            appendLine()

            val fileContent = document.text
            val lines = fileContent.lines()
            lines.forEachIndexed { index, line ->
                append(line)
                appendIssueComments(this, psiFile, issuesByLine[index].orEmpty())
                appendLine()
            }
        }
    }

    protected fun isCommentIncompatibleLanguage(psiFile: PsiFile): Boolean {
        val languageId = psiFile.language.id.lowercase()
        val extension = psiFile.virtualFile?.extension?.lowercase() ?: ""
        return languageId.contains("json") || extension == "json" ||
               languageId.contains("xml") && !languageId.contains("html")
    }

    protected fun confirmLargeFileCopy(lineCount: Int, fileName: String?): Boolean {
        if (ApplicationManager.getApplication().isUnitTestMode) return true
        if (lineCount < LARGE_FILE_THRESHOLD) return true

        val displayName = fileName ?: "file"
        val result = ApplicationManager.getApplication().runReadAction(com.intellij.openapi.util.Computable {
            Messages.showYesNoDialog(
                "The selected range in \"$displayName\" has $lineCount lines. " +
                    "Running IDE inspections on a large selection may take several seconds and cause brief UI lag.\n\nContinue?",
                "Large Selection — Copy with Inline Issues",
                "Copy Anyway",
                "Cancel",
                Messages.getWarningIcon()
            )
        })
        return result == Messages.YES
    }

    protected fun countIssueMarkers(content: String): Int {
        val pattern = Regex("""(ERROR|WARNING|WEAK_WARNING|INFO|INSPECTION):""")
        return pattern.findAll(content).count()
    }

    protected fun notifyCopyResult(project: Project?, lineCount: Int, issueCount: Int) {
        val level = try {
            PluginSettings.getInstance().state.notificationLevel
        } catch (_: Exception) {
            "STATUS_BAR"
        }
        if (level == "NONE") return

        val title = "Copied with inline issues"
        val content = "$lineCount lines copied" + if (issueCount > 0) ", $issueCount issues inlined" else ", no issues found"

        val notificationType = if (issueCount > 0) NotificationType.INFORMATION else NotificationType.WARNING

        try {
            val notificationGroup: NotificationGroup =
                NotificationGroupManager.getInstance().getNotificationGroup("Copy with inline issues")
            val notification = notificationGroup.createNotification(title, content, notificationType)
            if (project != null) {
                notification.notify(project)
            }
        } catch (_: Exception) {
            // Fallback: notification group not available in this IDE
        }
    }
}