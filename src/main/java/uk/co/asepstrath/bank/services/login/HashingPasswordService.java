package uk.co.asepstrath.bank.services.login;


import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import static uk.co.asepstrath.bank.Constants.*;

public class HashingPasswordService {

    private HashingPasswordService() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates a random salt
     *
     * @return byte array of salt
     */

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Hashes Password with generated salt
     *
     * @param password, the password to hash
     * @return a String containing the salt and Hash, seperated by ':'
     */

    public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = generateSalt();
        byte[] hashPassword = hashPasswordWithSalt(password, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashPassword);
    }

    /**
     * Hashes Password using given salt
     *
     * @param password, the password to hash.
     * @param salt,     the salt used for generating the hash password
     * @return Hashed password as a byte array
     */

    private static byte[] hashPasswordWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION, KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(spec).getEncoded();
    }

    /**
     * Verifies if given password matches the stored hashed password
     *
     * @param password,       plain text password
     * @param hashedPassword, stored password in hash format '(salt:hash)'
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        String[] parts = hashedPassword.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid hashed password");
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedPassword = Base64.getDecoder().decode(parts[1]);

        byte[] receivedPassword = hashPasswordWithSalt(password, salt);
        return Arrays.equals(expectedPassword, receivedPassword);
    }
}
