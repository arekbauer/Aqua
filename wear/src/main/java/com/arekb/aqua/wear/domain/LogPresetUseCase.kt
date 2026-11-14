package com.arekb.aqua.wear.domain

import com.arekb.aqua.sync.IntakeEvent
import com.arekb.aqua.wear.data.WearSyncGateway
import com.arekb.aqua.wear.data.local.PendingEventEntity
import com.arekb.aqua.wear.data.local.WatchDao
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class LogPresetUseCase @Inject constructor(
    private val clock: Clock,
    private val syncGateway: WearSyncGateway,
    private val watchDao: WatchDao
) {
    suspend operator fun invoke(amountMl: Int) {
        val event = IntakeEvent(
            eventId = UUID.randomUUID().toString(),
            amountMl = amountMl,
            timestampMillis = clock.millis(),
            localDate = LocalDate.now(clock).toString()
        )
        
        watchDao.insertEvent(
            PendingEventEntity(
                eventId = event.eventId,
                amountMl = event.amountMl,
                timestampMillis = event.timestampMillis,
                localDate = event.localDate
            )
        )
        
        syncGateway.publish(event)
    }
}
