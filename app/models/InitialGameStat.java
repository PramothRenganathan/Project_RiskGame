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
    private boolean skipTurn;
    private int oneTurn;
    private int twoTurn;
    private int level2Bonus;
    private int level3Bonus;
    private boolean observer=false;

    public boolean isObserver() {
        return observer;
    }

    public void setObserver(boolean observer) {
        this.observer = observer;
    }

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

    public boolean isSkipTurn() {
        return skipTurn;
    }

    public void setSkipTurn(boolean skipTurn) {
        this.skipTurn = skipTurn;
    }

    public int getOneTurn() {
        return oneTurn;
    }

    public void setOneTurn(int oneTurn) {
        this.oneTurn = oneTurn;
    }

    public int getTwoTurn() {
        return twoTurn;
    }

    public void setTwoTurn(int twoTurn) {
        this.twoTurn = twoTurn;
    }

    public int getLevel2Bonus() {
        return level2Bonus;
    }

    public void setLevel2Bonus(int level2Bonus) {
        this.level2Bonus = level2Bonus;
    }

    public int getLevel3Bonus() {
        return level3Bonus;
    }

    public void setLevel3Bonus(int level3Bonus) {
        this.level3Bonus = level3Bonus;
    }
}
