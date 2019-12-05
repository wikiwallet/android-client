package io.golos.cyber_android.ui.screens.ftue_finish.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactory
import io.golos.cyber_android.ui.common.mvvm.viewModel.FragmentViewModelFactoryImpl
import io.golos.cyber_android.ui.common.mvvm.viewModel.ViewModelKey
import io.golos.cyber_android.ui.screens.ftue_finish.view_model.FtueFinishViewModel
import io.golos.domain.dependency_injection.scopes.FragmentScope

@Module
interface FtueFinishFragmentModuleBinds {

    @Binds
    @ViewModelKey(FtueFinishViewModel::class)
    @IntoMap
    fun bindViewModel(viewModel: FtueFinishViewModel): ViewModel

    @Binds
    @FragmentScope
    fun bindViewModelFactory(factory: FragmentViewModelFactoryImpl): FragmentViewModelFactory
}