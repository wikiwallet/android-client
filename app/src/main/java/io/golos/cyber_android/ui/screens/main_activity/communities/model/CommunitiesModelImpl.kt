package io.golos.cyber_android.ui.screens.main_activity.communities.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.golos.cyber_android.ui.common.mvvm.model.ModelBaseImpl
import io.golos.cyber_android.ui.common.recycler_view.versioned.VersionedListItem
import io.golos.cyber_android.ui.screens.main_activity.communities.dto.CommunityListItem
import io.golos.cyber_android.ui.screens.main_activity.communities.dto.LoadingListItem
import io.golos.cyber_android.ui.screens.main_activity.communities.dto.RetryListItem
import io.golos.domain.DispatchersProvider
import io.golos.domain.dto.CommunityDomain
import io.golos.domain.use_cases.community.CommunitiesRepository
import io.golos.domain.utils.IdUtil
import io.golos.domain.utils.MurmurHash
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

open class CommunitiesModelImpl
@Inject
constructor(
    private val communitiesRepository: CommunitiesRepository,
    private val dispatchersProvider: DispatchersProvider
) : ModelBaseImpl(), CommunitiesModel {

    private enum class LoadingState {
        READY_TO_LOAD,
        LOADING,
        ALL_DATA_LOADED,
        IN_ERROR
    }

    private var currentLoadingState = LoadingState.READY_TO_LOAD

    private var subscribingInProgress = false

    private var loadedItems: MutableList<VersionedListItem> = mutableListOf()

    private val _items = MutableLiveData<List<VersionedListItem>>(listOf())

    protected open val showUserCommunityOnly: Boolean = false

    override val items: LiveData<List<VersionedListItem>>
        get() = _items

    override val pageSize = 25

    override suspend fun loadPage() {
        when(currentLoadingState) {
            LoadingState.READY_TO_LOAD -> loadData(false)

            LoadingState.LOADING,
            LoadingState.IN_ERROR,
            LoadingState.ALL_DATA_LOADED -> { /* do nothing */ }
        }
    }

    override suspend fun retry() {
        when(currentLoadingState) {
            LoadingState.IN_ERROR -> loadData(true)

            LoadingState.LOADING,
            LoadingState.READY_TO_LOAD,
            LoadingState.ALL_DATA_LOADED -> { /* do nothing */ }
        }
    }

    override suspend fun subscribeUnsubscribe(communityId: String): Boolean {
        if(subscribingInProgress) {
            return true
        }

        subscribingInProgress = true

        val community = loadedItems[getCommunityIndex(communityId)] as CommunityListItem

        setCommunityInProgress(communityId)

        val isSuccess = withContext(dispatchersProvider.ioDispatcher) {
            try {
                if(community.isJoined) {
                    communitiesRepository.unsubscribeToCommunity(communityId)
                } else {
                    communitiesRepository.subscribeToCommunity(communityId)
                }
                true
            } catch (ex: Exception) {
                Timber.e(ex)
                false
            }
        }

        completeCommunityInProgress(communityId, isSuccess)

        subscribingInProgress = false

        return isSuccess
    }

    protected fun CommunityDomain.map() = CommunityListItem(MurmurHash.hash64(this.communityId), 0, this, this.isSubscribed, false)

    private suspend fun loadData(isRetry: Boolean) {
        currentLoadingState = LoadingState.LOADING

        if(isRetry) {
            replaceRetryByLoading()
        } else {
            addLoading()
        }

        val data = getData(loadedItems.size-1)

        currentLoadingState = if(data != null) {
            addLoadedData(data)

            if(data.size < pageSize) LoadingState.ALL_DATA_LOADED else LoadingState.READY_TO_LOAD
        } else {
            replaceLoadingByRetry()
            LoadingState.IN_ERROR
        }
    }

    private suspend fun getData(offset: Int): List<CommunityListItem>? =
        withContext(dispatchersProvider.ioDispatcher) {
            delay(500)

            try {
                communitiesRepository
                    .getCommunitiesList(offset, pageSize, showUserCommunityOnly)
                    .map { rawItem -> rawItem.map() }
            } catch (ex: Exception) {
                Timber.e(ex)
                null
            }
        }

    private fun updateData(updateAction: (MutableList<VersionedListItem>) -> Unit) {
        updateAction(loadedItems)
        _items.value = loadedItems
    }

    private fun addLoading() = updateData { loadedItems.add(LoadingListItem(IdUtil.generateLongId(), 0)) }

    private fun replaceRetryByLoading() = updateData {
        loadedItems[loadedItems.lastIndex] = LoadingListItem(IdUtil.generateLongId(), 0)
    }

    private fun replaceLoadingByRetry() = updateData {
        loadedItems[loadedItems.lastIndex] = RetryListItem(IdUtil.generateLongId(), 0)
    }

    private fun addLoadedData(data: List<CommunityListItem>) = updateData {
        loadedItems.removeAt(loadedItems.lastIndex)
        loadedItems.addAll(data)
    }

    private fun setCommunityInProgress(communityId: String) =
        updateCommunity(communityId) { oldCommunity ->
            oldCommunity.copy(version = oldCommunity.version + 1, isProgress = true)
        }

    private fun completeCommunityInProgress(communityId: String, isSuccess: Boolean) =
        updateCommunity(communityId) { oldCommunity ->
            oldCommunity.copy(
                version = oldCommunity.version + 1,
                isProgress = false,
                isJoined = if(isSuccess) !oldCommunity.isJoined else oldCommunity.isJoined
            )
        }

    private fun updateCommunity(communityId: String, updateAction: (CommunityListItem) -> CommunityListItem) = updateData {
        val itemIndex = getCommunityIndex(communityId)
        loadedItems[itemIndex] = updateAction(loadedItems[itemIndex] as CommunityListItem)
    }

    private fun getCommunityIndex(communityId: String) =
        loadedItems.indexOfFirst { it is CommunityListItem && it.community.communityId == communityId }
}