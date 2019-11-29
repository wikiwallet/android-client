package io.golos.cyber_android.ui.screens.profile_communities.model

import androidx.lifecycle.LiveData
import io.golos.cyber_android.ui.common.mvvm.model.ModelBase
import io.golos.cyber_android.ui.common.recycler_view.versioned.VersionedListItem

interface ProfileCommunitiesModel : ModelBase {
    val items: LiveData<List<VersionedListItem>>

    fun loadPage()
}