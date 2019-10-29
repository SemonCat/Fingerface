package android.hardware.fingerprint;

public class MyAuthenticationResult extends FingerprintManager.AuthenticationResult {

    FingerprintManager.CryptoObject cryptoObject;

    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        this.cryptoObject = cryptoObject;
    }

    @Override
    public FingerprintManager.CryptoObject getCryptoObject() {
        if (cryptoObject != null) {
            return cryptoObject;
        }
        return super.getCryptoObject();
    }
}
