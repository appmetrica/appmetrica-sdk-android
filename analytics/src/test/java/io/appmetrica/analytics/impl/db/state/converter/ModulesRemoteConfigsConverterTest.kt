package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ModuleRemoteConfigController
import io.appmetrica.analytics.impl.modules.service.ServiceModulesController
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModulesRemoteConfigsConverterTest : CommonTest() {

    @get:Rule
    val globalServiceLocationRule = GlobalServiceLocatorRule()

    private val firstIdentifier = "first identifier"
    private val secondIdentifier = "second identifier"
    private val thirdIdentifier = "third identifier"

    private val firstModuleModel = mock<Any>()
    private val secondModuleModel = mock<Any>()
    private val thirdModuleModel = mock<Any>()

    private val firstModuleProto = byteArrayOfInts(1, 2, 3)
    private val secondModuleProto = byteArrayOfInts(4, 5, 6)
    private val thirdModuleProto = byteArrayOfInts(7, 8, 9)

    private val firstModuleRemoteConfigController = mock<ModuleRemoteConfigController> {
        on { toModel(firstModuleProto) } doReturn firstModuleModel
        on { fromModel(firstModuleModel) } doReturn firstModuleProto
    }
    private val secondModuleRemoteConfigController = mock<ModuleRemoteConfigController> {
        on { toModel(secondModuleProto) } doReturn secondModuleModel
        on { fromModel(secondModuleModel) } doReturn secondModuleProto
    }
    private val thirdModuleRemoteConfigController = mock<ModuleRemoteConfigController> {
        on { toModel(thirdModuleProto) } doReturn thirdModuleModel
        on { fromModel(thirdModuleModel) } doReturn thirdModuleProto
    }

    private val controllers = mapOf(
        firstIdentifier to firstModuleRemoteConfigController,
        secondIdentifier to secondModuleRemoteConfigController,
        thirdIdentifier to thirdModuleRemoteConfigController
    )

    private lateinit var modulesController: ServiceModulesController
    private lateinit var modulesRemoteConfigConverter: ModulesRemoteConfigsConverter

    @Before
    fun setUp() {
        modulesController = GlobalServiceLocator.getInstance().modulesController
        whenever(modulesController.collectRemoteConfigControllers()).thenReturn(controllers)
        modulesRemoteConfigConverter = ModulesRemoteConfigsConverter()
    }

    @Test
    fun fromModelForFilled() {
        assertThat(
            modulesRemoteConfigConverter.fromModel(
                mapOf(
                    firstIdentifier to firstModuleModel,
                    secondIdentifier to secondModuleModel,
                    thirdIdentifier to thirdModuleModel
                )
            )
        )
            .usingRecursiveComparison()
            .isEqualTo(
                arrayOf(
                    createProtoEntry(firstIdentifier, firstModuleProto),
                    createProtoEntry(secondIdentifier, secondModuleProto),
                    createProtoEntry(thirdIdentifier, thirdModuleProto)
                )
            )
    }

    @Test
    fun fromModelForEmpty() {
        assertThat(modulesRemoteConfigConverter.fromModel(emptyMap())).isEmpty()
    }

    @Test
    fun fromModelWithoutModulesConverters() {
        whenever(modulesController.collectRemoteConfigControllers()).thenReturn(emptyMap())
        assertThat(
            modulesRemoteConfigConverter.fromModel(
                mapOf(
                    firstIdentifier to firstModuleModel,
                    secondIdentifier to secondModuleModel,
                    thirdIdentifier to thirdModuleModel
                )
            )
        ).isEmpty()
    }

    @Test
    fun fromModelForPartiallyFilled() {
        whenever(modulesController.collectRemoteConfigControllers())
            .thenReturn(mapOf(firstIdentifier to firstModuleRemoteConfigController))
        assertThat(
            modulesRemoteConfigConverter.fromModel(
                mapOf(
                    firstIdentifier to firstModuleModel,
                    secondIdentifier to secondModuleModel,
                    thirdIdentifier to thirdModuleModel
                )
            )
        )
            .usingRecursiveComparison()
            .isEqualTo(
                arrayOf(
                    createProtoEntry(firstIdentifier, firstModuleProto)
                )
            )
    }

    @Test
    fun toModelForFilled() {
        assertThat(
            modulesRemoteConfigConverter.toModel(
                arrayOf(
                    createProtoEntry(firstIdentifier, firstModuleProto),
                    createProtoEntry(secondIdentifier, secondModuleProto),
                    createProtoEntry(thirdIdentifier, thirdModuleProto),
                )
            )
        )
            .usingRecursiveComparison()
            .isEqualTo(
                mapOf(
                    firstIdentifier to firstModuleModel,
                    secondIdentifier to secondModuleModel,
                    thirdIdentifier to thirdModuleModel
                )
            )
    }

    @Test
    fun toModelForEmpty() {
        assertThat(
            modulesRemoteConfigConverter.toModel(
                emptyArray()
            )
        ).isEmpty()
    }

    @Test
    fun toModelWithoutModulesConverters() {
        whenever(modulesController.collectRemoteConfigControllers()).thenReturn(emptyMap())
        assertThat(
            modulesRemoteConfigConverter.toModel(
                arrayOf(
                    createProtoEntry(firstIdentifier, firstModuleProto),
                    createProtoEntry(secondIdentifier, secondModuleProto),
                    createProtoEntry(thirdIdentifier, thirdModuleProto),
                )
            )
        ).isEmpty()
    }

    @Test
    fun toModelForPartiallyFilledConverters() {
        whenever(modulesController.collectRemoteConfigControllers())
            .thenReturn(mapOf(firstIdentifier to firstModuleRemoteConfigController))
        assertThat(
            modulesRemoteConfigConverter.toModel(
                arrayOf(
                    createProtoEntry(firstIdentifier, firstModuleProto),
                    createProtoEntry(secondIdentifier, secondModuleProto),
                    createProtoEntry(thirdIdentifier, thirdModuleProto),
                )
            )
        )
            .usingRecursiveComparison()
            .isEqualTo(
                mapOf(
                    firstIdentifier to firstModuleModel
                )
            )
    }

    private fun createProtoEntry(key: String, value: ByteArray) =
        StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry().apply {
            this.key = key
            this.value = value
        }

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
}
