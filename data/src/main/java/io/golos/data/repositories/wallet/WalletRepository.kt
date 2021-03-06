package io.golos.data.repositories.wallet

import io.golos.domain.GlobalConstants
import io.golos.domain.dto.*

interface WalletRepository {
    suspend fun getBalance(): List<WalletCommunityBalanceRecordDomain>

    /**
     * Returns a quantity of the community points in one commun
     */
    suspend fun getExchangeRate(communityId: CommunityIdDomain): Double

    suspend fun getTransferHistory(
        offset: Int,
        limit: Int,
        communityId: CommunityIdDomain = CommunityIdDomain(GlobalConstants.ALL_COMMUNITIES_CODE),
        historyFilterDomain: HistoryFilterDomain?
    ): List<WalletTransferHistoryRecordDomain>

    suspend fun sendToUser(toUser: UserIdDomain, amount: Double, communityId: CommunityIdDomain)

    suspend fun donate(contentId: ContentIdDomain, amount: Double, communityId: CommunityIdDomain): DonationsDomain?

    suspend fun convertPointsToCommun(amount: Double, communityId: CommunityIdDomain)

    suspend fun convertCommunToPoints(amount: Double, communityId: CommunityIdDomain)

    suspend fun getDonations(postId: ContentIdDomain): DonationsDomain?
}