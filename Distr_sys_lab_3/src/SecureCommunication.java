import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;

public class SecureCommunication {

    public static void main(String[] args) throws Exception {
        // --- 1. Load Werner’s keystore (sender) ---
        KeyStore wernerStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream("store1.jks")) {
            wernerStore.load(fis, "wernerpass".toCharArray());
        }
        PrivateKey wernerPrivateKey =
                (PrivateKey) wernerStore.getKey("werner", "wernerpass".toCharArray());
        Certificate freyaCert = wernerStore.getCertificate("freya");
        PublicKey freyaPublicKey = freyaCert.getPublicKey();

        // --- 2. Prepare message ---
        String message = "Hello Freya, this is a confidential and signed message.";

        // --- 3. Hash + sign message ---
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = sha.digest(message.getBytes());

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(wernerPrivateKey);
        sign.update(messageHash);
        byte[] signature = sign.sign();

        System.out.println("Digital Signature (Base64): " +
                Base64.getEncoder().encodeToString(signature));

        // --- 4. Generate AES session key ---
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // 256 bits for strong security
        SecretKey aesKey = keyGen.generateKey();

        byte[] iv = new byte[16]; // can randomize if you wish
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // --- 5. Encrypt message with AES ---
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encryptedMessage = aesCipher.doFinal(message.getBytes());

        // --- 6. Encrypt AES key with Freya’s public key ---
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, freyaPublicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        System.out.println("Encrypted AES key (Base64): " +
                Base64.getEncoder().encodeToString(encryptedAesKey));

        // --- "Send" encryptedMessage, encryptedAesKey, signature ---

        // ============================================================
        // --- Receiver (Freya) side ---
        // ============================================================

        KeyStore freyaStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream("store2.jks")) {
            freyaStore.load(fis, "".toCharArray());
        }
        PrivateKey freyaPrivateKey =
                (PrivateKey) freyaStore.getKey("freya", "".toCharArray());
        Certificate wernerCert = freyaStore.getCertificate("werner");
        PublicKey wernerPublicKey = wernerCert.getPublicKey();

        // --- 7. Decrypt AES key using Freya’s private key ---
        rsaCipher.init(Cipher.DECRYPT_MODE, freyaPrivateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey originalAesKey =
                new javax.crypto.spec.SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");

        // --- 8. Decrypt message with AES key ---
        aesCipher.init(Cipher.DECRYPT_MODE, originalAesKey, ivSpec);
        byte[] decryptedBytes = aesCipher.doFinal(encryptedMessage);
        String decryptedMessage = new String(decryptedBytes);

        System.out.println("Decrypted message: " + decryptedMessage);

        // --- 9. Verify signature ---
        byte[] recomputedHash = sha.digest(decryptedMessage.getBytes());
        Signature verifySign = Signature.getInstance("SHA256withRSA");
        verifySign.initVerify(wernerPublicKey);
        verifySign.update(recomputedHash);
        boolean verified = verifySign.verify(signature);

        System.out.println("Signature verified: " + verified);
    }
}
