package io.golos.cyber_android.ui.screens.ftue_search_community.viewmodel

import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.mvvm.viewModel.ViewModelBase
import io.golos.cyber_android.ui.common.mvvm.view_commands.SetLoadingVisibilityCommand
import io.golos.cyber_android.ui.common.mvvm.view_commands.ShowMessageCommand
import io.golos.cyber_android.ui.common.paginator.Paginator
import io.golos.cyber_android.ui.dto.Community
import io.golos.cyber_android.ui.mappers.mapToCollectionListItem
import io.golos.cyber_android.ui.mappers.mapToCommunityList
import io.golos.cyber_android.ui.mappers.mapToCommunityListItem
import io.golos.cyber_android.ui.screens.ftue_search_community.model.FtueItemListModelEventProcessor
import io.golos.cyber_android.ui.screens.ftue_search_community.model.FtueSearchCommunityModel
import io.golos.cyber_android.ui.screens.ftue_search_community.model.item.collection.CommunityCollection
import io.golos.cyber_android.ui.screens.ftue_search_community.model.item.collection.FtueCommunityCollectionListItem
import io.golos.cyber_android.ui.screens.ftue_search_community.model.item.community.FtueCommunityListItem
import io.golos.cyber_android.ui.utils.PAGINATION_PAGE_SIZE
import io.golos.cyber_android.ui.utils.toLiveData
import io.golos.domain.DispatchersProvider
import io.golos.domain.utils.IdUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FtueSearchCommunityViewModel @Inject constructor(
    dispatchersProvider: DispatchersProvider,
    model: FtueSearchCommunityModel,
    private val paginator: Paginator.Store<Community>
) : ViewModelBase<FtueSearchCommunityModel>(dispatchersProvider, model), FtueItemListModelEventProcessor {

    private var loadCommunitiesJob: Job? = null

    private val _communityListState: MutableLiveData<Paginator.State> = MutableLiveData(Paginator.State.Empty)

    private var communitiesSearchQuery: String? = null

    val communityListState = _communityListState.toLiveData()

    private val communitiesSubscriptions = mutableListOf<Community>()

    private val _collectionListState: MutableLiveData<List<FtueCommunityCollectionListItem>> = MutableLiveData(
        listOf(
            CommunityCollection().mapToCollectionListItem(),
            CommunityCollection().mapToCollectionListItem(),
            CommunityCollection().mapToCollectionListItem()
        )
    )

    val collectionListState = _collectionListState.toLiveData()

    private val _haveNeedCollections: MutableLiveData<Boolean> = MutableLiveData(false)

    val haveNeedCollection = _haveNeedCollections.toLiveData()

    init {
        paginator.sideEffectListener = { sideEffect ->
            when (sideEffect) {
                is Paginator.SideEffect.LoadPage -> loadCommunities(sideEffect.pageCount)
                is Paginator.SideEffect.ErrorEvent -> {
                }
            }
        }
        paginator.render = { state ->
            _communityListState.value = state
        }

        loadInitialCommunities()
    }

    override fun onFollowToCommunity(community: Community) {
        launch {
            try {
                _command.value = SetLoadingVisibilityCommand(true)
                model.onFollowToCommunity(community.communityId)
                val updatedCommunity = community.copy(isSubscribed = true)
                changeFollowingStatus(updatedCommunity)
                addCommunityToCollection(community)
            } catch (e: Exception) {
                Timber.e(e)
                _command.value = ShowMessageCommand(R.string.loading_error)
            } finally {
                _command.value = SetLoadingVisibilityCommand(false)
            }
        }
    }

    override fun onUnFollowFromCommunity(community: Community) {
        launch {
            try {
                _command.value = SetLoadingVisibilityCommand(true)
                model.onUnFollowFromCommunity(community.communityId)
                val updatedCommunity = community.copy(isSubscribed = false)
                changeFollowingStatus(updatedCommunity)
                removeCommunityFromCollection(community)
            } catch (e: Exception) {
                Timber.e(e)
                _command.value = ShowMessageCommand(R.string.loading_error)
            } finally {
                _command.value = SetLoadingVisibilityCommand(false)
            }
        }
    }


    private fun changeFollowingStatus(community: Community) {
        val state: Paginator.State? = _communityListState.value
        val updatedState = updateFollowingStatusInState(state, community)
        _communityListState.value = updatedState
    }

    private fun updateFollowingStatusInState(state: Paginator.State?, community: Community): Paginator.State? {
        when (state) {
            is Paginator.State.Data<*> -> {
                val communitiesList = state.data as ArrayList<FtueCommunityListItem>
                val findCommunityItem = communitiesList.find { it.community.name == community.name }
                findCommunityItem?.let {
                    val index = communitiesList.indexOf(it)
                    communitiesList[index] = FtueCommunityListItem(community, it.id)
                }

            }
            is Paginator.State.Refresh<*> -> {
                val communitiesList = state.data as ArrayList<FtueCommunityListItem>
                val findCommunityItem = communitiesList.find { it.community.name == community.name }
                findCommunityItem?.let {
                    val index = communitiesList.indexOf(it)
                    communitiesList[index] = FtueCommunityListItem(community, it.id)
                }
            }
            is Paginator.State.NewPageProgress<*> -> {
                val communitiesList = state.data as ArrayList<FtueCommunityListItem>
                val findCommunityItem = communitiesList.find { it.community.name == community.name }
                findCommunityItem?.let {
                    val index = communitiesList.indexOf(it)
                    communitiesList[index] = FtueCommunityListItem(community, it.id)
                }
            }
            is Paginator.State.FullData<*> -> {
                val communitiesList = state.data as ArrayList<FtueCommunityListItem>
                val findCommunityItem = communitiesList.find { it.community.name == community.name }
                findCommunityItem?.let {
                    val index = communitiesList.indexOf(it)
                    communitiesList[index] = FtueCommunityListItem(community, it.id)
                }
            }
        }
        return state
    }

    override fun onRetryLoadCommunity() {
        loadInitialCommunities()
    }

    override fun removeCommunityFromCollection(community: Community) {
        val communityForDelete = communitiesSubscriptions.find { it.communityId == community.communityId }
        communitiesSubscriptions.remove(communityForDelete)
        val collectionsItems = _collectionListState.value
        val communitiesCollection: MutableList<FtueCommunityCollectionListItem> = arrayListOf()
        for (i in 0 until MAX_COLLECTION_LIST_SIZE) {
            if (communitiesSubscriptions.size - 1 >= i) {
                val collection = CommunityCollection(communitiesSubscriptions[i])
                val finCommunityCollectionListItem =
                    collectionsItems?.find { ftueCommunityCollectionListItem -> ftueCommunityCollectionListItem.collection == collection }
                val id: Long = finCommunityCollectionListItem?.id ?: IdUtil.generateLongId()
                communitiesCollection.add(collection.mapToCollectionListItem(id))
            } else {
                val id: Long = collectionsItems?.get(i)?.id ?: IdUtil.generateLongId()
                communitiesCollection.add(CommunityCollection().mapToCollectionListItem(id))
            }
        }
        _collectionListState.value = communitiesCollection
        updateStateOfNextButton()
    }

    fun loadMoreCommunities() {
        paginator.proceed(Paginator.Action.LoadMore)
    }

    private fun addCommunityToCollection(community: Community) {
        communitiesSubscriptions.add(community)
        val collectionsItems = _collectionListState.value
        val communitiesCollection: MutableList<FtueCommunityCollectionListItem> = arrayListOf()
        for (i in 0 until MAX_COLLECTION_LIST_SIZE) {
            if (communitiesSubscriptions.size - 1 >= i) {
                val collection = CommunityCollection(communitiesSubscriptions[i])
                val finCommunityCollectionListItem =
                    collectionsItems?.find { ftueCommunityCollectionListItem -> ftueCommunityCollectionListItem.collection == collection }
                val id: Long = finCommunityCollectionListItem?.id ?: IdUtil.generateLongId()
                communitiesCollection.add(collection.mapToCollectionListItem(id))
            } else {
                val id: Long = collectionsItems?.get(i)?.id ?: IdUtil.generateLongId()
                communitiesCollection.add(CommunityCollection().mapToCollectionListItem(id))
            }
        }
        _collectionListState.value = communitiesCollection
        updateStateOfNextButton()
    }

    private fun updateStateOfNextButton() {
        val items = _collectionListState.value as MutableList<FtueCommunityCollectionListItem>
        val sizeOfHaveCollection = items.filter { it.collection.community != null }.size == MAX_COLLECTION_LIST_SIZE
        _haveNeedCollections.value = sizeOfHaveCollection
    }

    private fun loadCommunities(pageCount: Int) {
        loadCommunitiesJob = launch {
            try {
                val communityDomain = model.getCommunities(
                    communitiesSearchQuery,
                    offset = pageCount * PAGINATION_PAGE_SIZE,
                    pageSize = PAGINATION_PAGE_SIZE
                )
                val communitiesList = communityDomain
                    .mapToCommunityList()
                    .mapToCommunityListItem()
                launch(Dispatchers.Main) {
                    paginator.proceed(Paginator.Action.NewPage(pageCount, communitiesList))
                }
            } catch (e: Exception) {
                Timber.e(e)
                paginator.proceed(Paginator.Action.PageError(e))
            }
        }
    }

    override fun onCleared() {
        loadCommunitiesJob?.cancel()
        super.onCleared()
    }

    private fun restartLoadCommunities() {
        loadCommunitiesJob?.cancel()
        paginator.proceed(Paginator.Action.Restart)
    }

    private fun loadInitialCommunities() {
        val postsListState = _communityListState.value
        if (postsListState is Paginator.State.Empty || postsListState is Paginator.State.EmptyError) {
            restartLoadCommunities()
        }
    }

    fun onCommunitiesSearchQueryChanged(query: String) {
        if (communitiesSearchQuery != query) {
            communitiesSearchQuery = query
            loadCommunitiesJob?.cancel()
            paginator.proceed(Paginator.Action.Search)
        }
    }

    fun sendCommunitiesCollection() {
        launch {
            val communitiyIds = _collectionListState.value!!
                .filter { it.collection.community != null }
                .map { it.collection.community?.communityId }
            try {
                model.sendCommunitiesCollection(communitiyIds as List<String>)
                _command.value = SetLoadingVisibilityCommand(true)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private companion object {

        private const val MAX_COLLECTION_LIST_SIZE = 3
    }
}