package com.devshady.auth.sdk.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsRetrieverHelper(private val context: Context) {

    private var onOtpReceived: ((String) -> Unit)? = null

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("sms auto fill", "SmsRetrieverHelper sms received")
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
        Log.d("sms auto fill", "SmsRetrieverHelper startListening")
        this.onOtpReceived = onOtpReceived
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsRetriever()

        task.addOnSuccessListener {
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Log.d("sms auto fill", "SmsRetrieverHelper registerReceiver")
                context.registerReceiver(
                    smsReceiver,
                    intentFilter,
                    SmsRetriever.SEND_PERMISSION,
                    null,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                Log.d("sms auto fill", "SmsRetrieverHelper registerReceiver")
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
        Log.d("sms auto fill", "SmsRetrieverHelper stopListening")
        try {
            context.unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }

    private fun extractOtp(message: String): String? {
        Log.d("sms auto fill", "SmsRetrieverHelper extractOtp")
        // Simple regex to find a 6-digit code
        val pattern = Regex("(\\d{6})")
        return pattern.find(message)?.value
    }
}
