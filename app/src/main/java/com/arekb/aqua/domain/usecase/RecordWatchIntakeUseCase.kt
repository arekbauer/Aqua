package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.repository.WatchIntakeRepository
import com.arekb.aqua.sync.IntakeEvent
import com.arekb.aqua.sync.WearSyncCodec
import com.arekb.aqua.sync.WearSyncContract
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class WatchIntakeResult {
    /** A new entry was created. */
    STORED,

    /** The event had already been stored; nothing changed. */
    DUPLICATE,

    /** The payload was malformed or failed validation and was ignored. */
    REJECTED
}

/**
 * Validates a drink logged on the watch and stores it exactly once (de-duplicated by event id).
 * Mirrors the sanitising done for backup imports: positive amounts and well-formed dates only.
 */
class RecordWatchIntakeUseCase(
    private val repository: WatchIntakeRepository,
    private val clock: Clock
) {
    suspend fun fromPayload(payload: ByteArray?): WatchIntakeResult {
        val event = WearSyncCodec.decodeEvent(payload) ?: return WatchIntakeResult.REJECTED
        return invoke(event)
    }

    suspend operator fun invoke(event: IntakeEvent): WatchIntakeResult {
        if (!isValid(event)) return WatchIntakeResult.REJECTED
        val stored = repository.addIntakeFromWatch(
            externalId = event.eventId,
            amountMl = event.amountMl,
            timestampMillis = event.timestampMillis,
            localDate = event.localDate
        )
        return if (stored) WatchIntakeResult.STORED else WatchIntakeResult.DUPLICATE
    }

    private fun isValid(event: IntakeEvent): Boolean {
        val id = event.eventId
        if (id.isBlank() || id.length > WearSyncContract.MAX_EVENT_ID_LENGTH) return false
        // The id becomes part of a DataItem path, so keep it free of separators and whitespace.
        if (id.any { it == '/' || it.isWhitespace() }) return false
        if (event.amountMl !in 1..WearSyncContract.MAX_AMOUNT_ML) return false

        val localDate = try {
            LocalDate.parse(event.localDate)
        } catch (_: DateTimeParseException) {
            return false
        }

        val nowMillis = clock.instant().toEpochMilli()
        if (event.timestampMillis <= 0 || event.timestampMillis > nowMillis + MAX_FUTURE_SKEW_MILLIS) return false

        // A local date can differ from the UTC date by at most one day (largest zone offset is 14h).
        val utcDate = Instant.ofEpochMilli(event.timestampMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return abs(ChronoUnit.DAYS.between(utcDate, localDate)) <= 1
    }

    private companion object {
        /** Tolerated clock difference between watch and phone. */
        const val MAX_FUTURE_SKEW_MILLIS = 60 * 60 * 1000L
    }
}
