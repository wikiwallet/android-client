package io.golos.cyber_android.ui.screens.app_start.sign_in.qr_code.di

import dagger.Subcomponent
import io.golos.cyber_android.ui.screens.app_start.sign_in.qr_code.view.SignInQrCodeFragment
import io.golos.domain.dependency_injection.scopes.FragmentScope

@Subcomponent(modules = [SignInQrCodeFragmentModuleBinds::class])
@FragmentScope
interface SignInQrCodeFragmentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SignInQrCodeFragmentComponent
    }

    fun inject(fragment: SignInQrCodeFragment)
}