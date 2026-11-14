package com.arekb.aqua.domain.repository

import com.arekb.aqua.sync.WatchState

/**
 * Delivers [WatchState] snapshots to the paired watch. The only implementation that touches
 * Google Play services is `DataClientWatchStateSink`, which keeps the rest of the app transport-agnostic.
 */
interface WatchStateSink {
    /** Best effort: implementations must not throw for an unavailable or absent watch. */
    suspend fun publish(state: WatchState)
}
