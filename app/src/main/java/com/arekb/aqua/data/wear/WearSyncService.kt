package com.arekb.aqua.data.wear

import android.util.Log
import com.arekb.aqua.domain.usecase.RecordWatchIntakeUseCase
import com.arekb.aqua.domain.usecase.WatchIntakeResult
import com.arekb.aqua.sync.WearSyncContract
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Receives drinks logged on the watch (`/aqua/intake/{eventId}`), stores them, and removes the
 * handled DataItem. The refreshed snapshot (with the event acknowledged) is published by
 * `WatchStatePublisher`, which runs for the lifetime of the application process.
 */
@AndroidEntryPoint
class WearSyncService : WearableListenerService() {
    @Inject
    lateinit var recordWatchIntake: RecordWatchIntakeUseCase

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Buffers are only valid during this callback, so copy the events out first.
        val events = dataEvents.map { it.freeze() }
        runBlocking(Dispatchers.IO) {
            events
                .filter { it.type == DataEvent.TYPE_CHANGED }
                .filter { WearSyncContract.isIntakePath(it.dataItem.uri.path.orEmpty()) }
                .forEach { event -> handle(event) }
        }
    }

    private suspend fun handle(event: DataEvent) {
        val uri = event.dataItem.uri
        val result = try {
            recordWatchIntake.fromPayload(event.dataItem.data)
        } catch (error: Exception) {
            // Leave the item in place so a later delivery can retry (e.g. a transient database error).
            Log.w(TAG, "Failed to store watch event at $uri", error)
            return
        }
        if (result == WatchIntakeResult.REJECTED) Log.w(TAG, "Rejected invalid watch event at $uri")
        // Every outcome is terminal: stored, already stored, or invalid and never going to be valid.
        try {
            Wearable.getDataClient(this).deleteDataItems(uri).await()
        } catch (error: Exception) {
            Log.w(TAG, "Could not delete handled watch event at $uri", error)
        }
    }

    private companion object {
        const val TAG = "WearSyncService"
    }
}
