package io.golos.cyber_android.ui.screens.profile

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import io.golos.cyber4j.model.CyberName
import io.golos.cyber_android.ui.screens.profile.edit.BaseEditProfileViewModel
import io.golos.domain.interactors.model.UserAuthState
import io.golos.domain.interactors.model.UserMetadataModel
import io.golos.domain.interactors.sign.SignInUseCase
import io.golos.domain.interactors.user.UserMetadataUseCase
import io.golos.domain.map
import io.golos.domain.requestmodel.QueryResult

class ProfileViewModel(
    private val userMetadataUseCase: UserMetadataUseCase,
    private val signInUseCase: SignInUseCase,
    internal val forUser: CyberName
) : BaseEditProfileViewModel(userMetadataUseCase) {

    data class Profile(val metadata: UserMetadataModel, val isMyUser: Boolean) {
        companion object {
            val EMPTY = Profile(UserMetadataModel.empty, false)
        }
    }

    /**
     * [LiveData] that indicates if profile of this view model is the actual profile of an app user
     */
    private val getMyUserLiveData = signInUseCase.getAsLiveData.map(Function<UserAuthState, Boolean> {
        it.userName == forUser
    })

    /**
     * [LiveData] for profile
     */
    private val profileLiveData = MediatorLiveData<QueryResult<Profile>>().apply {
        var metadata: QueryResult<UserMetadataModel>? = null
        var isMyUser: Boolean? = null
        addSource(getMetadataLiveData) { metadataResult ->
            metadata = metadataResult
            isMyUser?.let {isMyUserResult ->
                postProfileData(metadataResult, isMyUserResult)
            }
        }

        addSource(getMyUserLiveData) {isMyUserResult ->
            isMyUser = isMyUserResult
            metadata?.let { metadataResult ->
                postProfileData(metadataResult, isMyUserResult)
            }
        }
    }

    val getProfileLiveData: LiveData<QueryResult<Profile>> = profileLiveData

    private fun MediatorLiveData<QueryResult<Profile>>.postProfileData(
        metadataResult: QueryResult<UserMetadataModel>?,
        isMyUserResult: Boolean
    ) {
        when (metadataResult) {
            is QueryResult.Success -> postValue(
                QueryResult.Success(
                    Profile(
                        metadataResult.originalQuery,
                        isMyUserResult
                    )
                )
            )
            is QueryResult.Error -> postValue(QueryResult.Error(metadataResult.error, Profile.EMPTY))
            is QueryResult.Loading -> postValue(QueryResult.Loading(Profile.EMPTY))
        }
    }

    fun clearProfileCover() {
        userMetadataUseCase.updateMetadata(newCoverUrl = "")
    }

    fun clearProfileAvatar() {
        userMetadataUseCase.updateMetadata(newProfileImageUrl = "")
    }

    init {
        signInUseCase.subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        signInUseCase.unsubscribe()
    }
}