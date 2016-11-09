package models;

/**
 * Created by srijithkarippure on 9/5/16.
 */
public class RiskCard {
    private String riskId;
    private String riskDescription;
    private int personnel;
    private int budget;
    private int totalMitigationSteps;
    private int performedMitigationSteps;

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    private boolean complete;

    public String getRiskId() {
        return riskId;
    }

    public void setRiskId(String riskId) {
        this.riskId = riskId;
    }

    public String getRiskDescription() {
        return riskDescription;
    }

    public void setRiskDescription(String riskDescription) {
        this.riskDescription = riskDescription;
    }

    public int getPersonnel() {
        return personnel;
    }

    public void setPersonnel(int personnel) {
        this.personnel = personnel;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getTotalMitigationSteps() {
        return totalMitigationSteps;
    }

    public void setTotalMitigationSteps(int totalMitigationSteps) {
        this.totalMitigationSteps = totalMitigationSteps;
    }

    public int getPerformedMitigationSteps() {
        return performedMitigationSteps;
    }

    public void setPerformedMitigationSteps(int performedMitigationSteps) {
        this.performedMitigationSteps = performedMitigationSteps;
    }
}
