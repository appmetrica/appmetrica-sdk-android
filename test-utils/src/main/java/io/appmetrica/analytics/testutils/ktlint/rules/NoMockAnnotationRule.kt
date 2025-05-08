package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile

class NoMockAnnotationRule : Rule("no-mock-annotation") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType != ElementType.ANNOTATION_ENTRY) {
            return
        }

        val annotationEntry = node.psi as KtAnnotationEntry
        val annotationText = annotationEntry.text ?: return
        val ktFile = annotationEntry.containingFile as? KtFile ?: return
        val isMockAnnotation = isMockitoMockAnnotation(annotationText, ktFile)
        if (isMockAnnotation) {
            emit(
                node.startOffset,
                "Using '@Mock' is forbidden. Use 'mock()' instead",
                false
            )
        }
    }

    private fun isMockitoMockAnnotation(annotationText: String, ktFile: KtFile): Boolean {
        if (annotationText.startsWith("@org.mockito.Mock")) {
            return true
        }

        if (annotationText.startsWith("@Mock") && hasMockImport(ktFile)) {
            return true
        }

        return false
    }

    private fun hasMockImport(ktFile: KtFile): Boolean {
        return ktFile.importDirectives.any { import ->
            import.importPath?.pathStr == "org.mockito.Mock"
        }
    }
}
