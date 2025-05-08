package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

class TestInheritanceRule : Rule("test-inheritance") {

    private val testAnnotations = setOf("Test", "ParameterizedTest", "RepeatedTest")
    private val commonTestClass = "CommonTest"

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val ktClass = node.psi as? KtClass ?: return

        val hasTestMethods = ktClass.body?.functions?.any { function ->
            function.annotationEntries.any { annotation ->
                testAnnotations.any { testAnn ->
                    annotation.text.startsWith("@$testAnn")
                }
            }
        } ?: false

        if (!hasTestMethods) return

        val hasCommonTestParent = ktClass.superTypeListEntries.any { superEntry ->
            superEntry.text.contains(commonTestClass) ||
                superEntry.text.contains("(") // extends from another class
        }

        if (!hasCommonTestParent) {
            emit(
                node.startOffset,
                "Test class ${ktClass.name} should extend $commonTestClass",
                false
            )
        }
    }
}
