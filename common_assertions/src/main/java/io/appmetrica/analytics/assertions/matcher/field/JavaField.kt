package io.appmetrica.analytics.assertions.matcher.field

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class JavaField(
    val field: Field
) : ClassField {

    override val name: String = field.name

    override fun isStatic() = Modifier.isStatic(field.modifiers)

    override fun isFinal() = Modifier.isFinal(field.modifiers)
}

fun Field.toClassField(): ClassField = JavaField(this)
