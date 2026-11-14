package com.arekb.aqua.wear.domain

import kotlinx.coroutines.flow.Flow

interface WatchHydrationRepository {
    val todayState: Flow<TodayState>
}
