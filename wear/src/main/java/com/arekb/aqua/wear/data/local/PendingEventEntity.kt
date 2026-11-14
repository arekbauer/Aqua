package com.arekb.aqua.wear.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arekb.aqua.sync.IntakeEvent

@Entity(tableName = "pending_events")
data class PendingEventEntity(
    @PrimaryKey val eventId: String,
    val amountMl: Int,
    val timestampMillis: Long,
    val localDate: String
)

fun PendingEventEntity.toEvent() = IntakeEvent(
    eventId = eventId,
    amountMl = amountMl,
    timestampMillis = timestampMillis,
    localDate = localDate
)

fun IntakeEvent.toEntity() = PendingEventEntity(
    eventId = eventId,
    amountMl = amountMl,
    timestampMillis = timestampMillis,
    localDate = localDate
)
