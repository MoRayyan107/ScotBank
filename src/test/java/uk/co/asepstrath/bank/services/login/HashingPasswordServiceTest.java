package uk.co.asepstrath.bank.services.login;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class HashingPasswordServiceTest {

    @Test
    void testHashedPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pwd = "password123";
        String hashedPassword = HashingPasswordService.hashPassword(pwd);

        assertTrue(HashingPasswordService.verifyPassword(pwd, hashedPassword), "Password Verification failed!");

        assertFalse(HashingPasswordService.verifyPassword("Blah", hashedPassword));

    }

    @Test
    void testUniquePassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pwd = "password123";
        String hashedPassword = HashingPasswordService.hashPassword(pwd);
        String hashedPassword2 = HashingPasswordService.hashPassword(pwd);

        assertNotEquals(hashedPassword, hashedPassword2);
    }

}