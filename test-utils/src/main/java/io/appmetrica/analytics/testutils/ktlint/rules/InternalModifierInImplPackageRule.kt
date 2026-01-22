package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class InternalModifierInImplPackageRule : Rule("internal-modifier-in-impl-package") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType != ElementType.CLASS && node.elementType != ElementType.OBJECT_DECLARATION) {
            return
        }

        val ktFile = node.psi.containingFile as? KtFile ?: return
        val packageName = ktFile.packageFqName.asString()

        if (!isImplPackage(packageName)) {
            return
        }

        val name: String?
        val hasInternalOrMoreRestrictiveModifier: Boolean

        when (val psi = node.psi) {
            is KtClass -> {
                name = psi.name
                hasInternalOrMoreRestrictiveModifier = psi.isNotAccessibleFromOutside()
            }

            is KtObjectDeclaration -> {
                if (psi.isCompanion()) {
                    return
                }
                name = psi.name
                hasInternalOrMoreRestrictiveModifier = psi.isNotAccessibleFromOutside()
            }

            else -> return
        }

        if (name == null) {
            return
        }

        if (!hasInternalOrMoreRestrictiveModifier) {
            emit(
                node.startOffset,
                "Class '$name' in 'impl' package must have 'internal' modifier",
                false
            )
        }
    }

    private fun KtClassOrObject.isNotAccessibleFromOutside(): Boolean {
        return hasModifier(KtTokens.INTERNAL_KEYWORD) ||
            hasModifier(KtTokens.PRIVATE_KEYWORD) ||
            hasModifier(KtTokens.PROTECTED_KEYWORD)
    }

    private fun isImplPackage(packageName: String): Boolean {
        return packageName.endsWith(".impl") || packageName.contains(".impl.")
    }
}
