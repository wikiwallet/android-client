package io.golos.cyber_android.ui.screens.in_app_auth_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.application.dependency_injection.graph.app.ui.in_app_auth_activity.InAppAuthActivityComponent
import io.golos.cyber_android.core.fingerprints.FingerprintAuthManager
import io.golos.cyber_android.ui.base.ActivityBase
import io.golos.cyber_android.ui.screens.in_app_auth_activity.navigation.Navigator
import io.golos.domain.DispatchersProvider
import io.golos.domain.KeyValueStorageFacade
import io.golos.domain.entities.AppUnlockWay
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class InAppAuthActivity : ActivityBase(), CoroutineScope {
    private val scopeJob: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = scopeJob + dispatchersProvider.uiDispatcher

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var keyValueStorage: KeyValueStorageFacade

    @Inject
    lateinit var fingerprintAuthManager: FingerprintAuthManager

    @Inject
    lateinit var dispatchersProvider: DispatchersProvider

    companion object {
        const val REQUEST_CODE = 4263

        const val PIN_CODE_HEADER_ID = "PIN_CODE_HEADER_ID"
        const val FINGERPRINT_HEADER_ID = "PIN_CODE_HEADER_ID"

        fun start(activity: ActivityBase,
                  @StringRes pinCodeHeaderText: Int = R.string.authPinCodeDefaultHeader,
                  @StringRes fingerprintHeaderText: Int = R.string.authFingerprintDefaultHeader) {

            activity.startActivityForResult(createStartIntent(activity, pinCodeHeaderText, fingerprintHeaderText), REQUEST_CODE)
        }

        fun start(fragment: Fragment,
                  @StringRes pinCodeHeaderText: Int = R.string.authPinCodeDefaultHeader,
                  @StringRes fingerprintHeaderText: Int = R.string.authFingerprintDefaultHeader) {

            fragment.startActivityForResult(createStartIntent(fragment.requireContext(), pinCodeHeaderText, fingerprintHeaderText), REQUEST_CODE)
        }

        private fun createStartIntent(context: Context, @StringRes pinCodeHeaderText: Int, @StringRes fingerprintHeaderText: Int): Intent =
            Intent(context, InAppAuthActivity::class.java)
                .also { intent ->
                    Bundle()
                        .apply {
                            putInt(PIN_CODE_HEADER_ID, pinCodeHeaderText)
                            putInt(FINGERPRINT_HEADER_ID, fingerprintHeaderText)

                            intent.putExtras(this)
                        }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_auth)

        App.injections.get<InAppAuthActivityComponent>().inject(this)

        launch {
            when(getAppUnlockWay()) {
                AppUnlockWay.PIN_CODE ->
                    navigator.setPinCodeAsHome(this@InAppAuthActivity, intent.extras!!.getInt(PIN_CODE_HEADER_ID))
                AppUnlockWay.FINGERPRINT ->
                    navigator.setFingerprintAsHome(this@InAppAuthActivity, intent.extras!!.getInt(FINGERPRINT_HEADER_ID))
            }
        }
    }

    override fun onBackPressed() {
        navigator.setAuthFailResult(this)
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        if(isFinishing) {
            scopeJob.takeIf { it.isActive }?.cancel()
            App.injections.release<InAppAuthActivityComponent>()
        }
    }

    private suspend fun getAppUnlockWay(): AppUnlockWay =
        withContext(dispatchersProvider.ioDispatcher) {
            if(!fingerprintAuthManager.isAuthenticationPossible) {
                return@withContext AppUnlockWay.PIN_CODE
            }

            keyValueStorage.getAppUnlockWay() ?: AppUnlockWay.PIN_CODE
        }
}