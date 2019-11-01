@file:Suppress("DEPRECATION")

package android.hardware.fingerprint

import android.hardware.biometrics.BiometricPrompt

class MyAuthenticationCallback(private val callback: FingerprintManager.AuthenticationCallback) :
    BiometricPrompt.AuthenticationCallback() {

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
