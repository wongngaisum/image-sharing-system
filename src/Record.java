import java.util.*;
import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * @author WongNgaiSum 3035380875
 * @version 1.0
 * This is an record object that manages the record of all users.
 * It is used to load records from / store records in file.
 */
public class Record {

    private String userName, password, fullName, email, loginDate;
    private long phoneNo, failedCount;
    private boolean locked;

    private ArrayList<User> users = new ArrayList<User>();

    /**
     * Load data from file
     * @param fileName file that stores users' records
     * @return array of users
     */
    public ArrayList<User> loadRecord(String fileName) {

        users = new ArrayList<User>();

        File f = new File(System.getProperty("user.dir") + "/" + fileName);

        if (!f.exists()) {

            try {

                f.createNewFile();  // create new file if file doesn't exist

            } catch (Exception e) {
            }


        } else {

            JSONParser parser = new JSONParser();

            try {

                Object obj = parser.parse(new FileReader(System.getProperty("user.dir") + "/" + fileName));

                JSONObject jsonObject = (JSONObject) obj;
                JSONArray userList = (JSONArray) jsonObject.get("user_array");

                for (Object anUserList : userList) {

                    obj = parser.parse(anUserList.toString());
                    jsonObject = (JSONObject) obj;

                    userName = (String) jsonObject.get("username");
                    password = (String) jsonObject.get("hash_password");
                    fullName = (String) jsonObject.get("Full Name");
                    email = (String) jsonObject.get("Email");
                    phoneNo = (long) jsonObject.get("Phone number");
                    failedCount = (long) jsonObject.get("Fail count");
                    loginDate = (String) jsonObject.get("Last Login Date");
                    locked = (boolean) jsonObject.get("Account locked");

                    users.add(new User(userName, password, fullName, email, phoneNo, failedCount, loginDate, locked));

                }

            } catch (Exception e) {
            }

        }

        return users;

    }

    /**
     * Store user records in file
     * @param userArray array of users
     * @param fileName the file user wants data to store in
     */
    public void storeRecord(ArrayList<User> userArray, String fileName) {

        users = userArray;

        JSONObject obj = new JSONObject();
        JSONArray user = new JSONArray();

        for (User u : users)
            user.add(u.genRecord());    // add all user records to an array

        obj.put("user_array", user);    // name the array "user_array"

        try {

            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/" + fileName);

            file.write(obj.toJSONString()); // write data to file
            file.flush();   // force buffered output bytes to be written out

        } catch (Exception e) {
        }

    }

}
