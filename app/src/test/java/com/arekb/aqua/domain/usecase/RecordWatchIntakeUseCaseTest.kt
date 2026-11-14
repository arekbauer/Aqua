package com.arekb.aqua.domain.usecase

import com.arekb.aqua.sync.IntakeEvent
import com.arekb.aqua.sync.WearSyncCodec
import com.arekb.aqua.sync.WearSyncContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RecordWatchIntakeUseCaseTest {
    private val now = Instant.parse("2026-10-31T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val repository = FakeWatchIntakeRepository()
    private val useCase = RecordWatchIntakeUseCase(repository, clock)

    private fun event(
        eventId: String = "event-1",
        amountMl: Int = 350,
        timestampMillis: Long = now.minusSeconds(60).toEpochMilli(),
        localDate: String = "2026-10-31"
    ) = IntakeEvent(
        eventId = eventId,
        amountMl = amountMl,
        timestampMillis = timestampMillis,
        localDate = localDate
    )

    @Test
    fun validEventIsStoredWithItsOriginalValues() = runTest {
        val event = event()

        assertEquals(WatchIntakeResult.STORED, useCase(event))

        assertEquals(
            listOf(StoredWatchEntry("event-1", 350, event.timestampMillis, "2026-10-31")),
            repository.stored
        )
    }

    @Test
    fun redeliveredEventIsReportedAsDuplicateAndStoredOnce() = runTest {
        assertEquals(WatchIntakeResult.STORED, useCase(event()))
        assertEquals(WatchIntakeResult.DUPLICATE, useCase(event()))

        assertEquals(1, repository.stored.size)
    }

    @Test
    fun differentEventIdsAreStoredSeparately() = runTest {
        useCase(event(eventId = "a"))
        useCase(event(eventId = "b"))

        assertEquals(listOf("a", "b"), repository.stored.map { it.externalId })
    }

    @Test
    fun invalidEventIdsAreRejected() = runTest {
        val invalidIds = listOf("", "   ", "has/slash", "has space", "x".repeat(WearSyncContract.MAX_EVENT_ID_LENGTH + 1))

        invalidIds.forEach { id ->
            assertEquals("id '$id'", WatchIntakeResult.REJECTED, useCase(event(eventId = id)))
        }
        assertEquals(emptyList<StoredWatchEntry>(), repository.stored)
    }

    @Test
    fun amountsOutsideTheAllowedRangeAreRejected() = runTest {
        listOf(0, -250, WearSyncContract.MAX_AMOUNT_ML + 1).forEach { amount ->
            assertEquals("amount $amount", WatchIntakeResult.REJECTED, useCase(event(amountMl = amount)))
        }
        assertEquals(
            WatchIntakeResult.STORED,
            useCase(event(eventId = "max", amountMl = WearSyncContract.MAX_AMOUNT_ML))
        )
    }

    @Test
    fun malformedDatesAreRejected() = runTest {
        listOf("", "31-10-2026", "2026-13-01", "2026-10-31T10:00").forEach { date ->
            assertEquals("date '$date'", WatchIntakeResult.REJECTED, useCase(event(localDate = date)))
        }
    }

    @Test
    fun implausibleTimestampsAreRejected() = runTest {
        assertEquals(WatchIntakeResult.REJECTED, useCase(event(timestampMillis = 0)))
        assertEquals(WatchIntakeResult.REJECTED, useCase(event(timestampMillis = -5)))
        // Far in the future (more than the tolerated clock skew).
        assertEquals(
            WatchIntakeResult.REJECTED,
            useCase(event(timestampMillis = now.plusSeconds(3 * 3600).toEpochMilli()))
        )
    }

    @Test
    fun localDateMustMatchTheTimestampWithinOneDayOfUtc() = runTest {
        // 23:30 UTC on Oct 30 is already Oct 31 in a zone such as UTC+2.
        val lateEvening = Instant.parse("2026-10-30T23:30:00Z").toEpochMilli()
        assertEquals(
            WatchIntakeResult.STORED,
            useCase(event(eventId = "tz", timestampMillis = lateEvening, localDate = "2026-10-31"))
        )
        assertEquals(
            WatchIntakeResult.REJECTED,
            useCase(event(eventId = "wrong-day", timestampMillis = lateEvening, localDate = "2026-10-20"))
        )
    }

    @Test
    fun rejectedEventsNeverReachTheRepository() = runTest {
        useCase(event(amountMl = 0))
        useCase(event(eventId = "bad id"))

        assertEquals(emptyList<StoredWatchEntry>(), repository.stored)
    }

    @Test
    fun encodedPayloadIsDecodedAndStored() = runTest {
        val payload = WearSyncCodec.encode(event(eventId = "from-payload"))

        assertEquals(WatchIntakeResult.STORED, useCase.fromPayload(payload))
        assertEquals("from-payload", repository.stored.single().externalId)
    }

    @Test
    fun missingOrMalformedPayloadsAreRejected() = runTest {
        assertEquals(WatchIntakeResult.REJECTED, useCase.fromPayload(null))
        assertEquals(WatchIntakeResult.REJECTED, useCase.fromPayload(ByteArray(0)))
        assertEquals(WatchIntakeResult.REJECTED, useCase.fromPayload("garbage".toByteArray()))
        assertEquals(emptyList<StoredWatchEntry>(), repository.stored)
    }
}
