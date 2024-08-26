package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.service.LocationServiceExtension
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ModuleLoaderTest : CommonTest() {

    private val moduleLoader = ModuleLoader()

    @Test
    fun loadModule() {
        assertThat(moduleLoader.loadModule<ModuleServiceEntryPoint<Any>>(TestClass1::class.java.name))
            .isNotNull
    }

    @Test
    fun loadModuleWithoutDefaultConstructor() {
        assertThat(moduleLoader.loadModule<ModuleServiceEntryPoint<Any>>(TestClass2::class.java.name))
            .isNull()
    }

    @Test
    fun loadWrongModule() {
        assertThat(moduleLoader.loadModule<ModuleServiceEntryPoint<Any>>(TestClass3::class.java.name))
            .isNull()
    }

    @Test
    fun loadModuleIfEntryPointClassNotPresent() {
        assertThat(moduleLoader.loadModule<ModuleServiceEntryPoint<Any>>("io.appmetrica.analytics.impl.modules.TestClassNotPresent"))
            .isNull()
    }

}

class TestClass1 : ModuleServiceEntryPoint<Any>() {
    override val identifier: String
        get() = "Some string"

    override val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<Any>?
        get() = null

    override val moduleEventServiceHandlerFactory: ModuleEventServiceHandlerFactory? = null

    override fun initServiceSide(serviceContext: ServiceContext, config: ModuleRemoteConfig<Any?>) {
    }

    override val locationServiceExtension: LocationServiceExtension?
        get() = null

    override val moduleServicesDatabase: ModuleServicesDatabase?
        get() = null
}

class TestClass2 private constructor(): ModuleServiceEntryPoint<Any>() {

    constructor(arg1: Any): this()

    override val identifier: String
        get() = "some identifier"

    override val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<Any>?
        get() = null

    override val moduleEventServiceHandlerFactory: ModuleEventServiceHandlerFactory? = null

    override fun initServiceSide(serviceContext: ServiceContext, config: ModuleRemoteConfig<Any?>) {
    }

    override val locationServiceExtension: LocationServiceExtension?
        get() = null

    override val moduleServicesDatabase: ModuleServicesDatabase?
        get() = null
}

class TestClass3
