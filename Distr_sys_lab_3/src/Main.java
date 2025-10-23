import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.security.Signature;
import java.util.Base64;




void main() throws Exception {
    ex_1();

    ex_2();
}



static class AESEncryption {
    public static void encrypt(String text) throws Exception {

        // Sleutel en IV genereren
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();

        byte[] iv = new byte[16]; // 16 bytes voor AES
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Encryptie
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(text.getBytes());

        System.out.println("Encrypted (Base64): " + Base64.getEncoder().encodeToString(encrypted));

        // Decryptie
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        System.out.println("Decrypted: " + new String(decrypted));
    }
}


void ex_1() throws Exception {
    Person person1 = new Person("Alice", "Wonderland", "123456789");
    Person person2 = new Person("Bob", "Builderland", "987654321");
    Person person1Fake = new Person("Alice", "Wonderland", "123456788"); // Last digit different
    byte[] bytes1 = person1.getBytes();
    byte[] bytes2 = person2.getBytes();
    byte[] bytes1Fake = person1Fake.getBytes();

    MessageDigest sha = MessageDigest.getInstance("SHA-256");

    // No difference in bytes, same hash
    System.out.println(Arrays.toString(sha.digest(bytes1)));
    System.out.println(Arrays.toString(sha.digest(bytes1Fake)));

    // AES encryption
    // Lengths 128, 192, 256 bits
    // TripleDES lengths 112, 168 bits
    // Symmetric key encryption
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey secretKey = keyGen.generateKey();
    System.out.println("AES Key: " + Arrays.toString(secretKey.getEncoded()));
    String text = "Lorem ipsum dolor sit amet";
    AESEncryption.encrypt(text);

    // Assymmetric key encryption
    // RSA key lengths 1024, 2048, 4096 bits
    // DSA key lengths 1024, 2048 bits --> Way longer than symmetric keys
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    keyPairGen.initialize(2048);
    KeyPair keyPair = keyPairGen.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();

    // Encrypt with public key
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encryptedData = cipher.doFinal(text.getBytes());
    System.out.println("Encrypted with RSA (Base64): " + Base64.getEncoder().encodeToString(encryptedData));
    // Decrypt with private key
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decryptedData = cipher.doFinal(encryptedData);
    System.out.println("Decrypted with RSA: " + new String(decryptedData));

    // Time difference between symmetric and asymmetric
    // Symmetric is way faster

    //Sign person2 with private key
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey);
    signature.update(bytes2);
    byte[] digitalSignature = signature.sign();
    System.out.println("Digital Signature (Base64): " + Base64.getEncoder().encodeToString(digitalSignature));

    // Verify signature with public key
    signature.initVerify(publicKey);
    signature.update(bytes2);
    boolean isVerified = signature.verify(digitalSignature);
    System.out.println("Signature verified: " + isVerified);
}


void ex_2() throws Exception {

}

