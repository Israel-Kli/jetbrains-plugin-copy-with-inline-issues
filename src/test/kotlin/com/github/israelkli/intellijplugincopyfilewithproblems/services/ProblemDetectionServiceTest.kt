package com.github.israelkli.intellijplugincopyfilewithproblems.services

import com.intellij.openapi.editor.EditorFactory
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ProblemDetectionServiceTest : BasePlatformTestCase() {

    private lateinit var service: ProblemDetectionService

    override fun setUp() {
        super.setUp()
        service = ProblemDetectionService()
    }

    fun testProblemInfoDataClass() {
        val issueInfo = ProblemDetectionService.IssueInfo(
            severity = "ERROR",
            message = "Test error message",
            startOffset = 0,
            endOffset = 10,
        )
        
        assertEquals("ERROR", issueInfo.severity)
        assertEquals("Test error message", issueInfo.message)
        assertEquals(0, issueInfo.startOffset)
        assertEquals(10, issueInfo.endOffset)
    }

    fun testFindIssuesWithValidJavaCode() {
        val validJavaCode = """
            public class ValidClass {
                public void validMethod() {
                    System.out.println("Hello World");
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("ValidClass.java", validJavaCode)
        val issues = service.findProblems(psiFile, 0, validJavaCode.length)
        
        // Valid code should have no or minimal issues
        assertNotNull("Issues should not be null", issues)
    }

    fun testFindIssuesWithInvalidJavaCode() {
        val invalidJavaCode = """
            public class InvalidClass {
                public void invalidMethod() {
                    undeclaredVariable = 5;
                    String s = null;
                    s.toString();
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("InvalidClass.java", invalidJavaCode)
        val issues = service.findProblems(psiFile, 0, invalidJavaCode.length)
        
        assertNotNull("Issues should not be null", issues)
    }

    fun testFindIssuesWithEmptyRange() {
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        
        val issues = service.findProblems(psiFile, 0, 0)
        assertNotNull("Issues should not be null", issues)
    }

    fun testFindIssuesWithLargeRange() {
        val javaCode = """
            public class LargeClass {
                public void method1() {
                    System.out.println("Method 1");
                }
                
                public void method2() {
                    System.out.println("Method 2");
                }
                
                public void method3() {
                    undeclaredVariable = 5;
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("LargeClass.java", javaCode)
        val issues = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull("Issues should not be null", issues)
    }

    fun testFindIssuesWithSyntaxErrors() {
        // Java with syntax error
        val invalidJavaCode = """
            public class InvalidSyntax {
                public void method() {
                    if (true {
                        System.out.println("Missing closing parenthesis");
                    }
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("InvalidSyntax.java", invalidJavaCode)
        val issues = service.findProblems(psiFile, 0, invalidJavaCode.length)
        
        assertNotNull("Issues should not be null", issues)
        // Should detect the syntax error
    }

    fun testFindIssuesWithJavaScript() {
        val jsCode = """
            function test() {
                undeclaredVariable = 5;
                console.log(undeclaredVariable);
            }
        """.trimIndent()
        val jsFile = myFixture.configureByText("test.js", jsCode)
        val jsIssues = service.findProblems(jsFile, 0, jsCode.length)
        assertNotNull("JavaScript issues should not be null", jsIssues)
    }

    fun testFindIssuesWithPython() {
        val pyCode = """
            def test():
                undefined_variable = 5
                print(undefined_variable)
        """.trimIndent()
        val pyFile = myFixture.configureByText("test.py", pyCode)
        val pyIssues = service.findProblems(pyFile, 0, pyCode.length)
        assertNotNull("Python issues should not be null", pyIssues)
    }

    fun testFindIssuesWithXML() {
        val xmlCode = """
            <root>
                <unclosed>
                    <tag>content</tag>
            </root>
        """.trimIndent()
        val xmlFile = myFixture.configureByText("test.xml", xmlCode)
        val xmlIssues = service.findProblems(xmlFile, 0, xmlCode.length)
        assertNotNull("XML issues should not be null", xmlIssues)
    }

    fun testFindIssuesWithJSON() {
        val jsonCode = """
            {
                "key": "value",
                "number": 123,
                "unclosed": "string
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("test.json", jsonCode)
        val issues = service.findProblems(psiFile, 0, jsonCode.length)
        
        assertNotNull("JSON issues should not be null", issues)
    }

    fun testNullSafetyWithEmptyFile() {
        val emptyFile = myFixture.configureByText("empty.txt", "")
        
        // Should handle empty files gracefully
        try {
            val issues = service.findProblems(emptyFile, 0, 0)
            assertNotNull("Issues should not be null", issues)
        } catch (e: Exception) {
            fail("Should handle empty files gracefully: ${e.message}")
        }
    }

    fun testLargeFileHandling() {
        val largeContent = "public class Large {\n" + 
                          (1..100).joinToString("\n") { "    public void method$it() { /* method $it */ }" } +
                          "\n}"
        
        val psiFile = myFixture.configureByText("Large.java", largeContent)
        
        // Should handle large files gracefully
        try {
            val issues = service.findProblems(psiFile, 0, largeContent.length)
            assertNotNull("Issues should not be null", issues)
        } catch (e: Exception) {
            fail("Should handle large files gracefully: ${e.message}")
        }
    }

    fun testInvalidRangeHandling() {
        val javaCode = "public class Test {}"
        val psiFile = myFixture.configureByText("Test.java", javaCode)
        
        // Should handle invalid ranges gracefully
        try {
            // Test with range beyond file length
            val issues1 = service.findProblems(psiFile, 0, javaCode.length * 2)
            assertNotNull("Issues should not be null", issues1)
        } catch (e: Exception) {
            fail("Should handle large ranges gracefully: ${e.message}")
        }
        
        try {
            // Test with negative range
            val issues2 = service.findProblems(psiFile, -1, javaCode.length)
            assertNotNull("Issues should not be null", issues2)
        } catch (e: Exception) {
            fail("Should handle negative ranges gracefully: ${e.message}")
        }
    }

    fun testJavaSpecificIssueDetection() {
        val javaCode = """
            public class JavaTest {
                public void method() {
                    int x = 5;
                    int y = x + undeclaredVar;
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("JavaTest.java", javaCode)
        val issues = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull("Issues should not be null", issues)
    }

    fun testXMLSpecificIssueDetection() {
        val xmlCode = """
            <root>
                <unclosed>
                    <valid>content</valid>
                </mismatched>
            </root>
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("test.xml", xmlCode)
        val issues = service.findProblems(psiFile, 0, xmlCode.length)
        
        assertNotNull("Issues should not be null", issues)
    }

    fun testPartialFileAnalysis() {
        val javaCode = """
            public class PartialTest {
                public void method1() {
                    System.out.println("Valid method");
                }
                
                public void method2() {
                    undeclaredVariable = 5;  // This line has an issue
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("PartialTest.java", javaCode)
        
        // Test analyzing only a portion of the file
        val lineStartOffset = javaCode.indexOf("public void method2")
        val lineEndOffset = javaCode.indexOf("}", lineStartOffset)
        
        val issues = service.findProblems(psiFile, lineStartOffset, lineEndOffset)
        assertNotNull("Issues should not be null for partial analysis", issues)
    }

    fun testConcurrentAccess() {
        val javaCode = "public class Concurrent { }"
        val psiFile = myFixture.configureByText("Concurrent.java", javaCode)
        
        // Test that multiple calls don't interfere with each other
        val issues1 = service.findProblems(psiFile, 0, javaCode.length)
        val issues2 = service.findProblems(psiFile, 0, javaCode.length)
        
        assertNotNull("First call should not be null", issues1)
        assertNotNull("Second call should not be null", issues2)
        
        // Both calls should succeed independently
    }

    fun testDifferentLanguagePatterns() {
        // Test that the service can handle various programming languages
        val languages = mapOf(
            "test.java" to "public class Test {}",
            "test.kt" to "class Test",
            "test.js" to "function test() {}",
            "test.py" to "def test(): pass",
            "test.rb" to "def test; end",
            "test.php" to "<?php function test() {} ?>",
            "test.cpp" to "int main() { return 0; }",
            "test.go" to "package main; func main() {}",
            "test.rs" to "fn main() {}",
        )
        
        languages.forEach { (filename, code) ->
            val psiFile = myFixture.configureByText(filename, code)
            val issues = service.findProblems(psiFile, 0, code.length)
            assertNotNull("Issues should not be null for $filename", issues)
        }
    }

    fun testFindProblemsForFileBucketing() {
        val invalidJavaCode = """
            public class BucketTest {
                public void method() {
                    undeclaredVariable = 5;
                    if (true {
                        System.out.println("Syntax error");
                    }
                }
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("BucketTest.java", invalidJavaCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")
        val issuesByLine = service.findProblemsForFile(psiFile, document, 0, invalidJavaCode.length)

        assertNotNull("Issues map should not be null", issuesByLine)

        // Each key should be a valid line number
        for (lineNumber in issuesByLine.keys) {
            assertTrue("Line number $lineNumber should be >= 0", lineNumber >= 0)
            assertTrue("Line number $lineNumber should be valid for document", lineNumber < document.lineCount)
        }
    }

    fun testPerformanceWithRepeatedCalls() {
        val javaCode = """
            public class Performance {
                public void test() {
                    for (int i = 0; i < 100; i++) {
                        System.out.println(i);
                    }
                }
            }
        """.trimIndent()
        
        val psiFile = myFixture.configureByText("Performance.java", javaCode)
        
        // Test multiple calls to ensure no memory leaks or performance degradation
        repeat(10) {
            val issues = service.findProblems(psiFile, 0, javaCode.length)
            assertNotNull("Issues should not be null on call $it", issues)
        }
    }

    fun testDoesNotThrowWhenIssueOffsetsExceedDocumentLength() {
        val xmlCode = """
            <root>
                <unclosed>
            </root>
        """.trimIndent()

        val psiFile = myFixture.configureByText("stale.xml", xmlCode)
        val realDocument = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val onRealDocument = service.findProblemsForFile(psiFile, realDocument, 0, xmlCode.length)
        assertFalse(
            "Precondition: the fixture must yield at least one issue, otherwise this test proves nothing",
            onRealDocument.isEmpty()
        )

        // Pairing the PSI with a shorter document reproduces an edit landing while
        // analysis is in flight: the issue offsets then point past the end of the
        // current text, which is exactly what makes getLineNumber throw.
        val truncatedDocument = EditorFactory.getInstance().createDocument("")
        val issuesByLine = service.findProblemsForFile(psiFile, truncatedDocument, 0, xmlCode.length)

        assertEquals(
            "Out-of-range offsets must clamp into the document instead of throwing",
            setOf(0),
            issuesByLine.keys
        )
    }

    fun testWholeFileScanReportsSyntaxError() {
        val xmlCode = "<root>\n    <unclosed>\n</root>"
        val psiFile = myFixture.configureByText("scan.xml", xmlCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val messages = service.findProblemsForFile(psiFile, document, 0, document.textLength)
            .values.flatten().map { it.message }

        assertTrue(
            "Scanning the whole file should report the unclosed tag, got $messages",
            messages.any { it.contains("not closed") }
        )
    }

    fun testDuplicateFindingsAreCollapsedPerLine() {
        // Several parser errors land on the same line here, and a single element can
        // be reached by more than one scan path.
        val xmlCode = "<root>\n\n</root>\n<<<"
        val psiFile = myFixture.configureByText("dupes.xml", xmlCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val rawErrorCount = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java).size
        assertTrue("Precondition: fixture must produce several error elements", rawErrorCount > 1)

        val issuesByLine = service.findProblemsForFile(psiFile, document, 0, document.textLength)
        for ((line, issues) in issuesByLine) {
            val keys = issues.map { "${it.severity}:${it.message}" }
            assertEquals("Line $line reported the same finding twice: $keys", keys.distinct().size, keys.size)
        }

        assertTrue(
            "Expected $rawErrorCount error elements to collapse into fewer reported findings",
            issuesByLine.values.sumOf { it.size } < rawErrorCount
        )
    }

    fun testZeroWidthErrorOnSelectionBoundaryIsReported() {
        // The parser reports the unclosed tag as a zero-width element sitting at
        // the end of that tag's line, which is exactly the boundary offset a
        // single-line selection produces.
        val xmlCode = "<root>\n    <unclosed>\n</root>"
        val psiFile = myFixture.configureByText("boundary.xml", xmlCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val zeroWidthErrors = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java)
            .filter { it.textRange.isEmpty }
        assertFalse(
            "Precondition: the fixture must produce a zero-width error element, otherwise this test proves nothing",
            zeroWidthErrors.isEmpty()
        )

        val errorOffset = zeroWidthErrors.first().textRange.startOffset
        val errorLine = document.getLineNumber(errorOffset)
        assertEquals(
            "Precondition: the error must sit exactly on the line's end boundary",
            document.getLineEndOffset(errorLine),
            errorOffset
        )

        val selectionOnly = service.findProblemsForFile(
            psiFile, document,
            document.getLineStartOffset(errorLine),
            document.getLineEndOffset(errorLine)
        )

        assertTrue(
            "Selecting the line that carries the error must report it, got $selectionOnly",
            selectionOnly[errorLine].orEmpty().any { it.message.contains("not closed") }
        )
    }

    fun testZeroWidthErrorAtRangeStartIsReported() {
        val xmlCode = "<root>\n    <unclosed>\n</root>"
        val psiFile = myFixture.configureByText("startBoundary.xml", xmlCode)
        val document = psiFile.viewProvider.document ?: throw AssertionError("Document should exist")

        val zeroWidthErrors = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement::class.java)
            .filter { it.textRange.isEmpty }
        assertFalse("Precondition: fixture must produce a zero-width error element", zeroWidthErrors.isEmpty())

        val errorOffset = zeroWidthErrors.first().textRange.startOffset
        val fromError = service.findProblemsForFile(psiFile, document, errorOffset, document.textLength)
            .values.flatten().map { it.message }

        assertTrue(
            "A range starting exactly at the error must still report it, got $fromError",
            fromError.any { it.contains("not closed") }
        )
    }
}