package io.appmetrica.analytics.coreutils.internal.reflection

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReflectionUtilsTest {

    private interface TestInterface

    internal class TestClassWithDefaultConstructor : TestInterface

    internal class TestClassWithoutDefaultConstructor private constructor() : TestInterface

    internal class TestClassNotImplementTestInterface

    @Test
    fun loadAndInstantiateClass() {
        assertThat(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            TestClassWithDefaultConstructor::class.java.name,
            TestInterface::class.java
        )).isNotNull()
    }

    @Test
    fun loadAndInstantiateClassWithoutDefaultConstructor() {
        assertThat(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            TestClassWithoutDefaultConstructor::class.java.name,
            TestInterface::class.java
        )).isNull()
    }

    @Test
    fun loadAndInstantiateClassWithoutInterfaceImplementation() {
        assertThat(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            TestClassNotImplementTestInterface::class.java.name,
            TestInterface::class.java
        )).isNull()
    }

    @Test
    fun loadClassIfExists() {
        assertThat(ReflectionUtils.loadClass(
            TestClassWithDefaultConstructor::class.java.name,
            TestClassWithDefaultConstructor::class.java
        )).isNotNull()
    }

    @Test
    fun loadClassIfNotExist() {
        assertThat(ReflectionUtils.loadClass(
            "${TestClassWithDefaultConstructor::class.java.name}1",
            TestClassWithDefaultConstructor::class.java
        )).isNull()
    }

    @Test
    fun loadClassIfWrongType() {
        assertThat(ReflectionUtils.loadClass(
            TestClassNotImplementTestInterface::class.java.name, TestInterface::class.java
        )).isNull()
    }

    @Test
    fun isArgumentsOfClasses() {
        assertThat((ReflectionUtils.isArgumentsOfClasses(
            arrayOf(
                "some String",
                object : TestInterface {}
            ),
            String::class.java,
            TestInterface::class.java
        ))).isTrue()
    }

    @Test
    fun isArgumentsOfClassesIfWrongNumberOfArguments() {
        assertThat((ReflectionUtils.isArgumentsOfClasses(
            arrayOf(
                "some String",
                object : TestInterface {}
            ),
            String::class.java,
            TestInterface::class.java,
            String::class.java
        ))).isFalse()
    }

    @Test
    fun isArgumentsOfClassesIfWrongTypesOfArguments() {
        assertThat((ReflectionUtils.isArgumentsOfClasses(
            arrayOf(
                "some String",
                object : TestInterface {}
            ),
            String::class.java,
            String::class.java
        ))).isFalse()
    }

    @Test
    fun isArgumentsOfClassesIfArgumentsAreNull() {
        val nullString: String? = null
        val nullClass: TestInterface? = null
        assertThat((ReflectionUtils.isArgumentsOfClasses(
            arrayOf(
                nullString,
                nullClass
            ),
            String::class.java,
            TestInterface::class.java
        ))).isFalse()
    }

    @Test
    fun isArgumentsOfClassesIfArgumentsAreNullWithWrongType() {
        val nullString: String? = null
        val nullClass: TestInterface? = null
        assertThat((ReflectionUtils.isArgumentsOfClasses(
            arrayOf(
                nullString,
                nullClass
            ),
            String::class.java,
            String::class.java
        ))).isFalse()
    }
}
