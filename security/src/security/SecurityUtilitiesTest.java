package security;

import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SecurityUtilitiesTest {


    @org.junit.Test
    public void loadKeystore(){

        try {
            KeyPair kp = SecurityUtilities.generateKeyPair();
            X509Certificate cert = SecurityUtilities.makeV1Certificate(kp.getPrivate(),kp.getPublic(),"tom");
            String fingerprint = SecurityUtilities.calculateFingerprint(cert.getEncoded());

            SecurityUtilities.storePrivateKeyEntry("tompass",kp.getPrivate(), new X509Certificate[]{cert}, fingerprint);

            KeyStore keystore = SecurityUtilities.loadKeystore("tompass");
            KeyStore.ProtectionParameter keyPassword = new KeyStore.PasswordProtection("tompass".toCharArray());
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(fingerprint, keyPassword);

            assertEquals(cert, privateKeyEntry.getCertificate());
            assertEquals(kp.getPrivate(), privateKeyEntry.getPrivateKey());

            SecurityUtilities.deletePrivateKeyEntry("tompass", fingerprint);


        }catch (GeneralSecurityException | OperatorCreationException | IOException e){
            e.getMessage();
            fail("faloopsyboopsy");
        }


    }

    @org.junit.Test
    public void loadTruststore(){


    }

}