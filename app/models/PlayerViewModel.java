package models;

/**
 * Created by daawar on 11/8/16.
 */
public class PlayerViewModel {
    public String playerId;
    public boolean isObserver;
    public PlayerViewModel(String id, boolean observer){
        this.playerId = id;
        this.isObserver = observer;
    }
}
