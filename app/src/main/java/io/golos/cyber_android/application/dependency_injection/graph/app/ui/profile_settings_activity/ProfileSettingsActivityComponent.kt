package io.golos.cyber_android.application.dependency_injection.graph.app.ui.profile_settings_activity

import dagger.Subcomponent
import io.golos.cyber_android.ui.screens.profile.edit.settings.ProfileSettingsActivity
import io.golos.domain.dependency_injection.scopes.ActivityScope

@Subcomponent(modules = [
    ProfileSettingsActivityModule::class,
    ProfileSettingsActivityModuleBinds::class
])
@ActivityScope
interface ProfileSettingsActivityComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileSettingsActivityComponent
    }

    fun inject(activity: ProfileSettingsActivity)
}