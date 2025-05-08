package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class NoMockitoAnnotationsRule : Rule("no-mockito-annotations") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType != ElementType.CALL_EXPRESSION) {
            return
        }

        val callExpression = node.psi as KtCallExpression
        val calleeExpression = callExpression.calleeExpression as? KtNameReferenceExpression
        val parent = calleeExpression?.parent as? KtDotQualifiedExpression

        val methodName = calleeExpression?.getReferencedName()
        val receiverName = parent?.receiverExpression?.text
        val calleeName = callExpression.calleeExpression?.text

        if (methodName == "openMocks" && receiverName == "MockitoAnnotations" || calleeName == "openMocks") {
            emit(
                node.startOffset,
                "Using 'MockitoAnnotations.openMocks' is forbidden. Use 'mock' instead",
                false
            )
        }
    }
}
