import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionSpeedTest {

    private static final int DATA_SIZE_BYTES = 64;     // Data to encrypt (small block)
    private static final int RSA_KEY_SIZE_BITS = 2048;   // Common RSA key size
    private static final int AES_KEY_SIZE_BITS = 256;    // Common AES key size
    private static final int ITERATIONS = 1000;        // Number of enc/dec operations to average

    public static void main(String[] args) throws Exception {

        // 1. Generate random data for testing
        byte[] dataToEncrypt = new byte[DATA_SIZE_BYTES];
        new SecureRandom().nextBytes(dataToEncrypt);

        System.out.println("--- Encryption Performance Test ---");
        System.out.println("Testing with " + DATA_SIZE_BYTES + " bytes of data.");
        System.out.println("Averaging over " + ITERATIONS + " iterations.");
        System.out.println("-------------------------------------------------");

        // 2. Run the tests
        performSymmetricTest(dataToEncrypt);
        performAsymmetricTest(dataToEncrypt);

        System.out.println("-------------------------------------------------");
        System.out.println("\nSummary:");
        System.out.println("Symmetric (AES) is extremely fast for encryption/decryption.");
        System.out.println("Asymmetric (RSA) is much slower for those operations.");
        System.out.println("RSA's primary real-world use is to encrypt a *symmetric key* (hybrid encryption),");
        System.out.println("not large amounts of data directly.");
    }

    /**
     * Performs timing tests for AES (Symmetric).
     */
    public static void performSymmetricTest(byte[] data) throws Exception {
        System.out.println("\n--- Symmetric (AES-" + AES_KEY_SIZE_BITS + ") Test ---");

        // 1. Key Generation (Timed once)
        long keyGenStart = System.nanoTime();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE_BITS);
        SecretKey aesKey = keyGen.generateKey();
        long keyGenEnd = System.nanoTime();
        System.out.printf("AES Key Gen Time:    %.4f ms%n", (keyGenEnd - keyGenStart) / 1_000_000.0);

        // 2. Encryption/Decryption (Timed over N iterations)
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // --- Encryption ---
        long encryptTotalTime = 0;
        byte[] encryptedData = null;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            encryptedData = aesCipher.doFinal(data);
            long end = System.nanoTime();
            encryptTotalTime += (end - start);
        }
        System.out.printf("AES Encrypt Avg:     %.4f ms (%.4f ns)%n",
                (encryptTotalTime / (double) ITERATIONS) / 1_000_000.0,
                (encryptTotalTime / (double) ITERATIONS));

        // --- Decryption ---
        long decryptTotalTime = 0;
        byte[] decryptedData = null;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
            decryptedData = aesCipher.doFinal(encryptedData);
            long end = System.nanoTime();
            decryptTotalTime += (end - start);
        }
        System.out.printf("AES Decrypt Avg:     %.4f ms (%.4f ns)%n",
                (decryptTotalTime / (double) ITERATIONS) / 1_000_000.0,
                (decryptTotalTime / (double) ITERATIONS));


        // Verification
        if (!Arrays.equals(data, decryptedData)) {
            System.out.println("!!! AES TEST FAILED: Data mismatch !!!");
        } else {
            System.out.println("AES Test Passed (Data verified)");
        }
    }

    /**
     * Performs timing tests for RSA (Asymmetric).
     */
    public static void performAsymmetricTest(byte[] data) throws Exception {
        System.out.println("\n--- Asymmetric (RSA-" + RSA_KEY_SIZE_BITS + ") Test ---");

        // 1. Key Pair Generation (Timed once)
        long keyGenStart = System.nanoTime();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE_BITS);
        KeyPair rsaKeyPair = keyGen.generateKeyPair();
        PublicKey pubKey = rsaKeyPair.getPublic();
        PrivateKey privKey = rsaKeyPair.getPrivate();
        long keyGenEnd = System.nanoTime();
        System.out.printf("RSA Key Gen Time:    %.4f ms%n", (keyGenEnd - keyGenStart) / 1_000_000.0);

        // 2. Encryption/Decryption (Timed over N iterations)
        Cipher rsaCipher = Cipher.getInstance("RSA");

        // --- Encryption (with Public Key) ---
        long encryptTotalTime = 0;
        byte[] encryptedData = null;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encryptedData = rsaCipher.doFinal(data);
            long end = System.nanoTime();
            encryptTotalTime += (end - start);
        }
        System.out.printf("RSA Encrypt Avg:     %.4f ms (%.4f ns)%n",
                (encryptTotalTime / (double) ITERATIONS) / 1_000_000.0,
                (encryptTotalTime / (double) ITERATIONS));


        // --- Decryption (with Private Key) ---
        long decryptTotalTime = 0;
        byte[] decryptedData = null;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
            decryptedData = rsaCipher.doFinal(encryptedData);
            long end = System.nanoTime();
            decryptTotalTime += (end - start);
        }
        System.out.printf("RSA Decrypt Avg:     %.4f ms (%.4f ns)%n",
                (decryptTotalTime / (double) ITERATIONS) / 1_000_000.0,
                (decryptTotalTime / (double) ITERATIONS));


        // Verification
        if (!Arrays.equals(data, decryptedData)) {
            System.out.println("!!! RSA TEST FAILED: Data mismatch !!!");
        } else {
            System.out.println("RSA Test Passed (Data verified)");
        }
    }
}