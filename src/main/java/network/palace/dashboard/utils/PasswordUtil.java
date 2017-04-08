package network.palace.dashboard.utils;

/**
 * Created by Marc on 4/8/17.
 */
public class PasswordUtil {
    private BCrypt bCrypt = new BCrypt();

    public String hashPassword(String password, String salt) {
        return bCrypt.hashpw(password, salt);
    }

    public boolean validPassword(String plain, String hashed) {
        return bCrypt.checkpw(plain, hashed);
    }

    public String getNewSalt() {
        return bCrypt.gensalt();
    }

    public boolean isStrongEnough(String plain) {
        return plain.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$") && plain.length() >= 8;
    }
}
