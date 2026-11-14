package com.arekb.aqua.domain.repository

import kotlinx.coroutines.flow.Flow

/** Storage operations needed by the Wear OS sync, kept separate from [HydrationRepository]. */
interface WatchIntakeRepository {
    /**
     * Stores a drink recorded on the watch.
     *
     * @return true if a new entry was created, false if [externalId] was already stored.
     */
    suspend fun addIntakeFromWatch(
        externalId: String,
        amountMl: Int,
        timestampMillis: Long,
        localDate: String
    ): Boolean

    /** External ids of watch entries whose local date is on or after [startLocalDate]. */
    fun observeWatchEventIdsSince(startLocalDate: String): Flow<List<String>>
}
