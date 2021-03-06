package io.golos.cyber_android.ui.screens.wallet_send_points.model

import io.golos.cyber_android.ui.screens.wallet_dialogs.transfer_completed.TransferCompletedInfo
import io.golos.cyber_android.ui.screens.wallet_point.dto.CarouselStartData
import io.golos.cyber_android.ui.screens.wallet_shared.dto.AmountValidationResult
import io.golos.cyber_android.ui.shared.mvvm.model.ModelBase
import io.golos.domain.dto.CommunityIdDomain
import io.golos.domain.dto.UserBriefDomain
import io.golos.domain.dto.WalletCommunityBalanceRecordDomain

interface WalletSendPointsModel : ModelBase {
    val canSelectUser: Boolean

    var sendToUser: UserBriefDomain?

    val balance: List<WalletCommunityBalanceRecordDomain>

    val currentBalanceRecord: WalletCommunityBalanceRecordDomain

    val carouselItemsData: CarouselStartData

    val hasFee: Boolean

    val titleTextResId: Int

    fun updateAmount(amountAsString: String?): Boolean

    suspend fun updateBalances():Pair<Int?,Double?>?

    /**
     * @return Index of the community in the balance list
     */
    fun updateCurrentCommunity(communityId: CommunityIdDomain): Pair<Int?,Double?>?

    fun validateAmount(): AmountValidationResult

    suspend fun makeTransfer()

    suspend fun notifyBalanceUpdate(isBalanceUpdated:Boolean)

    fun getTransferCompletedInfo(): TransferCompletedInfo

    fun getAmountAsString(): String?
}