@file:Suppress("DEPRECATION")

package android.hardware.fingerprint

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.os.Handler
import com.edison.fingerface.HandlerExecutor
import com.edison.fingerface.MainActivity
import com.edison.fingerface.PreferenceProvider

class MyFingerprintManager(private val context: Context) : FingerprintManager() {

    private val manager = context.getSystemService(BiometricManager::class.java)!!
    private val prefs = PreferenceProvider.getRemote(context)

    override fun isHardwareDetected(): Boolean {
        return when (manager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    override fun hasEnrolledFingerprints(): Boolean {
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

    class MyAuthenticationCallback(
        private val callback: AuthenticationCallback
    ) : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            callback.onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            callback.onAuthenticationHelp(helpCode, helpString)
        }

        override fun onAuthenticationFailed() {
            callback.onAuthenticationFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            callback.onAuthenticationSucceeded(MyAuthenticationResult(result))
        }
    }

    class MyAuthenticationResult(
        private val result: BiometricPrompt.AuthenticationResult
    ) : AuthenticationResult() {

        private fun BiometricPrompt.CryptoObject.toLegacy() = when {
            cipher != null -> CryptoObject(cipher)
            mac != null -> CryptoObject(mac)
            signature != null -> CryptoObject(signature)
            else -> null
        }

        override fun getCryptoObject(): CryptoObject? {
            return result.cryptoObject?.toLegacy()
        }
    }
}
