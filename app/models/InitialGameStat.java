package models;

import java.util.List;

/**
 * Created by srijithkarippure on 9/5/16.
 */
public class InitialGameStat {

    private List<Phase> phases;
    private int resources;
    private int budget;
    private int capabilityPoints;
    private int capabilityBonus;
    private String gameId;
    private String userName;
    private int loanAmount;

    public int getTimeForEachMove() {
        return timeForEachMove;
    }

    public void setTimeForEachMove(int timeForEachMove) {
        this.timeForEachMove = timeForEachMove;
    }

    private int timeForEachMove;

    public int getStepsForEachPlayer() {
        return stepsForEachPlayer;
    }

    public void setStepsForEachPlayer(int stepsForEachPlayer) {
        this.stepsForEachPlayer = stepsForEachPlayer;
    }

    private int stepsForEachPlayer;

    public int getTurnNo() {
        return turnNo;
    }

    public void setTurnNo(int turnNo) {
        this.turnNo = turnNo;
    }

    private int turnNo;

    public int getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(int loanAmount) {
        this.loanAmount = loanAmount;
    }



    public List<Phase> getPhases() {
        return phases;
    }

    public void setPhases(List<Phase> phases) {
        this.phases = phases;
    }

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getCapabilityPoints() {
        return capabilityPoints;
    }

    public void setCapabilityPoints(int capabilityPoints) {
        this.capabilityPoints = capabilityPoints;
    }

    public int getCapabilityBonus() {
        return capabilityBonus;
    }

    public void setCapabilityBonus(int capabilityBonus) {
        this.capabilityBonus = capabilityBonus;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
