package io.golos.cyber_android.ui.screens.login_sign_up_select_method.google

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ViewCommand
import java.io.Closeable

interface GoogleAuth: Closeable {
    val command: LiveData<ViewCommand>

    fun startAuth(fragment: Fragment)

    /**
     * @return true - processed
     */
    fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}