package io.appmetrica.analytics.assertions

import io.appmetrica.analytics.assertions.matcher.field.ClassField
import io.appmetrica.analytics.assertions.matcher.field.toClassField
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

object Utils {

    fun <T : Any> getFieldsFromClass(
        actual: T,
        includingParents: Boolean,
        declaredAccessibleFields: Boolean,
        privateFields: Boolean
    ): List<ClassField> {
        val allClassesFields = getFieldsFromClass(actual::class.java, declaredAccessibleFields, privateFields).toMutableList()
        if (includingParents) {
            var c: Class<*>? = actual::class.java.superclass
            while (c != null && c != Any::class.java) {
                allClassesFields.addAll(getFieldsFromClass(c, declaredAccessibleFields, privateFields))
                c = c.superclass
            }
        }
        return allClassesFields
    }

    private fun getFieldsFromClass(
        clazz: Class<*>,
        declaredAccessibleFields: Boolean,
        privateFields: Boolean
    ): List<ClassField> {
        val thisClassFields: Array<Field>
        if (declaredAccessibleFields) {
            thisClassFields = clazz.declaredFields
        } else if (privateFields) {
            thisClassFields = clazz.declaredFields
            for (field in thisClassFields) {
                field.isAccessible = true
            }
        } else {
            thisClassFields = clazz.fields
        }
        return thisClassFields.map { it.toClassField() }
    }

    fun <T : Any> getFieldsFromKotlinClass(
        actual: T,
        includingParents: Boolean,
        declaredAccessibleFields: Boolean,
        privateFields: Boolean
    ): List<ClassField> {
        return getFieldsFromKotlinClass(actual::class, includingParents, declaredAccessibleFields, privateFields)
    }

    private fun getFieldsFromKotlinClass(
        clazz: KClass<*>,
        includingParents: Boolean,
        declaredAccessibleFields: Boolean,
        privateFields: Boolean
    ): List<ClassField> {
        val thisClassFields = mutableListOf<ClassField>()
        if (includingParents) {
            clazz.superclasses.forEach {
                thisClassFields.addAll(getFieldsFromKotlinClass(it, includingParents, declaredAccessibleFields, privateFields))
            }
        }
        if (declaredAccessibleFields) {
            thisClassFields.addAll(clazz.declaredMemberProperties.map { it.toClassField() })
        } else if (privateFields) {
            thisClassFields.addAll(clazz.declaredMemberProperties.map {
                it.getter.isAccessible = true
                it.toClassField()
            })
        } else {
            thisClassFields.addAll(clazz.memberProperties.map { it.toClassField() })
        }
        return thisClassFields
    }
}
