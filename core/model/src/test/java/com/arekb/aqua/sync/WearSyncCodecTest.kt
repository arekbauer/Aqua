package com.arekb.aqua.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearSyncCodecTest {
    private val event = IntakeEvent(
        eventId = "7b0c5b0e-0f4e-4f0c-9f43-2f0c3a9c1e10",
        amountMl = 350,
        timestampMillis = 1_793_400_000_000,
        localDate = "2026-10-31"
    )

    @Test
    fun intakeEventRoundTrips() {
        assertEquals(event, WearSyncCodec.decodeEvent(WearSyncCodec.encode(event)))
    }

    @Test
    fun watchStateRoundTrips() {
        val state = WatchState(
            localDate = "2026-10-31",
            totalMl = 1_250,
            goalMl = 2_000,
            presetsMl = listOf(200, 350, 500),
            liquidUnit = "OZ",
            acknowledgedEventIds = listOf("a", "b")
        )
        assertEquals(state, WearSyncCodec.decodeState(WearSyncCodec.encode(state)))
    }

    @Test
    fun protocolVersionIsWrittenAsShortKey() {
        val json = WearSyncCodec.encode(event).toString(Charsets.UTF_8)
        assertTrue(json, json.contains("\"v\":${WearSyncContract.PROTOCOL_VERSION}"))
    }

    @Test
    fun unknownKeysFromNewerVersionsAreIgnored() {
        val payload = """{"v":1,"eventId":"x","amountMl":200,"timestampMillis":5,"localDate":"2026-10-31","extra":true}"""
        val decoded = WearSyncCodec.decodeEvent(payload.toByteArray())
        assertEquals("x", decoded?.eventId)
    }

    @Test
    fun malformedOrEmptyPayloadsDecodeToNull() {
        assertNull(WearSyncCodec.decodeEvent(null))
        assertNull(WearSyncCodec.decodeEvent(ByteArray(0)))
        assertNull(WearSyncCodec.decodeEvent("not json".toByteArray()))
        assertNull(WearSyncCodec.decodeEvent("""{"eventId":"x"}""".toByteArray()))
        assertNull(WearSyncCodec.decodeState("{}".toByteArray()))
    }

    @Test
    fun intakePathsRoundTripThroughHelpers() {
        val path = WearSyncContract.intakePath("abc")
        assertEquals("/aqua/intake/abc", path)
        assertTrue(WearSyncContract.isIntakePath(path))
        assertTrue(!WearSyncContract.isIntakePath(WearSyncContract.STATE_PATH))
    }
}
