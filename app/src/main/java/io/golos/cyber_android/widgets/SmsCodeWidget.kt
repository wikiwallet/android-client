package io.golos.cyber_android.widgets

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import io.golos.cyber_android.R
import io.golos.cyber_android.views.utils.BaseTextWatcher
import io.golos.cyber_android.views.utils.ViewUtils
import kotlinx.android.synthetic.main.view_sms_code.view.*


/**
 * Use this so we don't have to rely on [KeyEvent] constants order in comparison
 */
val DIGIT_KEYCODES = arrayOf(
    KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2,
    KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
    KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8,
    KeyEvent.KEYCODE_9
)

/**
 * Custom view for sms code input
 */
class SmsCodeWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var listener: Listener? = null

    private val digits by lazy { arrayOf(smsCodeDigit0, smsCodeDigit1, smsCodeDigit2, smsCodeDigit3) }

    init {
        inflate(getContext(), R.layout.view_sms_code, this)

        digits.forEach {
            it.filters = arrayOf(InputFilter.LengthFilter(1))

            it.addTextChangedListener(object : BaseTextWatcher() {
                override fun afterTextChanged(s: Editable?) {
                    onCodeChanged()
                }
            })

            it.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    //if user presses del
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        //if current digit view is empty and it's not the first digit
                        if (it.text.isEmpty() && digits.indexOf(it) > 0)
                        //then delete previous digit
                            digits[digits.indexOf(it) - 1].setText("")
                        //remove this digit anyway (if it's already empty then this will do nothing)
                        it.setText("")

                        //request focus on previous digit (if it's not the first one)
                        if (digits.indexOf(it) > 0)
                            digits[digits.indexOf(it) - 1].requestFocus()
                    }
                    //if user presses digit button
                    if (keyCode in DIGIT_KEYCODES) {
                        //and it's not the last digit
                        if (digits.indexOf(it) < digits.size - 1)
                        //then request focus for next digit
                            digits[digits.indexOf(it) + 1].requestFocus()
                        //if current digit is already not empty
                        if (it.text.isNotEmpty())
                        //then set text for the next (if any)
                            if (digits.indexOf(it) < digits.size - 1)
                            //then request focus for next digit
                                digits[digits.indexOf(it) + 1].setText(DIGIT_KEYCODES.indexOf(keyCode).toString())
                    }
                }
                false
            }
        }

        digits.last().setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && getCode().length == digits.size) {
                listener?.sendCode(getCode())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun showKeyboard() {
        digits.find { it.text.isEmpty() }?.let {
            ViewUtils.showKeyboard(it)
        }
    }

    private fun onCodeChanged() {
        listener?.onCodeChanged(getCode())
    }

    private fun getCode() = digits
        .map { it.text.toString() }
        .reduce { acc, text -> acc + text }
        .trim()

    fun clearCode() {
        digits.forEach { it.setText("") }
    }

    interface Listener {
        /**
         * Called when user press "Done" button and code has necessary length
         */
        fun sendCode(code: String)

        /**
         * Called when code was changed
         */
        fun onCodeChanged(code: String)
    }
}