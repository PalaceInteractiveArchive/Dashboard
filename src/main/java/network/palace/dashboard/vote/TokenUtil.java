package network.palace.dashboard.vote;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Marc on 1/15/17.
 */
public class TokenUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String newToken() {
        return new BigInteger(130, RANDOM).toString(32);
    }
}
