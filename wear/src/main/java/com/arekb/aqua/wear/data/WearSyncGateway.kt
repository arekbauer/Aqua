package com.arekb.aqua.wear.data

import com.arekb.aqua.sync.IntakeEvent

interface WearSyncGateway {
    suspend fun publish(event: IntakeEvent)
}
