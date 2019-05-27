package io.golos.cyber_android.ui.screens.notifications

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.golos.domain.interactors.model.UpdateOption
import io.golos.domain.interactors.notifs.events.EventsUseCase
import io.golos.domain.map
import io.golos.domain.requestmodel.EventModel
import io.golos.domain.requestmodel.EventsListModel
import io.golos.domain.requestmodel.QueryResult


class NotificationsViewModel(private val eventsUseCase: EventsUseCase): ViewModel() {
    companion object {
        const val PAGE_SIZE = 20
    }

    /**
     * [LiveData] that indicates if data is loading
     */
    val loadingStatusLiveData = eventsUseCase.getUpdatingState.map(Function<QueryResult<UpdateOption>, Boolean> {
        it is QueryResult.Loading
    })

    val readinessLiveData = eventsUseCase.getReadinessLiveData

    /**
     * [LiveData] there was an error during loading
     */
    val errorStatusLiveData = eventsUseCase.getUpdatingState.map(Function<QueryResult<UpdateOption>, Boolean> {
        it is QueryResult.Error
    })

    /**
     * [LiveData] that indicates if last page was reached
     */
    val lastPageLiveData = eventsUseCase.getLastFetchedChunk.map(Function<EventsListModel, Boolean> {
        it.size % PAGE_SIZE != 0 || it.isEmpty()
    })


    /**
     * [LiveData] of all the [EventModel] items
     */
    val feedLiveData = eventsUseCase.getAsLiveData

    init {
        eventsUseCase.subscribe()
        requestRefresh()
    }

    /**
     * Requests full refresh of the data. New data can be listened by [feedLiveData]
     */
    fun requestRefresh() {
        eventsUseCase.requestUpdate(PAGE_SIZE, UpdateOption.REFRESH_FROM_BEGINNING)
    }

    /**
     * Requests new page of the data. New data can be listened by [feedLiveData]
     */
    fun loadMore() {
        eventsUseCase.requestUpdate(PAGE_SIZE, UpdateOption.FETCH_NEXT_PAGE)
    }

    override fun onCleared() {
        super.onCleared()
        eventsUseCase.unsubscribe()
    }
}