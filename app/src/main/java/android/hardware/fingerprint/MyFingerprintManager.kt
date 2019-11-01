@file:Suppress("DEPRECATION")

package android.hardware.fingerprint

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.os.Handler
import android.util.Log
import com.edison.fingerface.HandlerExecutor
import com.edison.fingerface.MainActivity
import com.edison.fingerface.PreferenceProvider

class MyFingerprintManager(private val context: Context) : FingerprintManager() {

    private val manager = context.getSystemService(BiometricManager::class.java)!!
    private val prefs = PreferenceProvider.getRemote(context)

    override fun isHardwareDetected(): Boolean {
        Log.d(TAG, "isHardwareDetected")
        return when (manager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    override fun hasEnrolledFingerprints(): Boolean {
        Log.d(TAG, "hasEnrolledFingerprints")
        return when (manager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun CryptoObject.toBio() = when {
        cipher != null -> BiometricPrompt.CryptoObject(cipher)
        mac != null -> BiometricPrompt.CryptoObject(mac)
        signature != null -> BiometricPrompt.CryptoObject(signature)
        else -> null
    }

    override fun authenticate(
        crypto: CryptoObject?, cancel: CancellationSignal?,
        flags: Int, legacyCallback: AuthenticationCallback, handler: Handler?
    ) {
        Log.d(TAG, "authenticate")

        val bioCrypto = crypto?.toBio()
        val cancelSignal = cancel ?: CancellationSignal()
        val executor = handler?.let { HandlerExecutor(it) } ?: context.mainExecutor
        val callback = MyAuthenticationCallback(legacyCallback)

        val biometricPrompt = BiometricPrompt.Builder(context).setTitle("Fingerface")
            .setNegativeButton(
                context.getString(android.R.string.cancel),
                executor,
                DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
            .setConfirmationRequired(prefs.getBoolean(MainActivity.REQUIRE_CONFIRM, false))
            .build()

        if (bioCrypto == null) {
            biometricPrompt.authenticate(cancelSignal, executor, callback)
        } else {
            biometricPrompt.authenticate(bioCrypto, cancelSignal, executor, callback)
        }
    }

    companion object {
        const val TAG = "Fingerface"
    }
}
