package com.github.israelkli.intellijplugincopyfilewithproblems.actions

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Verifies comment delimiters per file type.
 *
 * These assertions are deliberately exact. Earlier versions asserted
 * `prefix in listOf("# ", "// ")`, which also accepted the `"// "` fallback and
 * therefore passed even when the mapping was wrong or absent.
 *
 * The expected values must hold whether the style is resolved from the language
 * ID or from the file extension, because the test IDE bundles no language
 * plugins (nearly every fixture file reports `language.id == "TEXT"`) while a
 * real IDE resolves a precise language. Both paths must agree.
 */
class BaseFileActionTest : BasePlatformTestCase() {

    private val action = CopyFileWithInlineIssues()

    /** fileName -> (expected prefix, expected suffix) */
    private val expectedStyles: Map<String, Pair<String, String>> = mapOf(
        // Hash-comment languages
        "test.py" to ("# " to ""),
        "test.rb" to ("# " to ""),
        "test.sh" to ("# " to ""),
        "test.bash" to ("# " to ""),
        "test.zsh" to ("# " to ""),
        "test.yaml" to ("# " to ""),
        "test.yml" to ("# " to ""),
        "test.toml" to ("# " to ""),
        "app.properties" to ("# " to ""),
        "test.tf" to ("# " to ""),
        "test.graphql" to ("# " to ""),
        "test.pl" to ("# " to ""),
        "test.ex" to ("# " to ""),
        "test.r" to ("# " to ""),
        "test.jl" to ("# " to ""),
        "test.nim" to ("# " to ""),
        "test.ps1" to ("# " to ""),
        "Dockerfile" to ("# " to ""),
        "Makefile" to ("# " to ""),

        // Semicolon-comment languages
        "config.ini" to ("; " to ""),
        "test.clj" to ("; " to ""),
        "test.lisp" to ("; " to ""),
        "test.el" to ("; " to ""),
        "test.ahk" to ("; " to ""),

        // Percent-comment languages
        "test.erl" to ("% " to ""),
        "test.hrl" to ("% " to ""),

        // Double-dash-comment languages
        "test.sql" to ("-- " to ""),
        "test.lua" to ("-- " to ""),
        "test.hs" to ("-- " to ""),
        "test.adb" to ("-- " to ""),
        "test.vhd" to ("-- " to ""),

        // Block comments - opener REQUIRES a matching closer
        "test.html" to ("<!-- " to " -->"),
        "test.xml" to ("<!-- " to " -->"),
        "icon.svg" to ("<!-- " to " -->"),
        "notes.md" to ("<!-- " to " -->"),
        "test.css" to ("/* " to " */"),
        "test.twig" to ("{# " to " #}"),
        "test.jinja" to ("{# " to " #}"),
        "test.j2" to ("{# " to " #}"),
        "test.hbs" to ("{{! " to " }}"),
        "test.erb" to ("<%# " to " %>"),
        "test.ejs" to ("<%# " to " %>"),
        "test.ml" to ("(* " to " *)"),
        "test.mli" to ("(* " to " *)"),

        // Line-comment languages (CSS preprocessors use // and must NOT inherit
        // the CSS block-comment suffix)
        "test.scss" to ("// " to ""),
        "test.sass" to ("// " to ""),
        "test.less" to ("// " to ""),
        "Test.java" to ("// " to ""),
        "Test.kt" to ("// " to ""),
        "test.js" to ("// " to ""),
        "test.ts" to ("// " to ""),
        "test.go" to ("// " to ""),
        "test.rs" to ("// " to ""),
        "test.swift" to ("// " to ""),
        "test.dart" to ("// " to ""),
        "test.cs" to ("// " to ""),
        "test.proto" to ("// " to ""),
        "test.groovy" to ("// " to ""),
        "test.sv" to ("// " to "")
    )

