package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.db.protobuf.ClidsInfoStateSerializer
import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClidsInfoStateSerializerTest : CommonTest() {

    private val serializer = ClidsInfoStateSerializer()

    @Test
    fun testToByteArrayDefaultObject() {
        val protoState = ClidsInfoProto.ClidsInfo()
        val rawData: ByteArray = serializer.toByteArray(protoState)
        val restored = serializer.toState(rawData)
        assertThat(restored).usingRecursiveComparison().isEqualTo(protoState)
        ProtoObjectPropertyAssertions(restored)
            .checkFieldIsNull("chosenClids")
            .checkField("candidates", emptyArray<ClidsInfoProto.ClidsInfo.ClidsCandidate>())
            .checkAll()
    }

    @Test
    fun testToByteArrayFilledObject() {
        val protoState = ClidsInfoProto.ClidsInfo()
        protoState.chosenClids = ClidsInfoProto.ClidsInfo.ClidsCandidate().also {
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
        protoState.candidates = arrayOf(
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
                it.source = ClidsInfoProto.ClidsInfo.SATELLITE
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
                it.source = ClidsInfoProto.ClidsInfo.UNDEFINED
            },
        )

        val rawData = serializer.toByteArray(protoState)
        assertThat(rawData).isNotEmpty
        val restored = serializer.toState(rawData)
        assertThat(restored).usingRecursiveComparison().isEqualTo(protoState)
        ProtoObjectPropertyAssertions(restored)
            .withIgnoredFields("candidates")
            .checkFieldRecursively<ClidsInfoProto.ClidsInfo.ClidsCandidate>("chosenClids") {
                it.checkFieldRecursively<ClidsInfoProto.ClidsInfo.NullableMap>("clids") {
                    it.withIgnoredFields("pairs")
                    assertThat(it.actual.pairs.size).isEqualTo(2)
                    ProtoObjectPropertyAssertions(it.actual.pairs[0])
                        .checkField("key", "clid0")
                        .checkField("value", "0")
                        .checkAll()
                    ProtoObjectPropertyAssertions(it.actual.pairs[1])
                        .checkField("key", "clid1")
                        .checkField("value", "1")
                        .checkAll()
                }
                it.checkField("source", ClidsInfoProto.ClidsInfo.RETAIL)
            }
            .checkAll()
        val candidates = restored.candidates
        assertThat(candidates.size).isEqualTo(2)
        ProtoObjectPropertyAssertions(candidates[0])
            .checkFieldRecursively<ClidsInfoProto.ClidsInfo.NullableMap>("clids") {
                it.withIgnoredFields("pairs")
                assertThat(it.actual.pairs.size).isEqualTo(2)
                ProtoObjectPropertyAssertions(it.actual.pairs[0])
                    .checkField("key", "clid2")
                    .checkField("value", "2")
                    .checkAll()
                ProtoObjectPropertyAssertions(it.actual.pairs[1])
                    .checkField("key", "clid3")
                    .checkField("value", "3")
                    .checkAll()
            }
            .checkField("source", ClidsInfoProto.ClidsInfo.SATELLITE)
            .checkAll()
        ProtoObjectPropertyAssertions(candidates[1])
            .checkFieldRecursively<ClidsInfoProto.ClidsInfo.NullableMap>("clids") {
                it.withIgnoredFields("pairs")
                assertThat(it.actual.pairs.size).isEqualTo(2)
                ProtoObjectPropertyAssertions(it.actual.pairs[0])
                    .checkField("key", "clid4")
                    .checkField("value", "4")
                    .checkAll()
                ProtoObjectPropertyAssertions(it.actual.pairs[1])
                    .checkField("key", "clid5")
                    .checkField("value", "5")
                    .checkAll()
            }
            .checkField("source", ClidsInfoProto.ClidsInfo.UNDEFINED)
            .checkAll()
    }

    @Test(expected = InvalidProtocolBufferNanoException::class)
    fun testDeserializationInvalidByteArray() {
        serializer.toState(byteArrayOf(1, 2, 3))
    }
}
