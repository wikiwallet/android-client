package io.golos.cyber_android.ui.screens.profile_communities.view

import android.os.Bundle
import io.golos.cyber_android.application.App
import io.golos.cyber_android.databinding.FragmentProfileCommunitiesBinding
import io.golos.cyber_android.ui.dto.ProfileCommunities
import io.golos.cyber_android.ui.screens.profile_communities.di.ProfileCommunitiesExternalUserFragmentComponent
import io.golos.cyber_android.ui.screens.profile_communities.view_model.ProfileCommunitiesViewModel

class ProfileCommunitiesExternalUserFragment : ProfileCommunitiesFragment() {
    companion object {
        private const val SOURCE_DATA = "SOURCE_DATA"

        fun newInstance(sourceData: ProfileCommunities) =
            ProfileCommunitiesExternalUserFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SOURCE_DATA, sourceData)
                }
            }
    }

    override fun inject(key: String) =
        App.injections
            .get<ProfileCommunitiesExternalUserFragmentComponent>(
                key,
                arguments!!.getParcelable<ProfileCommunities>(SOURCE_DATA))
            .inject(this)

    override fun releaseInjection(key: String) = App.injections.release<ProfileCommunitiesExternalUserFragmentComponent>(key)

    override fun linkViewModel(binding: FragmentProfileCommunitiesBinding, viewModel: ProfileCommunitiesViewModel) {
        super.linkViewModel(binding, viewModel)
        binding.isExternalUserProfile = true
    }
}