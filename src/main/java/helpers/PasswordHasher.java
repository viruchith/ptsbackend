package helpers;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
    public static String toSHA512(String password){
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(password.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String passwordHash = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (passwordHash.length() < 32) {
                passwordHash = "0" + passwordHash;
            }

            // return the HashText
            return passwordHash;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean verifyPassword(String password,String passswordHash){
        return PasswordHasher.toSHA512(password).equals(passswordHash);
    }
}
