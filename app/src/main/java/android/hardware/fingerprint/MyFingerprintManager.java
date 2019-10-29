package android.hardware.fingerprint;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class MyFingerprintManager extends FingerprintManager {

    private Context mContext;

    private KeyGenerator keyGenerator;
    private KeyStore keyStore;
    private Cipher cipher;

    private String KEY_NAME = "MyFingerprintManager";

    public void SetContext(Context context) {
        mContext = context;
    }

    @Override
    public boolean isHardwareDetected() {

        Log.d("XposedHandler", "isHardwareDetected");
        return true;
    }

    @Override
    public boolean hasEnrolledFingerprints() {
        Log.d("XposedHandler", "hasEnrolledFingerprints");
        return true;
    }

    @Override
    public void authenticate(final CryptoObject crypto, CancellationSignal cancel,
                             int flags, final AuthenticationCallback callback, Handler handler) {

        BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(mContext).setTitle("FaceID").setNegativeButton(mContext.getString(android.R.string.cancel), mContext.getMainExecutor(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).build();

        BiometricPrompt.CryptoObject c = null;

        if (crypto == null) {

            generateKey();
            initCipher();
            c = new BiometricPrompt.CryptoObject(cipher);

        } else if (crypto.getCipher() != null) {
            c = new BiometricPrompt.CryptoObject(crypto.getCipher());
        } else if (crypto.getMac() != null) {
            c = new BiometricPrompt.CryptoObject(crypto.getMac());
        } else if (crypto.getSignature() != null) {
            c = new BiometricPrompt.CryptoObject(crypto.getSignature());
        }

        biometricPrompt.authenticate(c, cancel, mContext.getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                callback.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                callback.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {

                BiometricPrompt.CryptoObject BioCrypto = result.getCryptoObject();

                CryptoObject c = null;

                if (BioCrypto.getCipher() != null) {
                    c = new CryptoObject(BioCrypto.getCipher());
                } else if (BioCrypto.getMac() != null) {
                    c = new CryptoObject(BioCrypto.getMac());
                } else if (BioCrypto.getSignature() != null) {
                    c = new CryptoObject(BioCrypto.getSignature());
                }

                AuthenticationResult fingerprintResult = new AuthenticationResult();

                if (crypto != null) {
                    fingerprintResult = new MyAuthenticationResult();
                    ((MyAuthenticationResult) fingerprintResult).setCryptoObject(c);
                }

                callback.onAuthenticationSucceeded(fingerprintResult);
            }

            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        });
    }


    private void generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
        }
    }

    private boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;


        } catch (KeyPermanentlyInvalidatedException e) {
            return false;

        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {

            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}
