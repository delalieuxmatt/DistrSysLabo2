## AES Cipher Modes and Padding in Java

```java
Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
```

This means:

* **1st** → encryption algorithm
* **2nd** → mode of operation
* **3rd** → padding scheme

---

### AES Modes

| Mode    | Description                                                                   | IV | Secure?             |
| ------- | ----------------------------------------------------------------------------- | -- | ------------------- |
| **ECB** | Each block encrypted separately (identical plaintext → identical ciphertext). | ❌  | ❌ No                |
| **CBC** | Each block XORed with previous ciphertext block. Needs random IV.             | ✅  | ✅ Yes               |
| **CFB** | Stream-like mode, uses previous ciphertext to encrypt next block.             | ✅  | ✅ Yes               |
| **OFB** | Generates keystream independent of plaintext.                                 | ✅  | ✅ Yes               |
| **CTR** | Encrypts counters; fast and parallel.                                         | ✅  | ✅ Yes               |
| **GCM** | Like CTR but with authentication (integrity check).                           | ✅  | ✅ Yes (recommended) |

### Padding Schemes

| Padding             | Description                                                            |
| ------------------- | ---------------------------------------------------------------------- |
| **NoPadding**       | Input length must be multiple of 16 bytes                              |
| **PKCS5Padding**    | Fills with bytes equal to padding length (e.g., `0x04 0x04 0x04 0x04`) |
| **PKCS7Padding**    | Same as PKCS5 for AES                                                  |
| **ISO10126Padding** | Deprecated, random fill then padding length                            |

**Default if not specified:**

`Cipher.getInstance("AES")` → uses **AES/CBC/PKCS5Padding** 
`Cipher.getInstance("RSA")` → uses **RSA/ECB/PKCS1Padding**
---
