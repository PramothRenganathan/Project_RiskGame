package models;

import java.util.Date;

/**
 * Created by srijithkarippure on 10/11/16.
 */
public class Snapshot {

    private String gamePlayerId;
    private int turnNo;
    private int budget;
    private int personnel;
    private int capabilityBonus;
    private int capabilityPoints;
    private int timeTaken;
    private String moveType;
    private boolean moveStatus;
    private boolean skipTurnStatus;
    private String projectStepId;
    private String riskId;
    private String oopsId;
    private String oopsImpactId;
    private String surpriseId;
    private String surpriseImpactId;
    private int loanAmount;
    private boolean isProduction;

    private int oneTurn;
    private int twoTurn;


    public String getGamePlayerId() {
        return gamePlayerId;
    }

    public void setGamePlayerId(String gamePlayerId) {
        this.gamePlayerId = gamePlayerId;
    }

    public int getTurnNo() {
        return turnNo;
    }

    public void setTurnNo(int turnNo) {
        this.turnNo = turnNo;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getPersonnel() {
        return personnel;
    }

    public void setPersonnel(int personnel) {
        this.personnel = personnel;
    }

    public int getCapabilityBonus() {
        return capabilityBonus;
    }

    public void setCapabilityBonus(int capabilityBonus) {
        this.capabilityBonus = capabilityBonus;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(int timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getMoveType() {
        return moveType;
    }

    public void setMoveType(String moveType) {
        this.moveType = moveType;
    }

    public boolean isMoveStatus() {
        return moveStatus;
    }

    public void setMoveStatus(boolean moveStatus) {
        this.moveStatus = moveStatus;
    }

    public boolean isSkipTurnStatus() {
        return skipTurnStatus;
    }

    public void setSkipTurnStatus(boolean skipTurnStatus) {
        this.skipTurnStatus = skipTurnStatus;
    }

    public String getProjectStepId() {
        return projectStepId;
    }

    public void setProjectStepId(String projectStepId) {
        this.projectStepId = projectStepId;
    }

    public String getRiskId() {
        return riskId;
    }

    public void setRiskId(String riskId) {
        this.riskId = riskId;
    }

    public String getOopsId() {
        return oopsId;
    }

    public void setOopsId(String oopsId) {
        this.oopsId = oopsId;
    }

    public String getOopsImpactId() {
        return oopsImpactId;
    }

    public void setOopsImpactId(String oopsImpactId) {
        this.oopsImpactId = oopsImpactId;
    }

    public String getSurpriseId() {
        return surpriseId;
    }

    public void setSurpriseId(String surpriseId) {
        this.surpriseId = surpriseId;
    }

    public String getSurpriseImpactId() {
        return surpriseImpactId;
    }

    public void setSurpriseImpactId(String surpriseImpactId) {
        this.surpriseImpactId = surpriseImpactId;
    }

    public int getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(int loanAmount) {
        this.loanAmount = loanAmount;
    }

    public boolean isProduction() {
        return isProduction;
    }

    public void setProduction(boolean production) {
        isProduction = production;
    }

    public int getCapabilityPoints() {
        return capabilityPoints;
    }

    public void setCapabilityPoints(int capabilityPoints) {
        this.capabilityPoints = capabilityPoints;
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
}
