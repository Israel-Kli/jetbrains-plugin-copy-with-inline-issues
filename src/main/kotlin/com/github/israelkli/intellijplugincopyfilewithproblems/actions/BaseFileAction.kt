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

    /**
     * Opening and closing comment delimiters, kept together so they can never
     * diverge. Emitting an opener without its closer turns the rest of the
     * copied text into a comment.
     */
    data class CommentStyle(val prefix: String, val suffix: String = "")

    companion object {
        const val LARGE_FILE_THRESHOLD = 500

        private val SLASH = CommentStyle("// ")
        private val HASH = CommentStyle("# ")
        private val DASH = CommentStyle("-- ")
        private val SEMICOLON = CommentStyle("; ")
        private val PERCENT = CommentStyle("% ")
        private val ANGLE_BANG = CommentStyle("<!-- ", " -->")
        private val SLASH_STAR = CommentStyle("/* ", " */")
        private val BRACE_HASH = CommentStyle("{# ", " #}")
        private val MUSTACHE = CommentStyle("{{! ", " }}")
        private val ERB = CommentStyle("<%# ", " %>")
        private val PAREN_STAR = CommentStyle("(* ", " *)")

        // Layer 1: exact language ID (lowercased). Authoritative when the
        // matching language plugin is installed.
        private val STYLE_BY_LANGUAGE_ID: Map<String, CommentStyle> = mapOf(
            "python" to HASH,
            "ruby" to HASH,
            "shell" to HASH,
            "shellscript" to HASH,
            "bash" to HASH,
            "zsh" to HASH,
            "yaml" to HASH,
            "toml" to HASH,
            "dockerfile" to HASH,
            "makefile" to HASH,
            "properties" to HASH,
            "elixir" to HASH,
            "julia" to HASH,
            "nim" to HASH,
            "powershell" to HASH,
            "graphql" to HASH,
            "hcl" to HASH,
            "r" to HASH,
            "ini" to SEMICOLON,
            "clojure" to SEMICOLON,
            "lisp" to SEMICOLON,
            "emacslisp" to SEMICOLON,
            "erlang" to PERCENT,
            "sql" to DASH,
            "mysql" to DASH,
            "postgresql" to DASH,
            "lua" to DASH,
            "haskell" to DASH,
            "ada" to DASH,
            "vhdl" to DASH,
            "html" to ANGLE_BANG,
            "xhtml" to ANGLE_BANG,
            "xml" to ANGLE_BANG,
            "svg" to ANGLE_BANG,
            "markdown" to ANGLE_BANG,
            "css" to SLASH_STAR,
            // CSS preprocessors support line comments, so they must not inherit
            // the CSS block-comment delimiters.
            "scss" to SLASH,
            "sass" to SLASH,
            "less" to SLASH,
            "twig" to BRACE_HASH,
            "jinja" to BRACE_HASH,
            "handlebars" to MUSTACHE,
            "ocaml" to PAREN_STAR
        )

        // Layer 2: well-known file names that carry no extension, so Layer 3
        // cannot classify them.
        private val STYLE_BY_FILENAME: Map<String, CommentStyle> = mapOf(
            "dockerfile" to HASH,
            "containerfile" to HASH,
            "makefile" to HASH,
            "gnumakefile" to HASH,
            "cmakelists.txt" to HASH,
            "gemfile" to HASH,
            "rakefile" to HASH,
            "podfile" to HASH,
            "brewfile" to HASH,
            "procfile" to HASH,
            "vagrantfile" to HASH,
            ".gitignore" to HASH,
            ".gitattributes" to HASH,
            ".dockerignore" to HASH,
            ".editorconfig" to HASH,
            ".env" to HASH,
            ".bashrc" to HASH,
            ".zshrc" to HASH,
            ".profile" to HASH,
            "jenkinsfile" to SLASH
        )

        // Layer 3: file extension (lowercased). This is the fallback for IDEs
        // that do not bundle the relevant language plugin, in which case
        // `language.id` reports "TEXT". Every entry must resolve to the same
        // style as its Layer 1 counterpart.
        private val STYLE_BY_EXTENSION: Map<String, CommentStyle> = mapOf(
            "py" to HASH,
            "pyi" to HASH,
            "rb" to HASH,
            "sh" to HASH,
            "bash" to HASH,
            "zsh" to HASH,
            "fish" to HASH,
            "yaml" to HASH,
            "yml" to HASH,
            "toml" to HASH,
            "properties" to HASH,
            "conf" to HASH,
            "cfg" to HASH,
            "tf" to HASH,
            "tfvars" to HASH,
            "hcl" to HASH,
            "graphql" to HASH,
            "gql" to HASH,
            "ex" to HASH,
            "exs" to HASH,
            "pl" to HASH,
            "pm" to HASH,
            "t" to HASH,
            "raku" to HASH,
            "rakumod" to HASH,
            "r" to HASH,
            "jl" to HASH,
            "nim" to HASH,
            "nimble" to HASH,
            "ps1" to HASH,
            "psm1" to HASH,
            "psd1" to HASH,
            "ini" to SEMICOLON,
            "clj" to SEMICOLON,
            "cljs" to SEMICOLON,
            "cljc" to SEMICOLON,
            "edn" to SEMICOLON,
            "lisp" to SEMICOLON,
            "el" to SEMICOLON,
            "scm" to SEMICOLON,
            "ahk" to SEMICOLON,
            "au3" to SEMICOLON,
            "erl" to PERCENT,
            "hrl" to PERCENT,
            "tex" to PERCENT,
            "sql" to DASH,
            "lua" to DASH,
            "hs" to DASH,
            "lhs" to DASH,
            "adb" to DASH,
            "ads" to DASH,
            "vhd" to DASH,
            "vhdl" to DASH,
            "elm" to DASH,
            "html" to ANGLE_BANG,
            "htm" to ANGLE_BANG,
            "xhtml" to ANGLE_BANG,
            "xml" to ANGLE_BANG,
            "xsd" to ANGLE_BANG,
            "xsl" to ANGLE_BANG,
            "svg" to ANGLE_BANG,
            "md" to ANGLE_BANG,
            "markdown" to ANGLE_BANG,
            "css" to SLASH_STAR,
            "scss" to SLASH,
            "sass" to SLASH,
            "less" to SLASH,
            "styl" to SLASH,
            "twig" to BRACE_HASH,
            "jinja" to BRACE_HASH,
            "jinja2" to BRACE_HASH,
            "j2" to BRACE_HASH,
            "hbs" to MUSTACHE,
            "handlebars" to MUSTACHE,
            "mustache" to MUSTACHE,
            "erb" to ERB,
            "ejs" to ERB,
            "ml" to PAREN_STAR,
            "mli" to PAREN_STAR,
            "proto" to SLASH,
            "gradle" to SLASH,
            "groovy" to SLASH,
            "dart" to SLASH,
            "swift" to SLASH,
            "rs" to SLASH,
            "go" to SLASH,
            "zig" to SLASH,
            "vue" to SLASH,
            "svelte" to SLASH,
            "astro" to SLASH,
            "blade" to SLASH,
            "cs" to SLASH,
            "fs" to SLASH,
            "fsx" to SLASH,
            "m" to SLASH,
            "mm" to SLASH,
            "pas" to SLASH,
            "pp" to SLASH,
            "v" to SLASH,
            "sv" to SLASH,
            "svh" to SLASH
        )

        // Layer 4: language ID fragments, restricted to identifiers that cannot
        // produce false positives (e.g. "cpp" matching "objective-cpp").
        private val STYLE_BY_LANGUAGE_FRAGMENT: List<Pair<String, CommentStyle>> = listOf(
            "kotlin" to SLASH,
            "typescript" to SLASH,
            "javascript" to SLASH,
            "php" to SLASH,
            "csharp" to SLASH,
            "scala" to SLASH,
            "groovy" to SLASH,
            "objectivecpp" to SLASH,
            "ruby" to HASH,
            "python" to HASH,
            "perl" to HASH,
            "coffeescript" to HASH,
            "terraform" to HASH
        )
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    protected fun copyToClipboard(content: String) {
        val selection = StringSelection(content)
        CopyPasteManager.getInstance().setContents(selection)
    }

    private fun resolveCommentStyle(psiFile: PsiFile): CommentStyle {
        val languageId = psiFile.language.id.lowercase()
        val virtualFile = psiFile.virtualFile
        val fileName = virtualFile?.name?.lowercase() ?: ""
        val extension = virtualFile?.extension?.lowercase() ?: ""

        STYLE_BY_LANGUAGE_ID[languageId]?.let { return it }
        STYLE_BY_FILENAME[fileName]?.let { return it }
        STYLE_BY_EXTENSION[extension]?.let { return it }

        for ((fragment, style) in STYLE_BY_LANGUAGE_FRAGMENT) {
            if (languageId.contains(fragment)) return style
        }

        return SLASH
    }

    fun getCommentPrefix(psiFile: PsiFile): String = resolveCommentStyle(psiFile).prefix

    fun getCommentSuffix(psiFile: PsiFile): String = resolveCommentStyle(psiFile).suffix

    protected fun formatComment(psiFile: PsiFile, severityPrefix: String, message: String): String {
        val style = resolveCommentStyle(psiFile)
        return "${style.prefix}$severityPrefix: $message${style.suffix}"
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
            builder.append(formatComment(psiFile, issue.severity, issue.message))
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
