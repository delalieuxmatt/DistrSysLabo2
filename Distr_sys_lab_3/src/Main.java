import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.security.Signature;
import java.util.Base64;




void main() throws Exception {
    ex_1();
}



static class AESEncryption {
    public static void encrypt(SecretKey secretKey, String text) throws Exception {

        //Initialisatie vector (willekeurige niet geheime waarde die zorgt tekst1 + sleutel1 =/= tekst1 + sleutel2
        byte[] iv = new byte[16]; // 16 bytes voor AES blok grootte 16 bytes
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Encryptie
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //CBC --> elke blok cijfertekst afhankelijk maakt van voorgaande blokken (XOR met IV of vorige cijferblok)
        //PKCS5Padding --> laatste gegevensblokken exact blokgrootte van cipher (16B voor AES) bereiken, nodig voor CBC-modus.
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);//Voert de encryptie uit
        byte[] encrypted = cipher.doFinal(text.getBytes());

        System.out.println("Encrypted (Base64): " + Base64.getEncoder().encodeToString(encrypted));
        // Decryptie
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);//Voert de decryptie uit
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
    keyGen.init(256); // 256 for better security
    SecretKey secretKey = keyGen.generateKey();
    System.out.println("AES Key: " + Arrays.toString(secretKey.getEncoded()));
    String text = "Lorem ipsum dolor sit amet";
    AESEncryption.encrypt(secretKey, text);

    // Assymmetric key encryption
    // RSA key lengths 1024, 2048, 4096 bits
    // DSA key lengths 1024, 2048 bits --> Way longer than symmetric keys
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    keyPairGen.initialize(4096); // 4096 for better security
    KeyPair keyPair = keyPairGen.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();
    System.out.println(publicKey);

    // Encrypt with public key
    Cipher cipher = Cipher.getInstance("RSA");

    //PKCS1Padding voegt willekeurige opvulling toe om de veiligheid te vergroten.
    //ECB omdat in RSA gegevens in blokken versleutelt die zijn beperkt aan sleutelgrootte

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
    //De berekende hash wordt versleuteld met de private sleute
    System.out.println("Digital Signature (Base64): " + Base64.getEncoder().encodeToString(digitalSignature));

    // Verify signature with public key
    signature.initVerify(publicKey);
    signature.update(bytes2);
    //De ontvanger berekent de hash van de ontvangen data (person2 bytes) op dezelfde manier.
    boolean isVerified = signature.verify(digitalSignature);
    System.out.println("Signature verified: " + isVerified);
}

