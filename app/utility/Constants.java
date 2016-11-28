package utility;

/**
 * Created by srijithkarippure on 9/27/16.
 */
public class Constants {

    public static final String HOSTED_STATUS = "HOSTED";
    public static final String RUNNING_STATUS = "RUNNING";
    public static final String COMPLETE_STATUS = "COMPLETE";
    public static final String USERNAME = "username";
    public static final String ERRORMSG = "errormsg";
    public static final String PUBLIC_IMAGES = "public/images/";
    public static final String MESSAGE = "message";
    public static final String FAILURE = "failure";
    public static final String GAMEPLAYERID = "gameplayerid";
    public static final String GAMEID = "gameid";
    public static final String SUCCESS = "success";
    public static final String CONFIG_PROJECT_STEP_MAPPING_ID = "config_project_step_mapping_id";
    public static final String CONFIG_ID = "config_id";
    public static final String CAPABILITY_BONUS = "capability_bonus";
    public static final String CAPABILITY_POINTS = "capability_points";
    public static final String STATUS = "status";
    public static final String BUDGET = "budget";
    public static final String PERSONNEL = "personnel";
    public static final String PROJECT_STEP_NAME = "project_step_name";
    public static final String LEVEL = "level";
    public static final String PRE_REQUISITE = "pre_requisite";
    public static final String APPLICATION_JAVASCRIPT = "application/javascript";

    /**
     * Different kinds of steps performed
     */
    public enum PerformStep {
        PROJECTSTEP,
        OOPS,
        SURPRISE,
        RISK
    }

    //GAME RELATED CONSTANTS
    public static final int INITIAL_BUDGET = 35000;
    public static final int INITIAL_CAPABILITY_POINTS = 0;
    public static final int INITIAL_CAPABILITY_BONUS = 0;

}
