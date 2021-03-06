package io.golos.cyber_android.ui.screens.post_report.view.view_commands

import io.golos.cyber_android.ui.screens.post_report.view.PostReportDialog
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ViewCommand

data class SendReportCommand (val report: PostReportDialog.Report): ViewCommand