package models;

import java.util.List;

/**
 * Created by daawar on 9/16/16.
 */
public class WebSocketData {
    /**
     * Type of data
     */
    public String type;
    /**
     * users joined
     */
    public List<String> joinedUsers;
    /**
     * Leaving users
     */
    public String leavingUser;
    /**
     * Players
     */
    public String player;
    /**
     * Step names
     */
    public String stepName;
    /**
     * message to use for display
     */
    public String message;
    /**
     * Turn Number
     */
    public String turnNumber;
    /**
     * Current player whose turn is going on
     */
    public String currentPlayer;
}
