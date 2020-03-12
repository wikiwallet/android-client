package io.golos.cyber_android.ui.shared.text

import android.text.InputFilter
import android.text.Spanned
import java.text.DecimalFormat

class CurrencyInputFilter
constructor(private val maxLen: Int, private val decimalPointsQuantity: Int): InputFilter {
    @Suppress("ConvertToStringTemplate")
    private val regex = Regex("""^\d+\.*\d{0,"""+decimalPointsQuantity.toString()+"""}$""")

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        return source?.let { sourceText ->
            dest?.let { destText ->
                return if (destText.length < maxLen) {
                    val prediction = StringBuilder(dest)
                    prediction.insert(dstart, sourceText)

                    if(regex.containsMatchIn(prediction)) {
                        null        // Accept changes
                    } else {
                        ""
                    }

                } else {
                    ""       // Reject changes because of length filter
                }
            }
        }
    }

    fun format(source: String): String =
        if(source.isBlank()) {
            ""
        } else {
            DecimalFormat("#.${"#".repeat(decimalPointsQuantity)}").format(source.toDouble())
        }
}