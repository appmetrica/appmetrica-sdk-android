package io.appmetrica.analytics.impl.component

import android.content.Context
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ComponentMigrationHelperTest : CommonTest() {

    private var context: Context = mock()

    private val componentId: ComponentId = mock {
        on { `package` } doReturn "test_package"
    }

    private val vitalComponentDataProvider: VitalComponentDataProvider = mock()

    private val componentUnit: ComponentUnit = mock {
        on { context } doReturn context
        on { componentId } doReturn componentId
        on { vitalComponentDataProvider } doReturn vitalComponentDataProvider
    }

    @get:Rule
    val v113MigrationMockedConstructionRule = constructionRule<ComponentMigrationToV113>()
    private val v113Migration by v113MigrationMockedConstructionRule

    private val mMigrationHelper: ComponentMigrationHelper by setUp {
        ComponentMigrationHelper.Creator().create(componentUnit)
    }

    @Test
    fun migrate() {
        val from = 112
        mMigrationHelper.migrate(from)
        verify(v113Migration).run(from)
    }

    @Test
    fun migrationScripts() {
        assertThat(getClasses(mMigrationHelper.getMigrationScripts())).containsExactly(
            ComponentMigrationToV113::class.java
        )
    }

    private fun getClasses(scripts: List<ComponentMigrationScript>): List<Class<*>> {
        val result: MutableList<Class<*>> = ArrayList()
        for (script in scripts) {
            result.add(script.javaClass)
        }
        return result
    }
}
