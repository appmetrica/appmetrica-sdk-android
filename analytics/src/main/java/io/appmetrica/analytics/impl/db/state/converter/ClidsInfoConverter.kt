package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto

internal class ClidsInfoConverter :
    ProtobufConverter<ClidsInfo, ClidsInfoProto.ClidsInfo> {

    override fun fromModel(value: ClidsInfo): ClidsInfoProto.ClidsInfo = ClidsInfoProto.ClidsInfo().apply {
        chosenClids = candidateToProto(value.chosen)
        candidates = Array(value.candidates.size) { candidateToProto(value.candidates[it]) }
    }

    override fun toModel(value: ClidsInfoProto.ClidsInfo): ClidsInfo = ClidsInfo(
        candidateToModel(value.chosenClids ?: ClidsInfoProto.ClidsInfo.ClidsCandidate()),
        value.candidates.map { candidateToModel(it) }
    )

    private fun candidateToProto(model: ClidsInfo.Candidate): ClidsInfoProto.ClidsInfo.ClidsCandidate =
        ClidsInfoProto.ClidsInfo.ClidsCandidate().apply {
            clids = model.clids?.let { mapToProto(it) }
            source = sourceToProto(model.source)
        }

    private fun mapToProto(model: Map<String, String>): ClidsInfoProto.ClidsInfo.NullableMap {
        val proto = ClidsInfoProto.ClidsInfo.NullableMap()
        proto.pairs = Array(model.size) { ClidsInfoProto.ClidsInfo.NullableMap.Pair() }
        var i = 0
        model.forEach { (key, value) ->
            proto.pairs[i].key = key
            proto.pairs[i].value = value
            i++
        }
        return proto
    }

    private fun sourceToProto(model: DistributionSource): Int =
        when (model) {
            DistributionSource.APP -> ClidsInfoProto.ClidsInfo.API
            DistributionSource.SATELLITE -> ClidsInfoProto.ClidsInfo.SATELLITE
            DistributionSource.RETAIL -> ClidsInfoProto.ClidsInfo.RETAIL
            DistributionSource.UNDEFINED -> ClidsInfoProto.ClidsInfo.UNDEFINED
        }

    private fun candidateToModel(proto: ClidsInfoProto.ClidsInfo.ClidsCandidate): ClidsInfo.Candidate =
        ClidsInfo.Candidate(
            proto.clids?.let { mapToModel(it) },
            sourceToModel(proto.source)
        )

    private fun mapToModel(proto: ClidsInfoProto.ClidsInfo.NullableMap): Map<String, String> =
        proto.pairs.associate { it.key to it.value }

    private fun sourceToModel(proto: Int): DistributionSource =
        when (proto) {
            ClidsInfoProto.ClidsInfo.API -> DistributionSource.APP
            ClidsInfoProto.ClidsInfo.SATELLITE -> DistributionSource.SATELLITE
            ClidsInfoProto.ClidsInfo.RETAIL -> DistributionSource.RETAIL
            ClidsInfoProto.ClidsInfo.UNDEFINED -> DistributionSource.UNDEFINED
            else -> DistributionSource.UNDEFINED
        }
}
