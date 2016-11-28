package models;

import java.util.Date;

/**
 * Game related fields
 */
public class Game{

    private String gameId;
    private Date startTime;
    private Date endTime;
    private String name;
    private int companyId;
    private int productId;
    private boolean isTimeBound;
    private int stepsForEachPlayer;
    private int timeForEachMove;
    private String host;
    private int initialResources;
    private int initialBudget;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public boolean isTimeBound() {
        return isTimeBound;
    }

    public void setTimeBound(boolean timeBound) {
        isTimeBound = timeBound;
    }

    public int getStepsForEachPlayer() {
        return stepsForEachPlayer;
    }

    public void setStepsForEachPlayer(int stepsForEachPlayer) {
        this.stepsForEachPlayer = stepsForEachPlayer;
    }

    public int getTimeForEachMove() {
        return timeForEachMove;
    }

    public void setTimeForEachMove(int timeForEachMove) {
        this.timeForEachMove = timeForEachMove;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getInitialResources() {
        return initialResources;
    }

    public void setInitialResources(int initialResources) {
        this.initialResources = initialResources;
    }

    public int getInitialBudget() {
        return initialBudget;
    }

    public void setInitialBudget(int initialBudget) {
        this.initialBudget = initialBudget;
    }
}