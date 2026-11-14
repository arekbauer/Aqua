package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.repository.WatchStateSink
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Keeps the watch snapshot up to date for as long as the given scope is alive. */
class WatchStatePublisher @Inject constructor(
    private val observeWatchState: ObserveWatchStateUseCase,
    private val sink: WatchStateSink
) {
    fun start(scope: CoroutineScope): Job = scope.launch {
        observeWatchState().collect { state ->
            try {
                sink.publish(state)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // A failed publish must not stop later updates; the sink is responsible for logging.
            }
        }
    }
}
