package com.arekb.aqua.wear.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.sync.WatchState
import com.arekb.aqua.sync.WearSyncCodec
import com.arekb.aqua.sync.WearSyncContract
import com.arekb.aqua.wear.data.local.WatchDao
import com.arekb.aqua.wear.domain.TodayState
import com.arekb.aqua.wear.domain.WatchHydrationRepository
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHydrationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val watchDao: WatchDao,
    private val clock: Clock
) : WatchHydrationRepository {

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("watch_sync_prefs", Context.MODE_PRIVATE)

    private val watchStateFlow: Flow<WatchState?> = callbackFlow {
        val cached = prefs.getString(PREF_CACHED_STATE, null)
        val initial = cached?.toByteArray(Charsets.UTF_8)?.let { WearSyncCodec.decodeState(it) }
        trySend(initial)

        val listener = DataClient.OnDataChangedListener { dataEvents ->
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == WearSyncContract.STATE_PATH) {
                    val data = event.dataItem.data
                    if (data != null) {
                        val state = WearSyncCodec.decodeState(data)
                        if (state != null) {
                            prefs.edit().putString(PREF_CACHED_STATE, String(data, Charsets.UTF_8)).apply()
                            trySend(state)
                        }
                    }
                }
            }
        }
        dataClient.addListener(listener).await()

        try {
            val uri = Uri.Builder().scheme("wear").path(WearSyncContract.STATE_PATH).build()
            val item = dataClient.getDataItem(uri).await()
            if (item != null && item.data != null) {
                val state = WearSyncCodec.decodeState(item.data!!)
                if (state != null) {
                    prefs.edit().putString(PREF_CACHED_STATE, String(item.data!!, Charsets.UTF_8)).apply()
                    trySend(state)
                }
            }
        } catch(e: Exception) {
            // Ignore
        }

        awaitClose {
            dataClient.removeListener(listener)
        }
    }

    override val todayState: Flow<TodayState> = combine(
        watchStateFlow,
        watchDao.getPendingEvents()
    ) { watchState, pendingEvents ->
        val localDate = LocalDate.now(clock).toString()

        val acknowledged = watchState?.acknowledgedEventIds.orEmpty()
        val toDelete = pendingEvents.filter { it.eventId in acknowledged }.map { it.eventId }
        if (toDelete.isNotEmpty()) {
            watchDao.deleteEvents(toDelete)
        }

        val activePending = pendingEvents.filter { it.eventId !in acknowledged && it.localDate == localDate }
        val pendingTotal = activePending.sumOf { it.amountMl }

        if (watchState == null || watchState.localDate != localDate) {
            TodayState(
                totalMl = pendingTotal,
                goalMl = 2000,
                presetsMl = listOf(200, 350, 500),
                liquidUnit = LiquidUnit.ML,
                isSyncNeeded = true
            )
        } else {
            val unit = runCatching { LiquidUnit.valueOf(watchState.liquidUnit) }.getOrDefault(LiquidUnit.ML)
            TodayState(
                totalMl = watchState.totalMl + pendingTotal,
                goalMl = watchState.goalMl,
                presetsMl = watchState.presetsMl,
                liquidUnit = unit,
                isSyncNeeded = false
            )
        }
    }

    companion object {
        private const val PREF_CACHED_STATE = "cached_watch_state"
    }
}
