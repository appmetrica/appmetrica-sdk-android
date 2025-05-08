package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction

class NoTestPrefixRule : Rule("no-test-prefix") {

    private val testAnnotations = setOf("Test", "ParameterizedTest", "RepeatedTest")

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val function = node.psi as? KtNamedFunction
        val functionName = function?.name ?: return

        val isTestMethod = function.annotationEntries.any { annotation ->
            testAnnotations.any { testAnn ->
                annotation.text.startsWith("@$testAnn")
            }
        }

        if (isTestMethod && functionName.startsWith("test", ignoreCase = true)) {
            emit(
                node.startOffset,
                "Using prefix 'test' for tests is forbidden. Use descriptive names",
                false
            )
        }
    }
}
