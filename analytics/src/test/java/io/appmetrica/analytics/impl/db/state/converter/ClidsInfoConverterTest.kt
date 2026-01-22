package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.clids.ClidsInfo
import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ClidsInfoConverterTest : CommonTest() {

    private val converter = ClidsInfoConverter()

    @Test
    fun toProtoNullable() {
        val model = ClidsInfo(ClidsInfo.Candidate(null, DistributionSource.UNDEFINED), emptyList())
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .checkFieldRecursively<ClidsInfoProto.ClidsInfo.ClidsCandidate>("chosenClids") {
                it.checkFieldIsNull("clids")
                it.checkField("source", ClidsInfoProto.ClidsInfo.UNDEFINED)
            }
            .checkField("candidates", emptyArray<ClidsInfoProto.ClidsInfo.ClidsCandidate>())
            .checkAll()
    }

    @Test
    fun toProtoFilled() {
        val model = ClidsInfo(
            ClidsInfo.Candidate(mapOf("clid0" to "0", "clid1" to "1"), DistributionSource.RETAIL),
            listOf(
                ClidsInfo.Candidate(mapOf("clid2" to "2", "clid3" to "3"), DistributionSource.APP),
                ClidsInfo.Candidate(mapOf("clid4" to "4", "clid5" to "5"), DistributionSource.SATELLITE),
                ClidsInfo.Candidate(mapOf("clid6" to "6", "clid7" to "7"), DistributionSource.UNDEFINED),
            )
        )
        val proto = converter.fromModel(model)
        ProtoObjectPropertyAssertions(proto)
            .withIgnoredFields("candidates", "chosenClids")
            .checkAll()
        checkProtoCandidate(
            proto.chosenClids,
            arrayOf(
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid0"
                    it.value = "0"
                },
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid1"
                    it.value = "1"
                }
            ),
            ClidsInfoProto.ClidsInfo.RETAIL
        )
        val candidates = proto.candidates
        assertThat(candidates.size).isEqualTo(3)
        checkProtoCandidate(
            candidates[0],
            arrayOf(
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid2"
                    it.value = "2"
                },
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid3"
                    it.value = "3"
                }
            ),
            ClidsInfoProto.ClidsInfo.API
        )
        checkProtoCandidate(
            candidates[1],
            arrayOf(
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid4"
                    it.value = "4"
                },
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid5"
                    it.value = "5"
                }
            ),
            ClidsInfoProto.ClidsInfo.SATELLITE
        )
        checkProtoCandidate(
            candidates[2],
            arrayOf(
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid6"
                    it.value = "6"
                },
                ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                    it.key = "clid7"
                    it.value = "7"
                }
            ),
            ClidsInfoProto.ClidsInfo.UNDEFINED
        )
    }

    @Test
    fun toModelNullable() {
        val proto = ClidsInfoProto.ClidsInfo()
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .checkFieldRecursively<ClidsInfo.Candidate>("chosen") {
                it.withPrivateFields(true)
                    .withFinalFieldOnly(false)
                    .checkFieldIsNull("clids", "getClids")
                    .checkField("source", "getSource", DistributionSource.UNDEFINED)
            }
            .checkField("candidates", "getCandidates", emptyList<ClidsInfo.Candidate>())
            .checkAll()
    }

    @Test
    fun toModelFilled() {
        val proto = ClidsInfoProto.ClidsInfo()
        proto.chosenClids = ClidsInfoProto.ClidsInfo.ClidsCandidate().also {
            it.clids = ClidsInfoProto.ClidsInfo.NullableMap().also {
                it.pairs = arrayOf(
                    ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                        it.key = "clid0"
                        it.value = "0"
                    },
                    ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                        it.key = "clid1"
                        it.value = "1"
                    },
                )
            }
            it.source = ClidsInfoProto.ClidsInfo.RETAIL
        }
        proto.candidates = arrayOf(
            ClidsInfoProto.ClidsInfo.ClidsCandidate().also {
                it.clids = ClidsInfoProto.ClidsInfo.NullableMap().also {
                    it.pairs = arrayOf(
                        ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                            it.key = "clid2"
                            it.value = "2"
                        },
                        ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                            it.key = "clid3"
                            it.value = "3"
                        },
                    )
                }
                it.source = ClidsInfoProto.ClidsInfo.API
            },
            ClidsInfoProto.ClidsInfo.ClidsCandidate().also {
                it.clids = ClidsInfoProto.ClidsInfo.NullableMap().also {
                    it.pairs = arrayOf(
                        ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                            it.key = "clid4"
                            it.value = "4"
                        },
                        ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                            it.key = "clid5"
                            it.value = "5"
                        },
                    )
                }
                it.source = ClidsInfoProto.ClidsInfo.SATELLITE
            },
            ClidsInfoProto.ClidsInfo.ClidsCandidate().also {
                it.clids = ClidsInfoProto.ClidsInfo.NullableMap().also {
                    it.pairs = arrayOf(
                        ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                            it.key = "clid6"
                            it.value = "6"
                        },
                        ClidsInfoProto.ClidsInfo.NullableMap.Pair().also {
                            it.key = "clid7"
                            it.value = "7"
                        },
                    )
                }
                it.source = ClidsInfoProto.ClidsInfo.UNDEFINED
            },
        )
        val model = converter.toModel(proto)
        ObjectPropertyAssertions(model)
            .withIgnoredFields("candidates", "chosen")
            .checkAll()
        checkModelCandidate(model.chosen, mapOf("clid0" to "0", "clid1" to "1"), DistributionSource.RETAIL)
        val candidates = model.candidates
        assertThat(candidates.size).isEqualTo(3)
        checkModelCandidate(candidates[0], mapOf("clid2" to "2", "clid3" to "3"), DistributionSource.APP)
        checkModelCandidate(candidates[1], mapOf("clid4" to "4", "clid5" to "5"), DistributionSource.SATELLITE)
        checkModelCandidate(candidates[2], mapOf("clid6" to "6", "clid7" to "7"), DistributionSource.UNDEFINED)
    }

    private fun checkModelCandidate(
        actual: ClidsInfo.Candidate,
        clids: Map<String, String>,
        source: DistributionSource
    ) {
        ObjectPropertyAssertions(actual)
            .checkField("clids", "getClids", clids)
            .checkField("source", "getSource", source)
            .checkAll()
    }

    private fun checkProtoCandidate(
        actual: ClidsInfoProto.ClidsInfo.ClidsCandidate,
        pairs: Array<ClidsInfoProto.ClidsInfo.NullableMap.Pair>,
        source: Int
    ) {
        ProtoObjectPropertyAssertions(actual)
            .checkFieldRecursively<ClidsInfoProto.ClidsInfo.NullableMap>("clids") {
                it.withIgnoredFields("pairs")
                assertThat(it.actual.pairs.size).isEqualTo(pairs.size)
                pairs.forEachIndexed { index, pair ->
                    ProtoObjectPropertyAssertions(it.actual.pairs[index])
                        .checkField("key", pair.key)
                        .checkField("value", pair.value)
                        .checkAll()
                }
            }
            .checkField("source", source)
            .checkAll()
    }
}
