package io.appmetrica.analytics.testutils

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

fun mockFile(absPath: String) = mock<File> {
    on { absolutePath } doReturn absPath
}
