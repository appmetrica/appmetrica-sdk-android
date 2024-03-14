package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.modulesapi.internal.ModuleEntryPoint
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModuleSeekerTest : CommonTest() {

    @get:Rule
    val moduleLoaderMockedRule = MockedConstructionRule(ModuleLoader::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val firstModuleClass = "io.appmetrica.analytics.FirstModuleClass"
    private val secondModuleClass = "io.appmetrica.analytics.SecondModulesClass"
    private val missingModuleClass = "io.appmetrica.analytics.MissingModuleClass"

    private val firstModuleEntryPoint = mock<ModuleEntryPoint<Any>>()
    private val secondModuleEntryPoint = mock<ModuleEntryPoint<Any>>()

    private lateinit var modulesController: ModulesController
    private lateinit var moduleLoader: ModuleLoader
    private lateinit var modulesEntryPointsRegister: ModuleEntryPointsRegister
    private lateinit var modulesSeeker: ModulesSeeker

    @Before
    fun setUp() {
        modulesEntryPointsRegister = GlobalServiceLocator.getInstance().moduleEntryPointsRegister
        modulesController = GlobalServiceLocator.getInstance().modulesController
        modulesSeeker = ModulesSeeker()
        assertThat(moduleLoaderMockedRule.constructionMock.constructed()).hasSize(1)
        moduleLoader = moduleLoaderMockedRule.constructionMock.constructed()[0]
        stubbing(moduleLoader) {
            on { loadModule(firstModuleClass) } doReturn firstModuleEntryPoint
            on { loadModule(secondModuleClass) } doReturn secondModuleEntryPoint
            on { loadModule(missingModuleClass) } doReturn null
        }
        whenever(modulesEntryPointsRegister.classNames)
            .thenReturn(hashSetOf(firstModuleClass, secondModuleClass, missingModuleClass))
    }

    @Test
    fun discoverModules() {
        modulesSeeker.discoverModules()
        verify(modulesController).registerModule(firstModuleEntryPoint)
        verify(modulesController).registerModule(secondModuleEntryPoint)
        verifyNoMoreInteractions(modulesController)
    }

    @Test
    fun discoverModulesForEmptyList() {
        whenever(modulesEntryPointsRegister.classNames).thenReturn(emptySet())
        modulesSeeker.discoverModules()
        verifyNoMoreInteractions(modulesController)
    }
}
