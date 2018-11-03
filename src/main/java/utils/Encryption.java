package utils;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better: FIXED
     // char[] key = Config.getEncryptionKeyArray(); //Gemt i config filen med en anden kode.

      //Laver en String med en Key i config.json, denne hentes ned
      String encryptKey = Config.getEncryptKeyString();
      //Konverterer Stringen til en Char-array, som kan bruges i XOR-krypteringen
      char[] key = encryptKey.toCharArray();

      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on? : FIXED
      //Bundet op på binære tal, hvor de lægges sammen via , key og string har en b^inær værdi som lægges sammen
      for (int i = 0; i < rawString.length(); i++) {
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
      } //this is encrypted består af chars ^lægger de binære værdier sammen

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
