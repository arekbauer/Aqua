package com.arekb.aqua.wear.data

import android.content.Context
import com.arekb.aqua.sync.IntakeEvent
import com.arekb.aqua.sync.WearSyncCodec
import com.arekb.aqua.sync.WearSyncContract
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearSyncGatewayImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WearSyncGateway {

    private val dataClient = Wearable.getDataClient(context)

    override suspend fun publish(event: IntakeEvent) {
        val request = com.google.android.gms.wearable.PutDataRequest.create(WearSyncContract.intakePath(event.eventId))
        request.data = WearSyncCodec.encode(event)
        request.setUrgent()
        dataClient.putDataItem(request).await()
    }
}
