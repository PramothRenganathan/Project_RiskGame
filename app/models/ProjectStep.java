package models;

/**
 * Created by srijithkarippure on 9/5/16.
 */
public class ProjectStep {
    String projectStepId;
    String projectStepName;
    String projectStepDescription;
    int level;
    String preRequisite;
    int budget;
    int personnel;
    int capabilityPoints;
    int capabilityBonus;
    private boolean status;


    public String getProjectStepId() {
        return projectStepId;
    }

    public void setProjectStepId(String projectStepId) {
        this.projectStepId = projectStepId;
    }

    public String getProjectStepName() {
        return projectStepName;
    }

    public void setProjectStepName(String projectStepName) {
        this.projectStepName = projectStepName;
    }

    public String getProjectStepDescription() {
        return projectStepDescription;
    }

    public void setProjectStepDescription(String projectStepDescription) {
        this.projectStepDescription = projectStepDescription;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPreRequisite() {
        return preRequisite;
    }

    public void setPreRequisite(String preRequisite) {
        this.preRequisite = preRequisite;
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


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
