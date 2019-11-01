@file:Suppress("DEPRECATION")

package android.hardware.fingerprint

import android.hardware.biometrics.BiometricPrompt

class MyAuthenticationResult(private val result: BiometricPrompt.AuthenticationResult) :
    FingerprintManager.AuthenticationResult() {

    private fun BiometricPrompt.CryptoObject.toLegacy() = when {
        cipher != null -> FingerprintManager.CryptoObject(cipher)
        mac != null -> FingerprintManager.CryptoObject(mac)
        signature != null -> FingerprintManager.CryptoObject(signature)
        else -> null
    }

    override fun getCryptoObject(): FingerprintManager.CryptoObject? {
        return result.cryptoObject?.toLegacy()
    }
}
