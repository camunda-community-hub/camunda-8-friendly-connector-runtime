package org.camunda.runtime.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.camunda.runtime.exception.TechnicalException;

public class CryptoUtils {
  private CryptoUtils() {}

  public static String encrypt(String value, byte[] publicKey) throws TechnicalException {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
      PublicKey publicKey2 = keyFactory.generatePublic(publicKeySpec);
      Cipher encryptCipher = Cipher.getInstance("RSA");
      encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey2);

      byte[] secretMessageBytes = value.getBytes(StandardCharsets.UTF_8);
      byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

      return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    } catch (NoSuchAlgorithmException
        | IllegalBlockSizeException
        | BadPaddingException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | InvalidKeyException e) {
      throw new TechnicalException("Error while encrypting secrets", e);
    }
  }

  public static String decrypt(String encrypted, byte[] privateKey) throws TechnicalException {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
      PrivateKey privateKey2 = keyFactory.generatePrivate(privateKeySpec);

      Cipher decryptCipher = Cipher.getInstance("RSA");
      decryptCipher.init(Cipher.DECRYPT_MODE, privateKey2);

      byte[] decryptedMessageBytes =
          decryptCipher.doFinal(Base64.getDecoder().decode(encrypted.getBytes()));
      return new String(decryptedMessageBytes, StandardCharsets.UTF_8);

    } catch (NoSuchAlgorithmException
        | IllegalBlockSizeException
        | BadPaddingException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | InvalidKeyException e) {
      throw new TechnicalException("Error while encrypting secrets", e);
    }
  }
}
