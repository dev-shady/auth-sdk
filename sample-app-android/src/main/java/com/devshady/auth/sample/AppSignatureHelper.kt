package com.devshady.auth.sample

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays

class AppSignatureHelper(context: Context) : ContextWrapper(context) {

    val appSignatures: ArrayList<String>
        get() {
            val appCodes = ArrayList<String>()
            try {
                val packageName = packageName
                val packageManager = packageManager
                val signatures = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES
                ).signatures

                if (signatures != null) {
                    for (signature in signatures) {
                        val hash = hash(packageName, signature.toCharsString())
                        if (hash != null) {
                            appCodes.add(hash)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("sms auto fill SDK_HASH", "Error fetching signature", e)
            }
            return appCodes
        }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            var hashSignature = messageDigest.digest()
            hashSignature = Arrays.copyOfRange(hashSignature, 0, 9)
            var base64Hash = Base64.encodeToString(
                hashSignature, Base64.NO_PADDING or Base64.NO_WRAP
            )
            base64Hash = base64Hash.substring(0, 11)
            return base64Hash
        } catch (e: NoSuchAlgorithmException) {
            return null
        }
    }
}
