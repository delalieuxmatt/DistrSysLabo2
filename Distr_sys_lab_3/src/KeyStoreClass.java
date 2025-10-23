import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.crypto.Cipher;

public class KeyStoreClass {

    public static void main(String[] args) throws Exception {
        // --- Load Werner's keystore (store1.jks) ---
        String store1Path = "store1.jks";
        char[] store1Pass = "wernerpass".toCharArray();
        KeyStore store1 = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream(store1Path)) {
            store1.load(fis, store1Pass);
        }

        // --- Get Freya's public certificate from Werner's store ---
        Certificate freyaCert = store1.getCertificate("freya");
        PublicKey freyaPublicKey = freyaCert.getPublicKey();

        // --- Encrypt a message using Freya's public key ---
        String message = "Hello Freya, this is a secure message!";
        Cipher encryptCipher = Cipher.getInstance("RSA");

        //De RSA asymmetrische encryptie-algoritme wordt gekozen.

        encryptCipher.init(Cipher.ENCRYPT_MODE, freyaPublicKey);
        byte[] encrypted = encryptCipher.doFinal(message.getBytes());
        System.out.println("Encrypted message: " + bytesToHex(encrypted));

        // --- Load Freya's keystore (store2.jks) ---
        String store2Path = "store2.jks";
        char[] store2Pass = "".toCharArray();
        KeyStore store2 = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream(store2Path)) {
            store2.load(fis, store2Pass);
        }

        // --- Get Freya's private key from her store ---
        char[] freyaKeyPass = "".toCharArray();
        PrivateKey freyaPrivateKey = (PrivateKey) store2.getKey("freya", freyaKeyPass);

        // --- Decrypt message ---
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, freyaPrivateKey);
        byte[] decrypted = decryptCipher.doFinal(encrypted);

        System.out.println("Decrypted message: " + new String(decrypted));
    }

    // Utility to print bytes as hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
