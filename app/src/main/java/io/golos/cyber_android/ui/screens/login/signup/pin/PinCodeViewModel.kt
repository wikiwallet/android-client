package io.golos.cyber_android.ui.screens.login.signup.pin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.mvvm.SingleLiveData
import io.golos.cyber_android.ui.common.mvvm.view_commands.NavigateToMainScreenCommand
import io.golos.cyber_android.ui.common.mvvm.view_commands.ShowMessageCommand
import io.golos.cyber_android.ui.common.mvvm.view_commands.ViewCommand
import io.golos.cyber_android.ui.screens.login.signup.pin.view_commands.NavigateToFingerprintCommand
import io.golos.cyber_android.ui.screens.login.signup.pin.view_commands.NavigateToKeysCommand
import io.golos.cyber_android.ui.screens.login.signup.pin.view_state_dto.CodeState
import io.golos.domain.DispatchersProvider
import io.golos.domain.entities.AuthType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.UnsupportedOperationException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PinCodeViewModel
@Inject
constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val model: PinCodeModel
) : ViewModel(), CoroutineScope {

    private val scopeJob: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = scopeJob + dispatchersProvider.uiDispatcher

    val isInExtendedMode = MutableLiveData(false)

    val codeState = MutableLiveData(CodeState(true, false, false, false))

    val command: SingleLiveData<ViewCommand> = SingleLiveData()

    fun onPrimaryCodeUpdated(code: String?) {
        model.updatePrimaryCode(code)

        if(model.isPrimaryCodeCompleted()) {
            isInExtendedMode.value = true
            codeState.value = CodeState(false, true, codeState.value!!.isInErrorState, false)
        }
    }

    fun onRepeatedCodeUpdated(code: String?) {
        model.updateRepeatedCode(code)

        if(model.isRepeatedCodeCompleted()) {
            if(model.validate()) {
                codeState.value = CodeState(false, true, false, false)

                launch {
                    if(model.saveCode()) {
                        command.value = if(model.isFingerprintAuthenticationPossible) {
                            NavigateToFingerprintCommand()
                        } else {
                            when(model.getAuthType()) {
                                AuthType.SIGN_IN -> NavigateToMainScreenCommand()
                                AuthType.SIGN_UP -> NavigateToKeysCommand()
                                else -> throw UnsupportedOperationException("This type is not supported")
                            }
                        }
                    } else {
                        command.value =
                            ShowMessageCommand(R.string.common_general_error)
                    }
                }
            } else {
                model.updatePrimaryCode(null)
                model.updateRepeatedCode(null)

                command.value = ShowMessageCommand(R.string.codes_not_match)
                codeState.value = CodeState(true, false, true, true)
            }
        }
    }

    override fun onCleared() {
        scopeJob.takeIf { it.isActive }?.cancel()
    }
}