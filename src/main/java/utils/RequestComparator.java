package utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RequestComparator {

    /**
     * Compares the SHA-1 hashes of two request strings to determine if they match.
     *
     * @param originalRequest      The original request string.
     * @param assumedEditedRequest The potentially edited request string.
     * @return true if the SHA-1 hashes match; false otherwise.
     */
    public static boolean isRequestEdited(String originalRequest, String assumedEditedRequest) {
        try {
            String hashOriginal = computeSHA1HashHex(originalRequest);
            String hashAssumedEdited = computeSHA1HashHex(assumedEditedRequest);

            // Compare the two hash byte arrays
            System.out.println("hashOriginal" + hashOriginal);
            System.out.println("hashAssumedEdited" + hashAssumedEdited);

            return true;

        } catch (NoSuchAlgorithmException e) {
            // Handle the exception as per your application's requirement
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Computes the SHA-1 hash of a given input string.
     *
     * @param input The input string to hash.
     * @return The SHA-1 hash as a byte array.
     * @throws NoSuchAlgorithmException If SHA-1 algorithm is not available.
     */
    private static byte[] computeSHA1Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.update(input.getBytes(StandardCharsets.UTF_8));
        return crypt.digest();
    }

    /**
     * (Optional) Computes the SHA-1 hash of a string and returns it as a hexadecimal string.
     *
     * @param input The input string to hash.
     * @return The SHA-1 hash as a hexadecimal string.
     * @throws NoSuchAlgorithmException If SHA-1 algorithm is not available.
     */
    private static String computeSHA1HashHex(String input) throws NoSuchAlgorithmException {
        byte[] hashBytes = computeSHA1Hash(input);
        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, hashBytes);
        // Convert message digest into hex value
        String hashText = no.toString(16);
        // Add preceding 0s to make it 40 characters (SHA-1 produces 160 bits)
        while (hashText.length() < 40) {
            hashText = "0" + hashText;
        }
        return hashText;
    }

}