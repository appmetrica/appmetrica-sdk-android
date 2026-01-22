package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class DistributionDataStorageTest : CommonTest() {

    private val diskStorage = mock<ProtobufStateStorage<ClidsInfo>>()
    private val satelliteProvider = mock<SatelliteDataProvider<ClidsInfo.Candidate>>()
    private val context = mock<Context>()
    private val priorityProvider = mock<DistributionPriorityProvider<ClidsInfo.Candidate>>()
    private val candidatesProvider = mock<UpdatedCandidatesProvider<ClidsInfo.Candidate, ClidsInfo.Candidate>>()
    private val stateProvider = mock<StateProvider<ClidsInfo.Candidate, ClidsInfo.Candidate, ClidsInfo>>()
    private val satelliteCheckedProvider = mock<SatelliteCheckedProvider>()
    private val dataAwaiter = mock<DataAwaiter>()
    private lateinit var storage: DistributionDataStorage<ClidsInfo.Candidate, ClidsInfo.Candidate, ClidsInfo>
    private val diskChosen = ClidsInfo.Candidate(mapOf("clid0" to "0"), DistributionSource.RETAIL)
    private val diskCandidates = listOf(
        ClidsInfo.Candidate(mapOf("clid1" to "1"), DistributionSource.SATELLITE),
        ClidsInfo.Candidate(mapOf("clid2" to "2"), DistributionSource.APP)
    )
    private val diskData = ClidsInfo(diskChosen, diskCandidates)

    @Test
    fun updateIfNeededUndefined() {
        storage = createDistributionStorage(diskData)
        val result = storage.updateIfNeeded(ClidsInfo.Candidate(mapOf("clid11" to "11"), DistributionSource.UNDEFINED))
        verifyNoInteractions(candidatesProvider, priorityProvider, stateProvider, diskStorage)
        assertThat(result).isFalse

        assertThat(storage.retrieveData()).isSameAs(diskChosen)
    }

    @Test
    fun updateIfNeededNewCandidatesAreNullButUpdatedChosen() {
        val newChosen = ClidsInfo.Candidate(mapOf("clid22" to "22"), DistributionSource.APP)
        val newData = ClidsInfo(
            newChosen,
            listOf(ClidsInfo.Candidate(mapOf("clid22" to "22"), DistributionSource.APP))
        )
        val newCandidate = ClidsInfo.Candidate(mapOf("clid11" to "11"), DistributionSource.APP)
        storage = createDistributionStorage(diskData)
        whenever(candidatesProvider(diskCandidates, newCandidate)).thenReturn(null)
        whenever(priorityProvider.isNewDataMoreImportant(newCandidate, diskChosen)).thenReturn(true)
        whenever(stateProvider(newCandidate, diskCandidates)).thenReturn(newData)
        val result = storage.updateIfNeeded(newCandidate)
        verify(candidatesProvider).invoke(diskCandidates, newCandidate)
        verify(priorityProvider).isNewDataMoreImportant(newCandidate, diskChosen)
        verify(stateProvider).invoke(newCandidate, diskCandidates)
        verify(diskStorage).save(newData)
        assertThat(result).isTrue

        assertThat(storage.retrieveData()).isSameAs(newChosen)
        assertThat(storage.getCachedState()).isSameAs(newData)
    }

    @Test
    fun updateIfNewCandidateTheSameAsCurrent() {
        val storageData = ClidsInfo(diskChosen, diskCandidates)
        storage = createDistributionStorage(storageData)
        clearInvocations(candidatesProvider, priorityProvider, stateProvider, diskStorage)
        storage.updateIfNeeded(diskChosen)
        storage.updateIfNeeded(diskChosen)
        verifyNoInteractions(candidatesProvider, priorityProvider, stateProvider, diskStorage)
    }

    @Test
    fun updateIfNeededNewCandidatesAreNullAndDidNotUpdateChosen() {
        val newData = ClidsInfo(diskChosen, diskCandidates)
        val newCandidate = ClidsInfo.Candidate(mapOf("clid11" to "11"), DistributionSource.APP)
        storage = createDistributionStorage(diskData)
        whenever(candidatesProvider(diskCandidates, newCandidate)).thenReturn(null)
        whenever(priorityProvider.isNewDataMoreImportant(newCandidate, diskChosen)).thenReturn(false)
        whenever(stateProvider(diskChosen, diskCandidates)).thenReturn(newData)
        val result = storage.updateIfNeeded(newCandidate)
        verify(candidatesProvider).invoke(diskCandidates, newCandidate)
        verify(priorityProvider).isNewDataMoreImportant(newCandidate, diskChosen)
        verifyNoInteractions(stateProvider, diskStorage)
        assertThat(result).isFalse

        assertThat(storage.retrieveData()).isSameAs(diskChosen)
        assertThat(storage.getCachedState()).isSameAs(diskData)
    }

    @Test
    fun updateIfNeededNewCandidatesAreNonNullAndUpdatedChosen() {
        val newChosen = ClidsInfo.Candidate(mapOf("clid22" to "22"), DistributionSource.APP)
        val newCandidates = listOf(ClidsInfo.Candidate(mapOf("clid22" to "22"), DistributionSource.APP))
        val newData = ClidsInfo(newChosen, newCandidates)
        val newCandidate = ClidsInfo.Candidate(mapOf("clid11" to "11"), DistributionSource.APP)
        storage = createDistributionStorage(diskData)
        whenever(candidatesProvider(diskCandidates, newCandidate)).thenReturn(newCandidates)
        whenever(priorityProvider.isNewDataMoreImportant(newCandidate, diskChosen)).thenReturn(true)
        whenever(stateProvider(newCandidate, newCandidates)).thenReturn(newData)
        val result = storage.updateIfNeeded(newCandidate)
        verify(candidatesProvider).invoke(diskCandidates, newCandidate)
        verify(priorityProvider).isNewDataMoreImportant(newCandidate, diskChosen)
        verify(stateProvider).invoke(newCandidate, newCandidates)
        verify(diskStorage).save(newData)
        assertThat(result).isTrue

        assertThat(storage.retrieveData()).isSameAs(newChosen)
        assertThat(storage.getCachedState()).isSameAs(newData)
    }

    @Test
    fun updateIfNeededNewCandidatesAreNonNullAndDidNotUpdateChosen() {
        val chosen = ClidsInfo.Candidate(mapOf("clid11" to "11"), DistributionSource.APP)
        val newCandidates = listOf(ClidsInfo.Candidate(mapOf("clid22" to "22"), DistributionSource.APP))
        val newData = ClidsInfo(chosen, newCandidates)
        storage = createDistributionStorage(diskData)
        whenever(candidatesProvider(diskCandidates, chosen)).thenReturn(newCandidates)
        whenever(priorityProvider.isNewDataMoreImportant(chosen, diskChosen)).thenReturn(false)
        whenever(stateProvider(diskChosen, newCandidates)).thenReturn(newData)
        val result = storage.updateIfNeeded(chosen)
        verify(candidatesProvider).invoke(diskCandidates, chosen)
        verify(priorityProvider).isNewDataMoreImportant(chosen, diskChosen)
        verify(stateProvider).invoke(diskChosen, newCandidates)
        verify(diskStorage).save(newData)
        assertThat(result).isFalse

        assertThat(storage.retrieveData()).isSameAs(chosen)
        assertThat(storage.getCachedState()).isSameAs(newData)
    }

    @Test
    fun retrieveDataWaits() {
        storage = createDistributionStorage(diskData)
        storage.retrieveData()
        verify(dataAwaiter).waitForData(context)
    }

    @Test
    fun retrieveDataDoesNotUpdateSatelliteIfChecked() {
        storage = createDistributionStorage(diskData)
        whenever(satelliteCheckedProvider.wasSatelliteChecked()).thenReturn(true)
        assertThat(storage.retrieveData()).isSameAs(diskChosen)
        verifyNoInteractions(satelliteProvider)
        verify(satelliteCheckedProvider, never()).markSatelliteChecked()
        verifyNoInteractions(candidatesProvider, priorityProvider, stateProvider, diskStorage)
    }

    @Test
    fun retrieveDataDoesNotUpdateIfSatelliteDataIsNull() {
        storage = createDistributionStorage(diskData)
        whenever(satelliteCheckedProvider.wasSatelliteChecked()).thenReturn(false)
        whenever(satelliteProvider.invoke()).thenReturn(null)
        assertThat(storage.retrieveData()).isSameAs(diskChosen)
        verify(satelliteProvider).invoke()
        verify(satelliteCheckedProvider).markSatelliteChecked()
        verifyNoInteractions(candidatesProvider, priorityProvider, stateProvider, diskStorage)
    }

    @Test
    fun retrieveDataUpdatesIfSatelliteDataIsNotNull() {
        val newCandidates = listOf(mock<ClidsInfo.Candidate>(), mock<ClidsInfo.Candidate>())
        val satelliteCandidate = ClidsInfo.Candidate(mapOf("clid9" to "9"), DistributionSource.SATELLITE)
        val state = ClidsInfo(satelliteCandidate, newCandidates)
        storage = createDistributionStorage(diskData)
        whenever(satelliteCheckedProvider.wasSatelliteChecked()).thenReturn(false)
        whenever(satelliteProvider.invoke()).thenReturn(satelliteCandidate)
        whenever(candidatesProvider(diskCandidates, satelliteCandidate)).thenReturn(newCandidates)
        whenever(priorityProvider.isNewDataMoreImportant(satelliteCandidate, diskChosen)).thenReturn(true)
        whenever(stateProvider(satelliteCandidate, newCandidates)).thenReturn(state)
        assertThat(storage.retrieveData()).isSameAs(satelliteCandidate)

        verify(satelliteProvider).invoke()
        verify(satelliteCheckedProvider).markSatelliteChecked()
        verify(candidatesProvider).invoke(diskCandidates, satelliteCandidate)
        verify(priorityProvider).isNewDataMoreImportant(satelliteCandidate, diskChosen)
        verify(stateProvider).invoke(satelliteCandidate, newCandidates)
        verify(diskStorage).save(state)
    }

    @Test
    fun retrieveStateDoesNotUpdateSatellite() {
        val newCandidates = listOf(mock<ClidsInfo.Candidate>(), mock<ClidsInfo.Candidate>())
        val satelliteCandidate = ClidsInfo.Candidate(mapOf("clid9" to "9"), DistributionSource.SATELLITE)
        val state = ClidsInfo(satelliteCandidate, newCandidates)
        storage = createDistributionStorage(diskData)
        whenever(satelliteCheckedProvider.wasSatelliteChecked()).thenReturn(false)
        whenever(satelliteProvider.invoke()).thenReturn(satelliteCandidate)
        whenever(candidatesProvider(diskCandidates, satelliteCandidate)).thenReturn(newCandidates)
        whenever(priorityProvider.isNewDataMoreImportant(satelliteCandidate, diskChosen)).thenReturn(true)
        whenever(stateProvider(satelliteCandidate, newCandidates)).thenReturn(state)
        assertThat(storage.getCachedState()).isSameAs(diskData)

        verify(satelliteProvider, never()).invoke()
        verify(satelliteCheckedProvider, never()).markSatelliteChecked()
        verify(candidatesProvider, never()).invoke(diskCandidates, satelliteCandidate)
        verify(priorityProvider, never()).isNewDataMoreImportant(satelliteCandidate, diskChosen)
        verify(stateProvider, never()).invoke(satelliteCandidate, newCandidates)
        verify(diskStorage, never()).save(state)
    }

    @Test
    fun updateAndRetrieveData() {
        val newCandidates = listOf(
            mock<ClidsInfo.Candidate>(),
            mock<ClidsInfo.Candidate>()
        )
        val newSatelliteCandidates = listOf(
            mock<ClidsInfo.Candidate>(),
            mock<ClidsInfo.Candidate>(),
            mock<ClidsInfo.Candidate>()
        )
        val candidate = ClidsInfo.Candidate(mapOf("clid9" to "9"), DistributionSource.RETAIL)
        val satelliteCandidate = ClidsInfo.Candidate(mapOf("clid8" to "8"), DistributionSource.SATELLITE)
        val state = ClidsInfo(candidate, newCandidates)
        val satelliteState = ClidsInfo(satelliteCandidate, newSatelliteCandidates)
        storage = createDistributionStorage(diskData)
        whenever(satelliteProvider()).thenReturn(satelliteCandidate)
        whenever(candidatesProvider(diskCandidates, candidate)).thenReturn(newCandidates)
        whenever(candidatesProvider(newCandidates, satelliteCandidate)).thenReturn(newSatelliteCandidates)
        whenever(priorityProvider.isNewDataMoreImportant(candidate, diskChosen)).thenReturn(true)
        whenever(priorityProvider.isNewDataMoreImportant(satelliteCandidate, candidate)).thenReturn(true)
        whenever(stateProvider(candidate, newCandidates)).thenReturn(state)
        whenever(stateProvider(satelliteCandidate, newSatelliteCandidates)).thenReturn(satelliteState)
        whenever(satelliteCheckedProvider.wasSatelliteChecked()).thenReturn(false)

        assertThat(storage.updateAndRetrieveData(candidate)).isSameAs(satelliteCandidate)
        verify(dataAwaiter).waitForData(context)
        val inOrder = inOrder(candidatesProvider, priorityProvider, stateProvider, diskStorage)
        inOrder.verify(candidatesProvider).invoke(diskCandidates, candidate)
        inOrder.verify(priorityProvider).isNewDataMoreImportant(candidate, diskChosen)
        inOrder.verify(stateProvider).invoke(candidate, newCandidates)
        inOrder.verify(diskStorage).save(state)
        inOrder.verify(candidatesProvider).invoke(newCandidates, satelliteCandidate)
        inOrder.verify(priorityProvider).isNewDataMoreImportant(satelliteCandidate, candidate)
        inOrder.verify(stateProvider).invoke(satelliteCandidate, newSatelliteCandidates)
        inOrder.verify(diskStorage).save(satelliteState)

        verify(satelliteCheckedProvider).markSatelliteChecked()
    }

    private fun createDistributionStorage(
        storageData: ClidsInfo
    ): DistributionDataStorage<ClidsInfo.Candidate, ClidsInfo.Candidate, ClidsInfo> {
        return DistributionDataStorage(
            context,
            diskStorage,
            priorityProvider,
            candidatesProvider,
            stateProvider,
            satelliteProvider,
            satelliteCheckedProvider,
            dataAwaiter,
            storageData,
            "clids"
        )
    }
}
