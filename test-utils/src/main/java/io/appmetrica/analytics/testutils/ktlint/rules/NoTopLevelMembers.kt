package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

/**
 * Allows only private top-level properties and functions
 */
class NoTopLevelMembers : Rule("no-top-level-members") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        when (node.elementType) {
            ElementType.PROPERTY -> checkProperty(node, emit)
            ElementType.FUN -> checkFunction(node, emit)
        }
    }

    private fun checkProperty(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val property = node.psi as? KtProperty ?: return
        val isTopLevel = property.parent is KtFile

        if (!isTopLevel) {
            return
        }

        val isPrivate = property.visibilityModifierTypeOrDefault() == KtTokens.PRIVATE_KEYWORD
        val isVal = !property.isVar

        // Allow all private val (const and non-const)
        if (isPrivate && isVal) {
            return
        }

        // Build error message based on what's wrong
        val errorMessage = when {
            property.isVar ->
                "Top-level 'var' is forbidden. Use 'private val' or move to class"

            !isPrivate ->
                "Public/internal top-level properties forbidden. Make it private"

            else ->
                "Top-level properties should be private or moved to class/object"
        }

        emit(
            node.startOffset,
            errorMessage,
            false
        )
    }

    private fun checkFunction(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val function = node.psi as? KtNamedFunction ?: return
        val isTopLevel = function.parent is KtFile

        if (!isTopLevel) {
            return
        }

        val isPrivate = function.visibilityModifierTypeOrDefault() == KtTokens.PRIVATE_KEYWORD

        // Allow all private functions
        if (isPrivate) {
            return
        }

        // Forbid public/internal top-level functions
        emit(
            node.startOffset,
            "Public/internal top-level functions forbidden. Make it private or move to object",
            false
        )
    }
}
