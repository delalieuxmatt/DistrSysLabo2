## AES Cipher Modes and Padding in Java

```java
Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
```

This means:

* **1st** → encryption algorithm
* **2nd** → mode of operation
* **3rd** → padding scheme
---

### Modes

| Mode    | Description                                                                   | IV | Secure?             |
| ------- | ----------------------------------------------------------------------------- | -- | ------------------- |
| **ECB** | Each block encrypted separately (identical plaintext → identical ciphertext). | ❌  | ❌ No                |
| **CBC** | Each block XORed with previous ciphertext block. Needs random IV.             | ✅  | ✅ Yes               |
| **CFB** | Stream-like mode, uses previous ciphertext to encrypt next block.             | ✅  | ✅ Yes               |
| **OFB** | Generates keystream independent of plaintext.                                 | ✅  | ✅ Yes               |
| **CTR** | Encrypts counters; fast and parallel.                                         | ✅  | ✅ Yes               |
| **GCM** | Like CTR but with authentication (integrity check).                           | ✅  | ✅ Yes (recommended) |

### AES Initialization Vector (IV)

An **IV (Initialization Vector)** is random data added at the start of encryption to ensure that the same plaintext produces **different ciphertexts** each time.

Without it, encryption would be deterministic — leaking patterns (especially dangerous in modes like CBC or CTR).

### Padding Schemes
Padding fills up the last block of plaintext so its length matches the block size required by the cipher (for AES, that’s 16 bytes).

| Padding             | Description                                                            |
| ------------------- | ---------------------------------------------------------------------- |
| **NoPadding**       | Input length must be multiple of 16 bytes                              |
| **PKCS5Padding**    | Fills with bytes equal to padding length (e.g., `0x04 0x04 0x04 0x04`) |
| **PKCS7Padding**    | Same as PKCS5 for AES                                                  |
| **ISO10126Padding** | Deprecated, random fill then padding length                            |

**Default if not specified:**

`Cipher.getInstance("AES")` → uses **AES/ECB/PKCS5Padding** 

---

### **Symmetric (AES-256) Test**

- **AES Key Generation Time:** 24.9813 ms
- **AES Encryption Average:** 0.0036 ms (3648.4450 ns)
- **AES Decryption Average:** 0.0040 ms (3987.4210 ns)
- ✅ **AES Test Passed (Data verified)**

---

### **Asymmetric (RSA-2048) Test**

- **RSA Key Generation Time:** 273.4660 ms
- **RSA Encryption Average:** 0.0534 ms (53,369.5530 ns)
- **RSA Decryption Average:** 0.9456 ms (945,578.7960 ns)
- ✅ **RSA Test Passed (Data verified)**


---
