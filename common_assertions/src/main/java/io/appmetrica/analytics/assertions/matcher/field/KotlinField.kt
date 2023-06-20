package io.appmetrica.analytics.assertions.matcher.field

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class KotlinField(
    val field: KProperty<*>
) : ClassField {

    override val name = field.name

    override fun isStatic() = false

    override fun isFinal() = field !is KMutableProperty<*>
}

fun KProperty<*>.toClassField(): ClassField = KotlinField(this)
