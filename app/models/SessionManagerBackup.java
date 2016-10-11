package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daawar on 9/16/16.
 */
public class SessionManagerBackup {
    private static Map<String, String> users = new HashMap<>();

    public static String getUser(String username){
        if(users.containsKey(username)){
            return users.get(username);
        }

        return null;
    }

    //Check if user exists in the users map
    public static boolean hasUser(String username){
        return users.containsKey(username);
    }

    //return false if user already exists
    public static boolean addUser(String username, String value){
        if(users.containsKey(username)){
            return false;
        }

        users.put(username, value);
        return true;
    }

    //return false if user does not exist
    public static boolean removeUser(String username){
        if(!users.containsKey(username)){
            return false;
        }

        users.remove(username);
        return true;
    }

    //return all users
    public static List<String> getAllUsers(){

        List<String> usersValues = new ArrayList<>();
        for(Map.Entry<String, String> entry : users.entrySet()){
            usersValues.add(entry.getValue());
        }
        return usersValues;
    }
}
