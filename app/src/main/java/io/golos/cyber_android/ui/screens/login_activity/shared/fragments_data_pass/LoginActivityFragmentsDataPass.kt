package io.golos.cyber_android.ui.screens.login_activity.shared.fragments_data_pass

import android.net.Uri
import io.golos.cyber_android.ui.dto.QrCodeContent
import io.golos.domain.dto.CountryDomain

interface LoginActivityFragmentsDataPass {
    fun putQrCode(qrCode: QrCodeContent)
    fun getQrCode(): QrCodeContent?

    fun putSelectedCountry(country: CountryDomain)
    fun getSelectedCountry(): CountryDomain?

    fun putPhone(phone: String)
    fun getPhonePhone(): String?

    fun putFtueCommunityBonus(bonus: Int)
    fun getFtueCommunityBonus(): Int?
}