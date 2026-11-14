package com.arekb.aqua.sync

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** JSON (UTF-8) encoding of the wear sync payloads. Decoders return null for malformed input. */
object WearSyncCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(event: IntakeEvent): ByteArray =
        json.encodeToString(IntakeEvent.serializer(), event).toByteArray(Charsets.UTF_8)

    fun encode(state: WatchState): ByteArray =
        json.encodeToString(WatchState.serializer(), state).toByteArray(Charsets.UTF_8)

    fun decodeEvent(payload: ByteArray?): IntakeEvent? = decode(payload) {
        json.decodeFromString(IntakeEvent.serializer(), it)
    }

    fun decodeState(payload: ByteArray?): WatchState? = decode(payload) {
        json.decodeFromString(WatchState.serializer(), it)
    }

    private inline fun <T> decode(payload: ByteArray?, block: (String) -> T): T? {
        if (payload == null || payload.isEmpty()) return null
        return try {
            block(payload.toString(Charsets.UTF_8))
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
