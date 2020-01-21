package io.golos.cyber_android.ui.screens.login_sign_up.fragments.fingerprint

import androidx.lifecycle.ViewModel
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.shared.mvvm.SingleLiveData
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateToInAppAuthScreenCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ShowMessageResCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ViewCommand
import io.golos.cyber_android.ui.screens.login_sign_up.fragments.fingerprint.view_commands.NavigateToKeysCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateToMainScreenCommand
import io.golos.domain.DispatchersProvider
import io.golos.domain.dto.AppUnlockWay
import io.golos.domain.dto.AuthType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.UnsupportedOperationException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class FingerprintViewModel
@Inject
constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val model: FingerprintModel
) : ViewModel(), CoroutineScope {

    private val scopeJob: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = scopeJob + dispatchersProvider.uiDispatcher

    val command: SingleLiveData<ViewCommand> = SingleLiveData()

    fun onUnlockViaPinCodeClick() = saveUnlockWay(AppUnlockWay.PIN_CODE)

    fun onUnlockViaFingerprintClick()  = saveUnlockWay(AppUnlockWay.FINGERPRINT)

    override fun onCleared() {
        scopeJob.takeIf { it.isActive }?.cancel()
    }

    private fun saveUnlockWay(appUnlockWay: AppUnlockWay) {
        launch {
            try {
                model.saveAppUnlockWay(appUnlockWay)
                command.value =
                    when(model.getAuthType()) {
                        AuthType.SIGN_UP -> {
                            if(appUnlockWay == AppUnlockWay.PIN_CODE){
                                NavigateToKeysCommand()
                            } else{
                                NavigateToInAppAuthScreenCommand()
                            }
                        }
                        AuthType.SIGN_IN -> {
                            model.saveKeysExported()
                            NavigateToMainScreenCommand()
                        }
                        else -> throw UnsupportedOperationException("This type is not supported")
                    }
            } catch (ex: Exception) {
                command.value = ShowMessageResCommand(R.string.common_general_error)
            }
        }
    }
}