package security;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;
import java.util.Date;

public class SecurityUtilities {

    private static final String ASYMMETRIC_KEY_ALG = "RSA";
    private static final String SYMMETRIC_KEY_ALG = "AES";
    private static final String SYMMETRIC_KEY_ALG_MODE_PAD = SYMMETRIC_KEY_ALG + "/ECB/PKCS7Padding";
    private static final String PROVIDER = "BC";
    private static final String HASH_DIGEST_ALG = "SHA3-512";
    private static final String CERT_FACTORY = "X.509";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String SIGNATURE_ALG = "SHA384with" + ASYMMETRIC_KEY_ALG;
    private static final String SECURE_RANDOM_ALG = "SHA1PRNG";
    private static final String AUTH_HASH_DIGEST_ALG = "PBKDF2WithHmacSHA512";
    private static final File TRUSTSTORE_NAME = new File("../resources/truststore.p12");
    private static final File KEYSTORE_NAME = new File("../resources/keystore.p12");

    private static final int NUM_ITERATIONS = 100000;

    private static long serialNumberBase = System.currentTimeMillis();

    // APPARENTLY FUNCTIONALITY TO UPDATE KS PASSWORDS IS GOOD

    public static KeyStore.PrivateKeyEntry loadKeyEntry(String storePassword, String fingerprint)
            throws GeneralSecurityException, IOException
    {
        KeyStore keystore = loadKeystore(storePassword);
        KeyStore.ProtectionParameter keyPassword = new KeyStore.PasswordProtection(storePassword.toCharArray());
        return (KeyStore.PrivateKeyEntry) keystore.getEntry(fingerprint, keyPassword);
    }

    public static KeyStore loadKeystore(String storePassword)
            throws GeneralSecurityException, IOException
    {
        char[] password = storePassword.toCharArray();
        return load(password, KEYSTORE_NAME);
    }

    public static KeyStore loadTruststore(String storePassword)
            throws GeneralSecurityException, IOException
    {
        char[] password = storePassword.toCharArray();
        return load(password,   TRUSTSTORE_NAME);
    }

    private static KeyStore load(char[] password, File store)
            throws GeneralSecurityException, IOException
    {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);
        try(InputStream in = new FileInputStream(store)) {
            keyStore.load(in , password);
        }
        return keyStore;
    }


    /**
     * Calls deleteEntry with truststore name
     * @param storePassword the store password
     * @param fingerprint the certificate fingerprint
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void deleteCertificate(String storePassword, String fingerprint)
            throws GeneralSecurityException, IOException
    {
        deleteEntry(storePassword, fingerprint, TRUSTSTORE_NAME);
    }

    /**
     * Calls deleteEntry with the keystore name
     * @param storePassword the store password
     * @param fingerprint the certificate fingerprint
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void deletePrivateKeyEntry(String storePassword, String fingerprint)
            throws GeneralSecurityException, IOException
    {
        deleteEntry(storePassword, fingerprint, KEYSTORE_NAME);
    }

    /**
     * Delete the entry of the provided fingerprint from the provided storeName
     * @param storePassword the store password
     * @param fingerprint the network_alias
     * @param storeName the store name
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static void deleteEntry(String storePassword, String fingerprint, File storeName)
            throws GeneralSecurityException, IOException
    {
        char[] password = storePassword.toCharArray();
        KeyStore store = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);
        store.load(new FileInputStream(storeName), password);
        store.deleteEntry(fingerprint);
        try(FileOutputStream os = new FileOutputStream(storeName)) {
            store.store(os, password);
        }

    }

    /**
     * Store the trusted certificate for a particular network to the truststore.p12 file
     * @param storePassword the truststore password
     * @param trustedCert the trusted certificate
     * @param fingerprint the certificate fingerprint for identification
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void storeCertificate(String storePassword, X509Certificate trustedCert, String fingerprint)
            throws GeneralSecurityException, IOException
    {
        char[] password = storePassword.toCharArray();
        KeyStore truststore = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);
        try {
            truststore.load(new FileInputStream(TRUSTSTORE_NAME), password);
        }catch (IOException e) {
            truststore.load(null, null);
        }
        truststore.setCertificateEntry(fingerprint, trustedCert);
        try(FileOutputStream os = new FileOutputStream( TRUSTSTORE_NAME)) {
            truststore.store(os, password);
        }
    }

    /**
     * Store the private key and certificate chain for a network in the keystore.p12 file
     * @param storePassword the password of the keystore
     * @param eeKey the the private key to be stored
     * @param eeCertChain the certificate chain to be stored
     * @param fingerprint The certificate fingerprint for identification
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void storePrivateKeyEntry(String storePassword, PrivateKey eeKey, X509Certificate[] eeCertChain, String fingerprint)
            throws GeneralSecurityException, IOException
    {
        char[] password = storePassword.toCharArray();

        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE, PROVIDER);
        try {
            keystore.load(new FileInputStream(KEYSTORE_NAME), password);
        }catch (IOException e) {
            keystore.load(null, null);
        }
        keystore.setKeyEntry(fingerprint, eeKey, null, eeCertChain);

        try (FileOutputStream os = new FileOutputStream(KEYSTORE_NAME)) {
            keystore.store(os, password);
        }
    }

    /**
     * Generate an RSA keypair
     * @return return the keypair
     * @throws GeneralSecurityException
     */
    public static KeyPair generateKeyPair()
            throws GeneralSecurityException
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_KEY_ALG, PROVIDER);
        keyPairGenerator.initialize(new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F4));
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * This method creates SHA3-512 (512 bit) digests of the input data bytes and then base64 encodes them to a fingerprint
     * @param data The byte array representation of a public key
     * @return the hash of those bytes
     */
    public static String calculateFingerprint(byte[] data)
            throws GeneralSecurityException
    {
        MessageDigest hash = MessageDigest.getInstance(HASH_DIGEST_ALG, PROVIDER);
        return Base64.getEncoder().encodeToString(hash.digest(data));
    }

    /**
     * Generate a self signed V1 X509Certificate for use by the server to authenticate and sign new users into the network
     * it pertains to.
     * @param caPrivateKey The private key for use in signing
     * @param caPublicKey the public key of the certificate
     * @param name The name of the self signing party
     * @return The Certificate
     * @throws GeneralSecurityException
     * @throws OperatorCreationException
     */
    public static X509Certificate makeV1Certificate(PrivateKey caPrivateKey, PublicKey caPublicKey, String name)
            throws GeneralSecurityException, OperatorCreationException
    {
        X509v1CertificateBuilder v1CertBldr = new JcaX509v1CertificateBuilder(
                new X500Name("CN=" + name),
                calculateSerialNumber(),
                calculateDate(0),
                calculateDate(24 * 365 * 100),
                new X500Name("CN=" + name),
                caPublicKey);

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(SIGNATURE_ALG).setProvider(PROVIDER);
        return new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(v1CertBldr.build(signerBuilder.build(caPrivateKey)));
    }

    /**
     * A date utilitiy for calculating how much time in the future a certificate will be valid for
     * @param hoursInFuture the number of hours you want the certificate to be valid for
     * @return the Date of that number of hours in the future from the current time
     */
    private static Date calculateDate(int hoursInFuture){

        long secs = System.currentTimeMillis() / 1000;

        return new Date((secs + (hoursInFuture * 60 * 60)) * 1000);
    }

    /**
     * A method for soliciting a distinct serial number for certificate generation for multiple threads
     * @return the SerialNumber
     */
    private static synchronized BigInteger calculateSerialNumber(){
        return BigInteger.valueOf(serialNumberBase++);
    }


}
