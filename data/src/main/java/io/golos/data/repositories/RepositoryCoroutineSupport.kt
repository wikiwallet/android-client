package io.golos.data.repositories

import io.golos.data.network_state.NetworkStateChecker
import io.golos.domain.DispatchersProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


abstract class RepositoryCoroutineSupport(
    private val dispatchersProvider: DispatchersProvider,
    private val networkStateChecker: NetworkStateChecker
) : RepositoryBase(dispatchersProvider, networkStateChecker), CoroutineScope{

    private val scopeJob: Job = SupervisorJob()

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }


    /**
     * Context of this scope.
     */
    override val coroutineContext: CoroutineContext = scopeJob + dispatchersProvider.ioDispatcher + errorHandler

}