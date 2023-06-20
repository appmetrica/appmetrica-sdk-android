package io.appmetrica.analytics.impl.db

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.ArgumentMatcher
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode

class VitalDataProviderTest : CommonTest() {

    private val primaryDataSource = mock<VitalDataSource>()
    private val backupDataSource = mock<VitalDataSource>()
    private val tag = "Tag"
    private val vitalDataProviderStateMerger = mock<VitalDataProviderStateMerger>()

    private val primaryJson = JSONObject().put("key", "primary")
    private val backupJson = JSONObject().put("key", "backup")
    private val fromMergerJson = JSONObject().put("key", "from merger")

    private val vitalDataProvider =
        VitalDataProvider(primaryDataSource, backupDataSource, tag, vitalDataProviderStateMerger)

    private val emptyJsonArgumentMatcher = ArgumentMatcher<JSONObject> { argument ->
        JSONCompare.compareJSON(argument, JSONObject(), JSONCompareMode.STRICT).passed()
    }

    private val primaryJsonArgumentMatcher = ArgumentMatcher<JSONObject> { argument ->
        JSONCompare.compareJSON(argument, primaryJson, JSONCompareMode.STRICT).passed()
    }

    private val backupJsonArgumentMatcher = ArgumentMatcher<JSONObject> { argument ->
        JSONCompare.compareJSON(argument, backupJson, JSONCompareMode.STRICT).passed()
    }

    @Test
    fun getOrLoadDataIfOnlyPrimaryDataSourceHasContent() {
        whenever(primaryDataSource.getVitalData()).thenReturn(primaryJson.toString())
        whenever(vitalDataProviderStateMerger.merge(
            argThat(primaryJsonArgumentMatcher),
            argThat(emptyJsonArgumentMatcher))
        )
            .thenReturn(fromMergerJson)
        assertUsedJson(fromMergerJson)
    }

    @Test
    fun getOrLoadDataIfOnlyBackupDataSourceHasContent() {
        whenever(backupDataSource.getVitalData()).thenReturn(backupJson.toString())
        whenever(vitalDataProviderStateMerger.merge(
            argThat(emptyJsonArgumentMatcher),
            argThat(backupJsonArgumentMatcher)
        ))
            .thenReturn(fromMergerJson)
        assertUsedJson(fromMergerJson)
    }

    @Test
    fun getOrLoadDataIfBothBackupSourcesHasContent() {
        whenever(primaryDataSource.getVitalData()).thenReturn(primaryJson.toString())
        whenever(backupDataSource.getVitalData()).thenReturn(backupJson.toString())
        whenever(vitalDataProviderStateMerger.merge(
            argThat(primaryJsonArgumentMatcher),
            argThat(backupJsonArgumentMatcher)
        ))
            .thenReturn(fromMergerJson)
        assertUsedJson(fromMergerJson)
    }

    @Test
    fun getOrLoadDataMultipleTimes() {
        whenever(primaryDataSource.getVitalData()).thenReturn(primaryJson.toString())
        whenever(backupDataSource.getVitalData()).thenReturn(backupJson.toString())
        whenever(vitalDataProviderStateMerger.merge(
            argThat(primaryJsonArgumentMatcher),
            argThat(backupJsonArgumentMatcher)
        ))
            .thenReturn(fromMergerJson)
        repeat((0..10).count()) {
            vitalDataProvider.getOrLoadData()
        }
        verify(primaryDataSource).getVitalData()
        verify(backupDataSource).getVitalData()
    }

    @Test
    fun getOrLoadDataIfStoragesIsEmpty() {
        whenever(vitalDataProviderStateMerger.merge(any(), any()))
            .thenReturn(fromMergerJson)
        vitalDataProvider.getOrLoadData()
        verify(vitalDataProviderStateMerger).merge(argThat(emptyJsonArgumentMatcher), argThat(emptyJsonArgumentMatcher))
    }

    @Test
    fun getOrLoadDataIfStoragesHaveEmptyContent() {
        whenever(primaryDataSource.getVitalData()).thenReturn("")
        whenever(backupDataSource.getVitalData()).thenReturn("")
        whenever(vitalDataProviderStateMerger.merge(argThat(emptyJsonArgumentMatcher), argThat(emptyJsonArgumentMatcher)))
            .thenReturn(fromMergerJson)
        assertUsedJson(fromMergerJson)
    }

    @Test
    fun getOrLoadDataIfStoragesHaveInvalidJson() {
        whenever(primaryDataSource.getVitalData()).thenReturn("Invalid json")
        whenever(backupDataSource.getVitalData()).thenReturn("Invalid json")
        whenever(vitalDataProviderStateMerger.merge(argThat(emptyJsonArgumentMatcher), argThat(emptyJsonArgumentMatcher)))
            .thenReturn(fromMergerJson)
        assertUsedJson(fromMergerJson)
    }

    @Test
    fun getOrLoadIfStoragesThrowException() {
        whenever(primaryDataSource.getVitalData()).thenThrow(RuntimeException())
        whenever(backupDataSource.getVitalData()).thenThrow(RuntimeException())
        whenever(vitalDataProviderStateMerger.merge(argThat(emptyJsonArgumentMatcher), argThat(emptyJsonArgumentMatcher)))
            .thenReturn(fromMergerJson)
        assertUsedJson(fromMergerJson)
    }

    @Test
    fun save() {
        vitalDataProvider.save(fromMergerJson)
        verify(primaryDataSource).putVitalData(fromMergerJson.toString())
        verify(backupDataSource).putVitalData(fromMergerJson.toString())
    }

    @Test
    fun saveForEmptyJson() {
        vitalDataProvider.save(JSONObject())
        verify(primaryDataSource).putVitalData(JSONObject().toString())
        verify(backupDataSource).putVitalData(JSONObject().toString())
    }

    @Test
    fun saveIfStoragesThrow() {
        whenever(primaryDataSource.putVitalData(any())).thenThrow(RuntimeException())
        whenever(backupDataSource.putVitalData(any())).thenThrow(RuntimeException())
        vitalDataProvider.save(JSONObject())
    }

    private fun assertUsedJson(json: JSONObject) {
        assertThat(vitalDataProvider.getOrLoadData()).isEqualTo(json)
        verify(primaryDataSource).putVitalData(json.toString())
        verify(backupDataSource).putVitalData(json.toString())
    }
}
