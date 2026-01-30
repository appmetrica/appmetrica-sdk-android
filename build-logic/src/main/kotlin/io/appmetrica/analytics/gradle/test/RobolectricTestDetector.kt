package io.appmetrica.analytics.gradle.test

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream

/**
 * Detects if a test class uses Robolectric by analyzing bytecode with ASM.
 */
class RobolectricTestDetector {

    /**
     * Scans a directory recursively to find all Robolectric test classes.
     *
     * @param directory The directory containing compiled test classes
     * @return Set of fully qualified class names that use Robolectric
     */
    fun findRobolectricTests(directory: File): Set<String> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptySet()
        }

        val robolectricTests = directory.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".class") }
            .filter { isRobolectricTest(it) }
            .map { classFile ->
                classFile.relativeTo(directory).path
                    .removeSuffix(".class")
                    .replace(File.separatorChar, '.')
            }.toSet()

        return robolectricTests
    }

    /**
     * Checks if a compiled test class uses RobolectricTestRunner or ParameterizedRobolectricTestRunner.
     *
     * @param classFile The .class file to analyze
     * @return true if the class uses Robolectric, false otherwise
     */
    private fun isRobolectricTest(classFile: File): Boolean {
        if (!classFile.exists() || !classFile.name.endsWith(".class")) {
            return false
        }

        return try {
            classFile.inputStream().use { inputStream ->
                isRobolectricTest(inputStream)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a test class uses Robolectric by analyzing the bytecode from an InputStream.
     */
    private fun isRobolectricTest(inputStream: InputStream): Boolean {
        val classReader = ClassReader(inputStream)
        val detector = RobolectricAnnotationDetector()
        classReader.accept(detector, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        return detector.isRobolectric
    }

    private class RobolectricAnnotationDetector : ClassVisitor(Opcodes.ASM9) {
        var isRobolectric = false
            private set

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
            if (descriptor == "Lorg/junit/runner/RunWith;") {
                return object : AnnotationVisitor(Opcodes.ASM9) {
                    override fun visit(name: String?, value: Any?) {
                        val valueStr = value.toString()
                        if (valueStr.contains("RobolectricTestRunner") ||
                            valueStr.contains("ParameterizedRobolectricTestRunner")
                        ) {
                            isRobolectric = true
                        }
                    }
                }
            }
            return null
        }
    }
}
