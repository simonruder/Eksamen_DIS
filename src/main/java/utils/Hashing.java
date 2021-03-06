package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.util.encoders.Hex;

public final class Hashing {

  private String salt;

  //SIMON - med denne metode sætter jeg salt, som kan være forskellig fra bruger til bruger, da jeg bruger created_at til passwords, og current_time til token
    public void setSalt(String salt) {
        this.salt = salt;
    }


  // TODO: You should add a salt and make this secure : FIXED
  // TODO: Bruge created_at som SALT :FIXED

  //SIMON - Jeg benytter ikke Md5 i systemet, men gemmer algoritmen, hvis kunden finder Md5 mere anvendelig end SHA256
  public static String md5(String rawString) {
    try {

      // We load the hashing algoritm we wish to use.
      MessageDigest md = MessageDigest.getInstance("MD5");

      // We convert to byte array
      byte[] byteArray = md.digest(rawString.getBytes());

      // Initialize a string buffer
      StringBuffer sb = new StringBuffer();

      // Run through byteArray one element at a time and append the value to our stringBuffer
      for (int i = 0; i < byteArray.length; ++i) {
        sb.append(Integer.toHexString((byteArray[i] & 0xFF) | 0x100).substring(1, 3));
      }

      //Convert back to a single string and return
      return sb.toString();

    } catch (java.security.NoSuchAlgorithmException e) {
      //If somethings breaks
      System.out.println("Could not hash string");
    }

    return null;
  }

  // TODO: You should add a salt and make this secure : FIXED
  public static String sha(String rawString) {
    try {
      // We load the hashing algoritm we wish to use.
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // We convert to byte array
      byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

      // We create the hashed string
      String sha256hex = new String(Hex.encode(hash));

      // And return the string
      return sha256hex;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return rawString;
  }
  /**
   * Hash string with MD5 hashing
   * @param str input string
   * @return MD5 hash of string
   */
  public  String hashWithMd5(String str) {
    return md5(str);
  }

  public  String hashWithSha(String str) {
    return sha(str);
  }

  /**
   * Hash string AND salt with MD5 hash
   * @param str input string
   * @return MD5 hashed of string
   */

  //SIMON - Denne metode tager en Salt-streng og tilføjer den til passwordet, og derefter hasher den det ved at kalde hashWithMd5
  public String md5WithSalt(String str){


    String md5Salt = str + salt;


    return hashWithMd5(md5Salt);
  }

  //SIMON - Denne metode tager en Salt-streng og tilføjer den til passwordet, og derefter hasher den det ved at kalde hashWithSha
  public String shaWithSalt(String str){


    String shaSalt = str + salt;


    return hashWithSha(shaSalt);
  }

}