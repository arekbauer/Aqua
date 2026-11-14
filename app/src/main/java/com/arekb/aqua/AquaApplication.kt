package com.arekb.aqua

import android.app.Application
import com.arekb.aqua.domain.usecase.WatchStatePublisher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class AquaApplication : Application() {
    @Inject
    lateinit var watchStatePublisher: WatchStatePublisher

    /** Lives as long as the process; owns work that must continue without any UI (watch sync). */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate() // Hilt injects dependencies here.
        watchStatePublisher.start(applicationScope)
    }
}
