package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daawar on 9/16/16.
 */
public class SessionManager {
    private static Map<String, List<String>> users = new HashMap<>();

    public static List<String> getUsers(String gameid){
        if(users.containsKey(gameid)){
            return users.get(gameid);
        }

        return null;
    }

    //Check if user exists in the users map
    public static boolean hasUser(String gameid, String username){
        if(users.containsKey(gameid))
        {
           return users.get(gameid).contains(username);
        }

        return false;
    }


    public static boolean addUser(String gameid, String username){
        if(users.containsKey(gameid)){
            users.get(gameid).add(username);
            return true;
        }

        users.put(gameid, new ArrayList<>());
        users.get(gameid).add(username);
        return true;
    }

    //return false if user does not exist
    public static boolean removeUser(String gameid, String username){
        if(!users.containsKey(gameid)){
            return false;
        }

        if(users.get(gameid).contains(username)){
            users.get(gameid).remove(username);
            return true;
        }
        return false;
    }

    //return all users
    public static List<String> getAllUsers(String gameid){

       if(users.containsKey(gameid)){
           return users.get(gameid);
       }
       return null;
    }
    //Remove map entry for the game
    public static boolean removeGame(String gameId){
        if(users.containsKey(gameId)){
            users.remove(gameId);
            return true;
        }
        return false;
    }
}