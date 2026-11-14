package com.arekb.aqua.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Constants shared by the phone and watch apps. See docs/wear-sync.md for the full protocol. */
object WearSyncContract {
    const val PROTOCOL_VERSION = 1

    /** Watch -> phone. One DataItem per drink at `/aqua/intake/{eventId}`. */
    const val INTAKE_PATH_PREFIX = "/aqua/intake"

    /** Phone -> watch. A single DataItem holding the latest [WatchState]. */
    const val STATE_PATH = "/aqua/state"

    const val MAX_EVENT_ID_LENGTH = 64
    const val MAX_AMOUNT_ML = 10_000

    /** The phone acknowledges watch events whose local date falls in the last this-many days. */
    const val ACK_WINDOW_DAYS = 7L

    fun intakePath(eventId: String): String = "$INTAKE_PATH_PREFIX/$eventId"

    fun isIntakePath(path: String): Boolean = path.startsWith("$INTAKE_PATH_PREFIX/")
}

/** A drink logged on the watch. [eventId] is a client-generated UUID used to de-duplicate on the phone. */
@Serializable
data class IntakeEvent(
    @SerialName("v") val protocolVersion: Int = WearSyncContract.PROTOCOL_VERSION,
    val eventId: String,
    val amountMl: Int,
    val timestampMillis: Long,
    /** ISO-8601 date (yyyy-MM-dd) in the time zone where the drink happened. */
    val localDate: String
)

/** Everything the watch needs to render today's progress. Written only by the phone. */
@Serializable
data class WatchState(
    @SerialName("v") val protocolVersion: Int = WearSyncContract.PROTOCOL_VERSION,
    val localDate: String,
    val totalMl: Int,
    val goalMl: Int,
    val presetsMl: List<Int>,
    /** `LiquidUnit` enum name; kept as a string so older watches tolerate new units. */
    val liquidUnit: String,
    /** Ids of watch events the phone has stored; the watch drops these from its pending queue. */
    val acknowledgedEventIds: List<String> = emptyList()
)
