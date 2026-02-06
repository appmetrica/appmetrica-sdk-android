package io.appmetrica.analytics.testutils

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

/* ktlint-disable appmetrica-rules:no-top-level-members */
fun mockFile(absPath: String) = mock<File> {
    on { absolutePath } doReturn absPath
}
/* ktlint-enable appmetrica-rules:no-top-level-members */
