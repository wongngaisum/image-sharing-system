import java.util.*;
import java.text.*;

import org.json.simple.JSONObject;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * This is an user object that stores his/her information.
 */
public class User {

    private String userName, password, fullName, email, loginDate;
    private long phoneNo, failedCount;
    private boolean locked;

    /**
     * Create new account
     * @param userName user's username
     * @param password user's passsword
     * @param fullName user's full name
     * @param email user's email address
     * @param phoneNo user's phone number
     */
    User(String userName, String password, String fullName, String email, long phoneNo) {

        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phoneNo = phoneNo;
        failedCount = 0;
        locked = false;

        // No need, the last login time should not be updated during account creation.
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // loginDate = dateFormat.format(new Date());

    }

    /**
     * Restore account
     * @param userName user's username
     * @param password user's passsword
     * @param fullName user's full name
     * @param email user's email address
     * @param phoneNo user's phone number
     * @param failedCount fail login times
     * @param loginDate last login date
     * @param locked whether account has been locked
     */
    User(String userName, String password, String fullName, String email, long phoneNo, long failedCount, String loginDate, boolean locked) {

        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.failedCount = failedCount;
        this.locked = locked;
        this.loginDate = loginDate;

    }

    /**
     * Check password is correct or not
     * @param pw password input
     * @return result of login
     */
    public int login(String pw) {

        if (password.equals(pw) && !locked) {   // login success

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            loginDate = dateFormat.format(new Date());
            failedCount = 0;
            return 1;

        }

        if (++failedCount >= 3) {   // fail consecutively for >= 3 times

            locked = true;
            return 3;

        }

        return 2;   // wrong password

    }

    /**
     * Edit user information
     * @param password new password
     * @param fullName new full name
     * @param email new email address
     */
    public void editRecord(String password, String fullName, String email) {

        this.password = password;
        this.fullName = fullName;
        this.email = email;

    }

    /**
     * Set new password, reset locked status and failed login times
     * @param newPassword new password that user want to set
     */
    public void setPassword(String newPassword) {

        password = newPassword;
        locked = false;
        failedCount = 0;

    }

    /**
     * Get name of the user
     * @return user's name
     */
    public String getName() {

        return userName;

    }

    /**
     * Get last login date of the user
     * @return user's last login date
     */
    public String getLastLoginDate() {

        return loginDate;

    }

    /**
     * Generate a JSON object for this user record
     * @return a JSON object for this user record
     */
    public JSONObject genRecord() {

        JSONObject obj = new JSONObject();
        obj.put("username", userName);
        obj.put("hash_password", password);
        obj.put("Full Name", fullName);
        obj.put("Email", email);
        obj.put("Phone number", phoneNo);
        obj.put("Fail count", failedCount);
        obj.put("Last Login Date", loginDate);
        obj.put("Account locked", locked);
        return obj;

    }

}
