package io.appmetrica.analytics.impl.modules

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ModulesSeekerTest : CommonTest() {

    @get:Rule
    val reflectionUtils = MockedStaticRule(ReflectionUtils::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val firstServiceModuleClass = "io.appmetrica.analytics.FirstServiceModuleClass"
    private val secondServiceModuleClass = "io.appmetrica.analytics.SecondServiceModulesClass"
    private val missingServiceModuleClass = "io.appmetrica.analytics.MissingServiceModuleClass"
    private val anotherServiceModuleClass = "io.appmetrica.analytics.AnotherServiceModuleClass"

    private val firstClientModuleClass = "io.appmetrica.analytics.FirstClientModuleClass"
    private val secondClientModuleClass = "io.appmetrica.analytics.SecondClientModulesClass"
    private val missingClientModuleClass = "io.appmetrica.analytics.MissingClientModuleClass"
    private val anotherClientModuleClass = "io.appmetrica.analytics.AnotherClientModuleClass"

    private val firstServiceModuleEntryPoint = mock<ModuleServiceEntryPoint<Any>>()
    private val secondServiceModuleEntryPoint = mock<ModuleServiceEntryPoint<Any>>()
    private val anotherServiceModuleEntryPoint = mock<ModuleServiceEntryPoint<Any>>()

    private val firstClientModuleEntryPoint = mock<ModuleClientEntryPoint<Any>>()
    private val secondClientModuleEntryPoint = mock<ModuleClientEntryPoint<Any>>()
    private val anotherClientModuleEntryPoint = mock<ModuleClientEntryPoint<Any>>()

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
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                anotherServiceModuleClass,
                ModuleServiceEntryPoint::class.java
            )
        ).thenReturn(anotherServiceModuleEntryPoint)

        whenever(serviceModulesEntryPointsRegister.classNames)
            .thenReturn(
                listOf(
                    firstServiceModuleClass,
                    secondServiceModuleClass,
                    missingServiceModuleClass,
                    anotherServiceModuleClass,
                )
            )

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
        whenever(
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                anotherClientModuleClass,
                ModuleClientEntryPoint::class.java
            )
        ).thenReturn(anotherClientModuleEntryPoint)
        whenever(clientModulesEntryPointsRegister.classNames)
            .thenReturn(
                listOf(
                    firstClientModuleClass,
                    secondClientModuleClass,
                    missingClientModuleClass,
                    anotherClientModuleClass,
                )
            )
    }

    @Test
    fun discoverServiceModules() {
        assertThat(modulesSeeker.discoverServiceModules()).containsExactly(
            ModuleStatus(firstServiceModuleClass, true),
            ModuleStatus(secondServiceModuleClass, true),
            ModuleStatus(missingServiceModuleClass, false),
            ModuleStatus(anotherServiceModuleClass, true),
        )
        inOrder(serviceModulesController).also {
            it.verify(serviceModulesController).registerModule(firstServiceModuleEntryPoint)
            it.verify(serviceModulesController).registerModule(secondServiceModuleEntryPoint)
            it.verify(serviceModulesController).registerModule(anotherServiceModuleEntryPoint)
        }
        verifyNoMoreInteractions(serviceModulesController)
    }

    @Test
    fun discoverServiceModulesForEmptyList() {
        whenever(serviceModulesEntryPointsRegister.classNames).thenReturn(emptyList())
        assertThat(modulesSeeker.discoverServiceModules()).isEmpty()
        verifyNoMoreInteractions(serviceModulesController)
    }

    @Test
    fun discoverClientModules() {
        assertThat(modulesSeeker.discoverClientModules()).containsExactly(
            ModuleStatus(firstClientModuleClass, true),
            ModuleStatus(secondClientModuleClass, true),
            ModuleStatus(missingClientModuleClass, false),
            ModuleStatus(anotherClientModuleClass, true),
        )
        inOrder(clientModulesController).also {
            it.verify(clientModulesController).registerModule(firstClientModuleEntryPoint)
            it.verify(clientModulesController).registerModule(secondClientModuleEntryPoint)
            it.verify(clientModulesController).registerModule(anotherClientModuleEntryPoint)
        }
    }

    @Test
    fun discoverClientModulesForEmptyList() {
        whenever(clientModulesEntryPointsRegister.classNames).thenReturn(emptyList())
        assertThat(modulesSeeker.discoverClientModules()).isEmpty()
        verifyNoMoreInteractions(clientModulesController)
    }
}
