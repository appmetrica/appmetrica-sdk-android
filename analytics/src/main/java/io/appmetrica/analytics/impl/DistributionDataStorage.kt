package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState
import io.appmetrica.analytics.logger.internal.YLogger

internal class ClidsInfoStorage(
    context: Context,
    storage: ProtobufStateStorage<ClidsInfo>,
    priorityProvider: DistributionPriorityProvider<ClidsInfo.Candidate>,
    candidatesProvider: UpdatedCandidatesProvider<ClidsInfo.Candidate, ClidsInfo.Candidate>,
    stateProvider: StateProvider<ClidsInfo.Candidate, ClidsInfo.Candidate, ClidsInfo>,
    satelliteDataProvider: SatelliteDataProvider<ClidsInfo.Candidate>,
    satelliteCheckedProvider: SatelliteCheckedProvider,
    dataWaiter: DataAwaiter,
    data: ClidsInfo,
    tag: String
) : DistributionDataStorage<ClidsInfo.Candidate, ClidsInfo.Candidate, ClidsInfo>(
    context,
    storage,
    priorityProvider,
    candidatesProvider,
    stateProvider,
    satelliteDataProvider,
    satelliteCheckedProvider,
    dataWaiter,
    data,
    tag
)

internal class PreloadInfoStorage(
    context: Context,
    storage: ProtobufStateStorage<PreloadInfoData>,
    priorityProvider: DistributionPriorityProvider<PreloadInfoState>,
    candidatesProvider: UpdatedCandidatesProvider<PreloadInfoData.Candidate, PreloadInfoState>,
    stateProvider: StateProvider<PreloadInfoData.Candidate, PreloadInfoState, PreloadInfoData>,
    satelliteDataProvider: SatelliteDataProvider<PreloadInfoState>,
    satelliteCheckedProvider: SatelliteCheckedProvider,
    dataWaiter: DataAwaiter,
    data: PreloadInfoData,
    tag: String
) : DistributionDataStorage<PreloadInfoData.Candidate, PreloadInfoState, PreloadInfoData>(
    context,
    storage,
    priorityProvider,
    candidatesProvider,
    stateProvider,
    satelliteDataProvider,
    satelliteCheckedProvider,
    dataWaiter,
    data,
    tag
)

internal open class DistributionDataStorage<CANDIDATE, CHOSEN, STORAGE> (
    private val context: Context,
    private val storage: ProtobufStateStorage<STORAGE>,
    private val priorityProvider: DistributionPriorityProvider<CHOSEN>,
    private val candidatesProvider: UpdatedCandidatesProvider<CANDIDATE, CHOSEN>,
    private val stateProvider: StateProvider<CANDIDATE, CHOSEN, STORAGE>,
    private val satelliteDataProvider: SatelliteDataProvider<CHOSEN>,
    private val satelliteCheckedProvider: SatelliteCheckedProvider,
    private val dataWaiter: DataAwaiter,
    private var data: STORAGE,
    private val tag: String
) where STORAGE : DistributionInfo<CANDIDATE, CHOSEN>, CHOSEN : DistributionSourceProvider {

    @Synchronized
    fun updateIfNeeded(newData: CHOSEN): Boolean {
        YLogger.info(tag, "Update if needed: $newData")
        val updatedChosen: Boolean
        if (newData.source == DistributionSource.UNDEFINED) {
            YLogger.info(tag, "Do not update to $newData because its source is undefined")
            return false
        }
        if (newData == data.chosen) {
            YLogger.info(tag, "Do not update to $newData because it's the same as ${data.chosen}")
            return false
        }
        var newCandidates = candidatesProvider(data.candidates, newData)
        val updatedCandidates = newCandidates != null
        newCandidates = newCandidates ?: data.candidates
        YLogger.info(
            tag,
            "Updated candidates got by merging ${data.candidates} with $newData is $newCandidates. " +
                "Were candidates updated: $updatedCandidates"
        )
        val newChosen: CHOSEN
        if (priorityProvider.isNewDataMoreImportant(newData, data.chosen)) {
            updatedChosen = true
            newChosen = newData
            YLogger.info(tag, "Updated chosen data to $newChosen from ${data.chosen}.")
        } else {
            updatedChosen = false
            newChosen = data.chosen
        }
        YLogger.info(tag, "New chosen data is $newChosen. Was chosen updated: $updatedChosen. New state is: $data")
        if (updatedChosen || updatedCandidates) {
            val oldData = data
            data = stateProvider(newChosen, newCandidates)
            storage.save(data)
            SdkUtils.logAttribution("Update distribution data: %s -> %s", oldData, data)
        }
        return updatedChosen
    }

    fun retrieveData(): CHOSEN {
        dataWaiter.waitForData(context)
        return retrieveStateInternal()
    }

    @Synchronized
    fun getCachedState(): STORAGE {
        return data
    }

    fun updateAndRetrieveData(newData: CHOSEN): CHOSEN {
        dataWaiter.waitForData(context)
        YLogger.info(tag, "Maybe update to $newData and then retrieve.")
        synchronized(this) {
            updateIfNeeded(newData)
            return retrieveStateInternal()
        }
    }

    @Synchronized
    private fun retrieveStateInternal(): CHOSEN {
        maybeUpdateSatelliteData()
        YLogger.info(tag, "Choosing distribution data: $data")
        return data.chosen
    }

    private fun maybeUpdateSatelliteData() {
        YLogger.info(tag, "Maybe update satellite data. Current data: $data")
        if (!satelliteCheckedProvider.wasSatelliteChecked()) {
            val dataFromSatellite = satelliteDataProvider()
            YLogger.info(tag, "Retrieved data from satellite: $dataFromSatellite")
            satelliteCheckedProvider.markSatelliteChecked()
            if (dataFromSatellite != null) {
                updateIfNeeded(dataFromSatellite)
            }
        }
    }
}
