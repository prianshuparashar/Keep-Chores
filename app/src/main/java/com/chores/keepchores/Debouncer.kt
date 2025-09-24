package com.chores.keepchores

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Generic debouncer: only the last submitted value within [waitMs] triggers the block.
 * Example:
 * val debouncer = Debouncer<String>(lifecycleScope, 300)
 * textWatcher.afterTextChanged { debouncer.submit(it) { query -> performSearch(query) } }
 */
class Debouncer<T>(
    private val scope: CoroutineScope,
    private val waitMs: Long,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private var job: Job? = null

    fun submit(value: T, block: (T) -> Unit) {
        job?.cancel()
        job = scope.launch(dispatcher) {
            delay(waitMs)
            block(value)
        }
    }

    fun cancel() { job?.cancel() }
}