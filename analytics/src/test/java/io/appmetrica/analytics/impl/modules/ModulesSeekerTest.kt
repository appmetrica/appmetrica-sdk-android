package io.appmetrica.analytics.impl.modules

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.client.ClientModulesController
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModulesSeekerTest : CommonTest() {

    private val context: Context = mock()

    @get:Rule
    val reflectionUtils = MockedStaticRule(ReflectionUtils::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val moduleStatusReporterRule = constructionRule<ModuleStatusReporter>()
    private val moduleStatusReporter by moduleStatusReporterRule

    private val modulesStatusCaptor = argumentCaptor<List<ModuleStatus>>()

    private val firstServiceModuleClass = "io.appmetrica.analytics.FirstModuleClass"
    private val secondServiceModuleClass = "io.appmetrica.analytics.SecondModulesClass"
    private val missingServiceModuleClass = "io.appmetrica.analytics.MissingModuleClass"

    private val firstClientModuleClass = "io.appmetrica.analytics.FirstModuleClass"
    private val secondClientModuleClass = "io.appmetrica.analytics.SecondModulesClass"
    private val missingClientModuleClass = "io.appmetrica.analytics.MissingModuleClass"

    private val firstServiceModuleEntryPoint = mock<ModuleServiceEntryPoint<Any>>()
    private val secondServiceModuleEntryPoint = mock<ModuleServiceEntryPoint<Any>>()

    private val firstClientModuleEntryPoint = mock<ModuleClientEntryPoint<Any>>()
    private val secondClientModuleEntryPoint = mock<ModuleClientEntryPoint<Any>>()

    private lateinit var serviceModulesController: ServiceModulesController
    private lateinit var serviceModulesEntryPointsRegister: ModuleEntryPointsRegister

    private lateinit var clientModulesController: ClientModulesController
    private lateinit var clientModulesEntryPointsRegister: ModuleEntryPointsRegister

    private lateinit var modulesSeeker: ModulesSeeker

    @Before
    fun setUp() {
        serviceModulesController = GlobalServiceLocator.getInstance().modulesController
        serviceModulesEntryPointsRegister = GlobalServiceLocator.getInstance().moduleEntryPointsRegister

        clientModulesController = ClientServiceLocator.getInstance().modulesController
        clientModulesEntryPointsRegister = ClientServiceLocator.getInstance().moduleEntryPointsRegister

        modulesSeeker = ModulesSeeker()

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                firstServiceModuleClass,
                ModuleServiceEntryPoint::class.java
            )
        ).thenReturn(firstServiceModuleEntryPoint)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                secondServiceModuleClass,
                ModuleServiceEntryPoint::class.java
            )
        ).thenReturn(secondServiceModuleEntryPoint)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                missingServiceModuleClass,
                ModuleServiceEntryPoint::class.java
            )
        ).thenReturn(null)
        whenever(serviceModulesEntryPointsRegister.classNames)
            .thenReturn(hashSetOf(firstServiceModuleClass, secondServiceModuleClass, missingServiceModuleClass))

        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                firstClientModuleClass,
                ModuleClientEntryPoint::class.java
            )
        ).thenReturn(firstClientModuleEntryPoint)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                secondClientModuleClass,
                ModuleClientEntryPoint::class.java
            )
        ).thenReturn(secondClientModuleEntryPoint)
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                missingClientModuleClass,
                ModuleClientEntryPoint::class.java
            )
        ).thenReturn(null)
        whenever(clientModulesEntryPointsRegister.classNames)
            .thenReturn(hashSetOf(firstClientModuleClass, secondClientModuleClass, missingClientModuleClass))
    }

    @Test
    fun discoverServiceModules() {
        modulesSeeker.discoverServiceModules()
        verify(serviceModulesController).registerModule(firstServiceModuleEntryPoint)
        verify(serviceModulesController).registerModule(secondServiceModuleEntryPoint)
        verifyNoMoreInteractions(serviceModulesController)
        verify(moduleStatusReporter).reportModulesStatus(modulesStatusCaptor.capture())
        assertThat(modulesStatusCaptor.firstValue).containsExactly(
            ModuleStatus(firstServiceModuleClass, true),
            ModuleStatus(missingServiceModuleClass, false),
            ModuleStatus(secondServiceModuleClass, true),
        )
    }

    @Test
    fun discoverServiceModulesForEmptyList() {
        whenever(serviceModulesEntryPointsRegister.classNames).thenReturn(emptySet())
        modulesSeeker.discoverServiceModules()
        verifyNoMoreInteractions(serviceModulesController)
        verify(moduleStatusReporter).reportModulesStatus(modulesStatusCaptor.capture())
        assertThat(modulesStatusCaptor.firstValue).isEmpty()
    }

    @Test
    fun discoverClientModules() {
        modulesSeeker.discoverClientModules(context)
        verify(clientModulesController).registerModule(firstClientModuleEntryPoint)
        verify(clientModulesController).registerModule(secondClientModuleEntryPoint)
        verifyNoMoreInteractions(clientModulesController)
        verify(moduleStatusReporter).reportModulesStatus(modulesStatusCaptor.capture())
        assertThat(modulesStatusCaptor.firstValue).containsExactly(
            ModuleStatus(firstServiceModuleClass, true),
            ModuleStatus(missingServiceModuleClass, false),
            ModuleStatus(secondServiceModuleClass, true),
        )
    }

    @Test
    fun discoverClientModulesForEmptyList() {
        whenever(clientModulesEntryPointsRegister.classNames).thenReturn(emptySet())
        modulesSeeker.discoverClientModules(context)
        verifyNoMoreInteractions(clientModulesController)
        verify(moduleStatusReporter).reportModulesStatus(modulesStatusCaptor.capture())
        assertThat(modulesStatusCaptor.firstValue).isEmpty()
    }
}