    fun testCommentStylePerFileType() {
        val failures = mutableListOf<String>()

        for ((fileName, expected) in expectedStyles) {
            val (expectedPrefix, expectedSuffix) = expected
            val psiFile = myFixture.configureByText(fileName, "placeholder")
            val actualPrefix = action.getCommentPrefix(psiFile)
            val actualSuffix = action.getCommentSuffix(psiFile)

            if (actualPrefix != expectedPrefix || actualSuffix != expectedSuffix) {
                failures += "$fileName (lang=${psiFile.language.id}): " +
                    "expected '$expectedPrefix'…'$expectedSuffix' " +
                    "but got '$actualPrefix'…'$actualSuffix'"
            }
        }

        assertTrue(
            "Wrong comment style for ${failures.size} file type(s):\n" +
                failures.joinToString("\n") { "  - $it" },
            failures.isEmpty()
        )
    }

    /**
     * Structural invariant: a prefix that opens a block or template comment must
     * come with the delimiter that closes it, otherwise the emitted comment
     * swallows the rest of the copied text.
     */
    fun testBlockCommentOpenersAlwaysHaveClosers() {
        val requiredClosers = mapOf(
            "<!-- " to " -->",
            "/* " to " */",
            "{# " to " #}",
            "{{! " to " }}",
            "<%# " to " %>",
            "(* " to " *)"
        )
        val failures = mutableListOf<String>()

        for (fileName in expectedStyles.keys) {
            val psiFile = myFixture.configureByText(fileName, "placeholder")
            val prefix = action.getCommentPrefix(psiFile)
            val expectedCloser = requiredClosers[prefix] ?: continue
            val actualSuffix = action.getCommentSuffix(psiFile)

            if (actualSuffix != expectedCloser) {
                failures += "$fileName: prefix '$prefix' opens a block comment " +
                    "but suffix is '$actualSuffix' (expected '$expectedCloser')"
            }
        }

        assertTrue(
            "Unterminated block comments for ${failures.size} file type(s):\n" +
                failures.joinToString("\n") { "  - $it" },
            failures.isEmpty()
        )
    }

    /** A line-comment prefix must never carry a block-comment closer. */
    fun testLineCommentsHaveNoSuffix() {
        val lineCommentPrefixes = setOf("// ", "# ", "-- ", "; ", "% ")
        val failures = mutableListOf<String>()

        for (fileName in expectedStyles.keys) {
            val psiFile = myFixture.configureByText(fileName, "placeholder")
            val prefix = action.getCommentPrefix(psiFile)
            if (prefix !in lineCommentPrefixes) continue
            val suffix = action.getCommentSuffix(psiFile)

            if (suffix.isNotEmpty()) {
                failures += "$fileName: line-comment prefix '$prefix' " +
                    "must have an empty suffix but got '$suffix'"
            }
        }

        assertTrue(
            "Spurious suffix on line comments for ${failures.size} file type(s):\n" +
                failures.joinToString("\n") { "  - $it" },
            failures.isEmpty()
        )
    }

    fun testUnknownExtensionFallsBackToCStyle() {
        val psiFile = myFixture.configureByText("mystery.qqzz", "content")
        assertEquals("// ", action.getCommentPrefix(psiFile))
        assertEquals("", action.getCommentSuffix(psiFile))
    }

    fun testExtensionLookupIsCaseInsensitive() {
        val lower = myFixture.configureByText("a.py", "x=1")
        val upper = myFixture.configureByText("b.PY", "x=1")
        assertEquals(
            "Extension matching must ignore case",
            action.getCommentPrefix(lower),
            action.getCommentPrefix(upper)
        )
    }

    fun testFormattedCommentIsWellFormedForBlockLanguages() {
        val markdown = myFixture.configureByText("readme.md", "# Title")
        val prefix = action.getCommentPrefix(markdown)
        val suffix = action.getCommentSuffix(markdown)
        val comment = "${prefix}ERROR: something broke$suffix"

        assertEquals("<!-- ERROR: something broke -->", comment)
    }

    fun testActionThreadConfiguration() {
        assertEquals(
            "Should use BGT thread",
            com.intellij.openapi.actionSystem.ActionUpdateThread.BGT,
            action.actionUpdateThread
        )
    }
}
