package com.devshady.auth.sdk.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsRetrieverHelper(private val context: Context) {

    private var onOtpReceived: ((String) -> Unit)? = null

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status

                when (status?.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                        message?.let {
                            val otp = extractOtp(it)
                            otp?.let { onOtpReceived?.invoke(it) }
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Handle timeout
                    }
                }
            }
        }
    }

    fun startListening(onOtpReceived: (String) -> Unit) {
        this.onOtpReceived = onOtpReceived
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsRetriever()

        task.addOnSuccessListener {
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    smsReceiver,
                    intentFilter,
                    SmsRetriever.SEND_PERMISSION,
                    null,
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                context.registerReceiver(
                    smsReceiver,
                    intentFilter,
                    SmsRetriever.SEND_PERMISSION,
                    null
                )
            }
        }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }

    private fun extractOtp(message: String): String? {
        // Simple regex to find a 6-digit code
        val pattern = Regex("(\\d{6})")
        return pattern.find(message)?.value
    }
}
