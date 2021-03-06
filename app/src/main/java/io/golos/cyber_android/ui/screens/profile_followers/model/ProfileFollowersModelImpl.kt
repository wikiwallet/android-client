package io.golos.cyber_android.ui.screens.profile_followers.model

import androidx.lifecycle.LiveData
import io.golos.cyber_android.ui.dto.FollowersFilter
import io.golos.cyber_android.ui.screens.profile_followers.model.lists_workers.UsersListWorker
import io.golos.cyber_android.ui.shared.mvvm.model.ModelBaseImpl
import io.golos.cyber_android.ui.shared.recycler_view.versioned.VersionedListItem
import io.golos.domain.dependency_injection.Clarification
import io.golos.domain.dto.ErrorInfoDomain
import io.golos.domain.dto.UserIdDomain
import io.golos.domain.repositories.CurrentUserRepositoryRead
import io.golos.domain.repositories.UsersRepository
import javax.inject.Inject
import javax.inject.Named

class ProfileFollowersModelImpl
@Inject
constructor(
    private val profileUserId: UserIdDomain,
    private val currentUserRepository: CurrentUserRepositoryRead,
    @Named(Clarification.PAGE_SIZE)
    override val pageSize: Int,
    @Named(Clarification.FOLLOWERS)
    private val followersListWorker: UsersListWorker,
    @Named(Clarification.FOLLOWING)
    private val followingListWorker: UsersListWorker,
    @Named(Clarification.MUTUAL)
    private val mutualListWorker: UsersListWorker,
    private val usersRepository: UsersRepository
) : ProfileFollowersModel,
    ModelBaseImpl() {

    override val isCurrentUser: Boolean
        get() = profileUserId == currentUserRepository.userId

    override fun getItems(filter: FollowersFilter): LiveData<List<VersionedListItem>> = getWorker(filter).items

    override suspend fun loadPage(filter: FollowersFilter) = getWorker(filter).loadPage()

    override suspend fun retry(filter: FollowersFilter) = getWorker(filter).retry()

    /**
     * @return true in case of success
     */
    override suspend fun subscribeUnsubscribe(userId: UserIdDomain, filter: FollowersFilter): ErrorInfoDomain? {
        val errorInfo = getWorker(filter).subscribeUnsubscribe(userId)

        // Sync action to others list
        if(errorInfo == null) {
            getOppositeWorkers(filter).forEach { it.subscribeUnsubscribeInstant(userId) }
        }

        return errorInfo
    }

    override suspend fun getUserName(): String = usersRepository.getUserProfile(profileUserId).name

    private fun getWorker(filter: FollowersFilter): UsersListWorker =
        when(filter) {
            FollowersFilter.FOLLOWINGS -> followingListWorker
            FollowersFilter.FOLLOWERS -> followersListWorker
            FollowersFilter.MUTUALS -> mutualListWorker
        }

    private fun getOppositeWorkers(filter: FollowersFilter): List<UsersListWorker> =
        when(filter) {
            FollowersFilter.FOLLOWINGS -> listOf(followersListWorker, mutualListWorker)
            FollowersFilter.FOLLOWERS -> listOf(followingListWorker, mutualListWorker)
            FollowersFilter.MUTUALS -> listOf(followersListWorker, followingListWorker)
        }
}