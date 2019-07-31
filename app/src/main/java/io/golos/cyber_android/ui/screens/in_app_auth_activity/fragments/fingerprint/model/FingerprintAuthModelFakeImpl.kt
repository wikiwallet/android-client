package io.golos.cyber_android.ui.screens.in_app_auth_activity.fragments.fingerprint.model

import io.golos.cyber_android.core.fingerprints.eventsHandler.FingerprintAuthEventHandler
import io.golos.cyber_android.ui.common.mvvm.model.ModelBaseImpl
import io.golos.domain.dependency_injection.scopes.ActivityScope
import javax.inject.Inject

@ActivityScope
class FingerprintAuthModelFakeImpl
@Inject
constructor(): ModelBaseImpl(),
    FingerprintAuthModel {

    override fun startAuth(eventsCallback: FingerprintAuthEventHandler) {
        // do nothing
    }

    override fun cancelAuth() {
        // do nothing
    }
}