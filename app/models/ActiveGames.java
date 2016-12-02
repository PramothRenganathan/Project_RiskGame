package models;

/**
 * Created by nivas on 11/10/16.
 */
public class ActiveGames {
    String gameid;
    String status;
    String gametime;
    String istimebound;
    String hostId;
    String fullName;

    public String getGameid() {
        return gameid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGametime() {
        return gametime;
    }

    public void setGametime(String gametime) {
        this.gametime = gametime;
    }

    public String getIstimebound() {
        return istimebound;
    }

    public void setIstimebound(String istimebound) {
        this.istimebound = istimebound;
    }

}
