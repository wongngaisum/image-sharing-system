import java.security.*;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * A hash generator that encrypts data.
 */
public class Hash {

    /**
     * Generate hashed code of a string
     * @param text the string that will be encrypted
     * @return encrypted string
     */
    public String getHashCode(String text) {

        try {

            MessageDigest alg = MessageDigest.getInstance("MD5");
            alg.update(text.getBytes());
            return bytes2Hex(alg.digest());

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    /**
     * Encode byte array values to hexadecimal
     * @param bytes byte array
     * @return hex values
     */
    public String bytes2Hex(byte[] bytes) {

        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();

    }

}