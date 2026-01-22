package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class DummyLocationInfoComposerTest : CommonTest() {

    private val locationInfo: DbLocationModel = mock()
    private var composer = DummyLocationInfoComposer()

    @Test
    fun getLocation() {
        assertThat(composer.getLocation(locationInfo)).isNull()
    }

    @Test
    fun getLocationForNull() {
        assertThat(composer.getLocation(null)).isNull()
    }
}
