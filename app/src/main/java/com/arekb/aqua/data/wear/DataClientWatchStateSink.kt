package com.arekb.aqua.data.wear

import android.content.Context
import android.util.Log
import com.arekb.aqua.domain.repository.WatchStateSink
import com.arekb.aqua.sync.WatchState
import com.arekb.aqua.sync.WearSyncCodec
import com.arekb.aqua.sync.WearSyncContract
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Publishes the watch snapshot as a single Wear Data Layer item (unchanged payloads are not re-sent). */
class DataClientWatchStateSink @Inject constructor(
    @ApplicationContext private val context: Context
) : WatchStateSink {
    private val dataClient by lazy { Wearable.getDataClient(context) }

    override suspend fun publish(state: WatchState) {
        try {
            val request = PutDataRequest.create(WearSyncContract.STATE_PATH)
                .setData(WearSyncCodec.encode(state))
                .setUrgent()
            dataClient.putDataItem(request).await()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            // Typically Play services / the Wearable API is unavailable (e.g. no watch support on this phone).
            Log.w(TAG, "Could not publish watch state", error)
        }
    }

    private companion object {
        const val TAG = "WatchStateSink"
    }
}
