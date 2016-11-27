package utility;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.Play;
import play.db.DB;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srijithkarippure on 9/5/16.
 */
public class GameUtility {

    /**
     * Private constructor
     */
    private GameUtility(){
        //Do Nothing. All static methods. No initialization required for this class
    }
    public static final Logger logger = Logger.getLogger(GameUtility.class.getName());
    /**
     * Web socket mapping for players stored in the map
     */
    public static Map<String,List<String>> webSocketMapping = new HashMap<>();



    /**
     * Get previous step details of the player
     * @param gamePlayerId
     * @param turnNo
     * @return
     */
    public static Snapshot getPreviousSnapshot(String gamePlayerId, int turnNo) {
        logger.log(Level.FINE, "In Getting previous step snapshot");
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT budget,personnel,capability_bonus,capability_points,skip_turn_status,turn_no,isProduction FROM GAME_MOVES_SNAPSHOT WHERE game_player_id = ? and turn_no = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            stmt.setInt(2,turnNo);
            ResultSet rs = stmt.executeQuery();
            Snapshot step = new Snapshot();
            while(rs.next()){
                step.setBudget(rs.getInt(Constants.BUDGET));
                step.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                step.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                step.setPersonnel(rs.getInt(Constants.PERSONNEL));
                step.setSkipTurnStatus(rs.getBoolean("skip_turn_status"));
                step.setProduction(rs.getBoolean("isProduction"));
                step.setTurnNo(turnNo);
            }
            logger.log(Level.FINE, "Done getting prevous snapshot");
            logger.log(Level.FINE , "Budget:" + step.getBudget() + " Personnel:"+ step.getPersonnel());
            return step;

        }catch(Exception e){
            logger.log(Level.SEVERE, "Error while getting snapshot:" + e);
            return null;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Get current step details
     * @param body
     * @return
     */
    public static Snapshot getCurrentDetailsFromTheUser(JsonNode body) {
        try {
            String type = body.get("type").asText();
            String id = body.get("id").asText();
            int turnNo = body.get("turnno").asInt();
            int budget = body.get(Constants.BUDGET).asInt();
            int capabilityBonus = body.get("capabilitybonus").asInt();
            int capabilityPoints = body.get("capabilitypoints").asInt();
            int personnel = body.get(Constants.PERSONNEL).asInt();
            int timeTaken = body.get("timetaken").asInt();
            boolean skipTurn = body.get("skipturn").asBoolean();
            int oneTurn = body.get("oneturn").asInt();
            int twoTurn = body.get("twoturn").asInt();
            int performedSteps = body.get("performedsteps").asInt();
            int totalSteps = body.get("totalsteps").asInt();

            Snapshot receivedSnapShot = new Snapshot();
            receivedSnapShot.setBudget(budget);
            receivedSnapShot.setPersonnel(personnel);
            receivedSnapShot.setCapabilityBonus(capabilityBonus);
            receivedSnapShot.setCapabilityPoints(capabilityPoints);
            receivedSnapShot.setTurnNo(turnNo);
            receivedSnapShot.setProjectStepId(id);
            receivedSnapShot.setMoveType(type);
            receivedSnapShot.setTimeTaken(timeTaken);
            receivedSnapShot.setSkipTurnStatus(skipTurn);
            receivedSnapShot.setOneTurn(oneTurn);
            receivedSnapShot.setTwoTurn(twoTurn);
            receivedSnapShot.setPerformedSteps(performedSteps);
            receivedSnapShot.setTotalSteps(totalSteps);
            return receivedSnapShot;
        }
        catch (Exception e){
            logger.log(Level.SEVERE,"In getting current details from user:" +  e);
            return null;
        }


    }

    /**
     *  Validate current step to check if user tampered data on frontend
     * @param previousStep
     * @param currentStep
     * @return
     */
    public static boolean validateStep(Snapshot previousStep, Snapshot currentStep) {
        try {
            if (previousStep.getBudget() != currentStep.getBudget())
                return false;
            if (previousStep.getCapabilityBonus() != currentStep.getCapabilityBonus())
                return false;
            if (previousStep.getCapabilityPoints() != currentStep.getCapabilityPoints())
                return false;
            if (previousStep.getTurnNo() != currentStep.getTurnNo() - 1)
                return false;

            return true;
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error while validating step:" + e);
            return false;
        }
    }

    /**
     * Insert the step into db
     * @param gamePlayerId
     * @param currentStep
     * @param moveType
     * @return
     */
    public static boolean performStep(String gamePlayerId, Snapshot currentStep,Constants.PerformStep moveType) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "INSERT INTO GAME_MOVES_SNAPSHOT (game_player_id,turn_no,budget,personnel,capability_bonus,time_taken,move_type,move_status,skip_turn_status,project_step_id,risk_id,oops_id,surprise_id,oops_impact_id,surprise_impact_id,loan_amount,isProduction,capability_points) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            stmt.setInt(2,currentStep.getTurnNo());//Turn number
            stmt.setInt(3,currentStep.getBudget());
            stmt.setInt(4,currentStep.getPersonnel() );
            stmt.setInt(5,currentStep.getCapabilityBonus());
            stmt.setInt(6,currentStep.getTimeTaken());//time taken
            stmt.setString(7, currentStep.getMoveType());//move type
            stmt.setBoolean(8,currentStep.isMoveStatus());//move Status
            stmt.setBoolean(9,currentStep.isSkipTurnStatus());//skip turn status
            if(moveType == Constants.PerformStep.PROJECTSTEP){
                stmt.setString(10,currentStep.getProjectStepId());//project Step Id
            }
            else
            {
                stmt.setNull(10,Types.VARCHAR);//risk id
            }

            if(moveType == Constants.PerformStep.OOPS){
                stmt.setString(12,currentStep.getOopsId());
                stmt.setString(10,currentStep.getProjectStepId());
            }
            else
            {
                stmt.setNull(12,Types.VARCHAR);//oops id
            }

            if(moveType == Constants.PerformStep.SURPRISE){
                stmt.setString(13,currentStep.getSurpriseId());//surprise id
                stmt.setString(10,currentStep.getProjectStepId());
            }
            else
            {
                stmt.setNull(13,Types.VARCHAR);//surprise id
            }

            if(moveType == Constants.PerformStep.RISK){
                stmt.setString(11,currentStep.getRiskId());

            }else {
                stmt.setNull(11, Types.VARCHAR);//risk id
            }


            stmt.setNull(14,Types.VARCHAR);//oops impact id
            stmt.setNull(15,Types.VARCHAR);//surprise impact id
            stmt.setInt(16,currentStep.getLoanAmount());
            stmt.setNull(17,Types.TINYINT);//Production status
            stmt.setInt(18,currentStep.getCapabilityPoints());
            int result = stmt.executeUpdate();
            return result > 0 ? true: false;
        }
        catch(Exception e){
            logger.log(Level.SEVERE, "Exception while updating project step" + e);
            return false;
        }finally {
            cleanUp(stmt,conn);
        }

    }


    /**
     * Insert a row into game player data
     * @param gameId
     * @param userName
     * @param isObserver
     * @return
     */
    public static String insertIntoGamePlayer(String gameId, String userName, boolean isObserver) {

        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String gamePlayerId = userName.split("@")[0] + "-" +  gameId;
            String query = "INSERT INTO GAME_PLAYER (game_player_id,game_id,player_id,isObserver,start_time,end_time) VALUES (?,?,?,?,?,?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            stmt.setString(2,gameId);
            stmt.setString(3,userName);
            stmt.setBoolean(4,isObserver);
            stmt.setNull(5,Types.DATE);
            stmt.setNull(6,Types.DATE);
            int rs = stmt.executeUpdate();

                return rs > 0 ? gamePlayerId : null;



        }catch (Exception e){
            logger.log(Level.SEVERE, "error while inserting into game player:" + e);
            return null;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Get the rules of the game
     * @param gameStat
     * @return
     */
    public static boolean getGameRules(InitialGameStat gameStat)
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            String configId = Play.application().configuration().getString(Constants.CONFIG_ID);
            conn = DB.getConnection();
            String query = "SELECT level2bonus,level3bonus FROM GAME_CONFIGURATIONS WHERE game_config_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,configId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                gameStat.setLevel2Bonus(rs.getInt("level2bonus"));
                gameStat.setLevel3Bonus(rs.getInt("level3bonus"));
            }
            return true;


        }catch (Exception e){
            logger.log(Level.SEVERE,"Error while getting rules" +  e);
            return false;

        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Identifies what action to be performed
     * @param level
     * @param capabilityBonus
     * @return
     */
    public static Constants.PerformStep getActiontobeTaken(int level,int capabilityBonus)
    {

        InitialGameStat gamerules = new InitialGameStat();
        Random rand = new Random();
        int randomnumber = rand.nextInt(100) + 1;
        getGameRules(gamerules);
        if(level == 2) {
            if((randomnumber + capabilityBonus) > gamerules.getLevel2Bonus()){
                return Constants.PerformStep.PROJECTSTEP;
            }
            else if (randomnumber + capabilityBonus > (gamerules.getLevel2Bonus() - 10)){
                return Constants.PerformStep.SURPRISE;
            }
            else
            {
                return Constants.PerformStep.OOPS;
            }

        }
        else if(level == 3){

            if((randomnumber + capabilityBonus) > gamerules.getLevel3Bonus()){
                return Constants.PerformStep.PROJECTSTEP;
            }
            else if (randomnumber + capabilityBonus > (gamerules.getLevel2Bonus() - 10)){
                return Constants.PerformStep.SURPRISE;
            }
            else
            {
                return Constants.PerformStep.OOPS;
            }
        }
        else{
            if(randomnumber > 40){
                return Constants.PerformStep.PROJECTSTEP;
            }
            else if (randomnumber > 30){
                return Constants.PerformStep.SURPRISE;
            }
            else{
                return Constants.PerformStep.OOPS;
            }

        }



    }

    /**
     * Get the project step details based on project step id
     * @param id
     * @return
     */
    public static ProjectStep getProjectStepDetails(String id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "select P.project_step_id, project_step_name, `level`, pre_requisite,budget, personnel, capability_points, capability_bonus from PROJECT_STEPS P JOIN CONFIG_PHASE_PROJECTSTEPS_MAPPING CPM on P.project_step_id = CPM.project_step_id and config_project_step_mapping_id= ?";
            stmt = conn.prepareStatement(query);

            stmt.setString(1,id);
            ResultSet rs = stmt.executeQuery();
            ProjectStep ps = null;
            while(rs.next()){
                ps = new ProjectStep();
                ps.setBudget(rs.getInt(Constants.BUDGET));
                ps.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                ps.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                ps.setPersonnel(rs.getInt(Constants.PERSONNEL));
                ps.setProjectStepName(rs.getString(Constants.PROJECT_STEP_NAME));
                ps.setLevel(rs.getInt(Constants.LEVEL));
                ps.setPreRequisite(rs.getString(Constants.PRE_REQUISITE));
            }
            return ps;

        }
        catch(Exception e){
            logger.log(Level.SEVERE, "Exception while retrieving project step:" + e);
            return null;
        }finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Check if the project step can be performed and make changes to resources for persisting
     * @param currentStep
     * @param projectStep
     * @return
     */
    public static boolean canProjectStepBePerformed(Snapshot currentStep, ProjectStep projectStep) {
        try {
            if (currentStep.getBudget() - projectStep.getBudget() < 0)
                return false;
            if (currentStep.getPersonnel() - projectStep.getPersonnel() < 0)
                return false;

            currentStep.setCapabilityBonus(currentStep.getCapabilityBonus() + projectStep.getCapabilityBonus());
            currentStep.setBudget(currentStep.getBudget() - projectStep.getBudget());
            currentStep.setCapabilityPoints(currentStep.getCapabilityPoints() + projectStep.getCapabilityPoints());
            currentStep.setPersonnel(currentStep.getPersonnel() - projectStep.getPersonnel());
            return true;
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error while checking if the project step can be performed:" + e);
            return false;
        }

    }


    /**
     * Perform the oops card
     * @param currentStep
     * @param currentOOPS
     * @return
     */
    public static boolean performOOPS(Snapshot currentStep,OOPS currentOOPS) {

        OOPS oops = new OOPS();
        List<OOPS> oopsList ;
        oopsList = generateOOPSCard();

        for(int i=0;i<oopsList.size();i++){

            if(currentStep.getBudget() - oopsList.get(i).getBudget() < 0)
                continue;
            if(currentStep.getCapabilityPoints() - oopsList.get(i).getCapabilityPoints() < 0 )
                continue;
            if(currentStep.getCapabilityBonus() - oopsList.get(i).getCapabilityBonus() < 0)
                continue;
            if(currentStep.getPersonnel() - oopsList.get(i).getResources() < 0)
                continue;
            oops = oopsList.get(i);
            break;
        }


        currentStep.setCapabilityBonus(currentStep.getCapabilityBonus() - oops.getCapabilityBonus());
        currentStep.setBudget(currentStep.getBudget() - oops.getBudget());
        currentStep.setCapabilityPoints(currentStep.getCapabilityPoints() - oops.getCapabilityPoints());
        currentStep.setPersonnel(currentStep.getPersonnel() - oops.getResources());
        currentStep.setCurrentStepResource(oops.getResources());
        currentStep.setMoveType("OOPS");
        currentStep.setOopsId(oops.getId());
        currentOOPS.setResources(oops.getResources());
        currentOOPS.setBudget(oops.getBudget());
        currentOOPS.setCapabilityPoints(oops.getCapabilityPoints());
        currentOOPS.setCapabilityBonus(oops.getCapabilityBonus());
        //Add pre-requisite step here
        return true;
    }

    /**
     * Perform surprise
     * @param currentStep
     * @param currentSurprise
     * @return
     */
    public static boolean performSurprise(Snapshot currentStep,SURPRISE currentSurprise) {

        SURPRISE surprise ;
        surprise = generateSurpriseCard();

        currentStep.setCapabilityBonus(currentStep.getCapabilityBonus() + surprise.getCapabilityBonus());
        currentStep.setBudget(currentStep.getBudget() + surprise.getBudget());
        currentStep.setCapabilityPoints(currentStep.getCapabilityPoints() + surprise.getCapabilityPoints());
        currentStep.setPersonnel(currentStep.getPersonnel() + surprise.getResources());
        currentStep.setMoveType("SURPRISE");
        currentStep.setSurpriseId(surprise.getId());
        currentStep.setCurrentStepResource(surprise.getResources());
        currentSurprise.setResources(surprise.getResources());
        currentSurprise.setBudget(surprise.getBudget());
        currentSurprise.setCapabilityPoints(surprise.getCapabilityPoints());
        currentSurprise.setCapabilityBonus(surprise.getCapabilityBonus());
        return true;
    }

    /**
     * Check if the game is complete
     * @param turnNo
     * @param gameId
     * @return
     */
    public static boolean isGameComplete(int turnNo, String gameId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT steps_for_each_player FROM GAME WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next() && rs.getInt("steps_for_each_player") == turnNo)
                return true;
            return false;
        }catch (Exception e){
            logger.log(Level.SEVERE, "Error while checking game complete:" + e);
            return false;
        }finally {
            cleanUp(stmt,conn);
        }

    }

    /**
     * Adds the returning resources
     * @param currentStep
     */
    public static void addReturningResources(Snapshot currentStep) {
        try {
            currentStep.setPersonnel(currentStep.getPersonnel() + currentStep.getOneTurn());//Resource already back
            currentStep.setOneTurn(currentStep.getTwoTurn());//Resources to be back in one turn
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error while adding resources:" + e);
        }
    }

    /**
     * Generating oopds card
     * @return
     */
    public static List<OOPS> generateOOPSCard()
    {
        List<OOPS> oopslist = new ArrayList<>();
        OOPS oopsobj = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DB.getConnection();


                String query = "SELECT * FROM OOPS ORDER BY RAND()";
                stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    oopsobj = new OOPS();
                    oopsobj.setId(rs.getString("oops_id"));
                    oopsobj.setBudget(rs.getInt(Constants.BUDGET));
                    oopsobj.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                    oopsobj.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                    oopsobj.setResources(rs.getInt(Constants.PERSONNEL));
                    oopslist.add(oopsobj);
                }

                return oopslist;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while generating oops card:" + e);
            return oopslist;
        } finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Generate surprise card
     * @return
     */
    public static SURPRISE generateSurpriseCard()
    {
        SURPRISE surpriseobj = new SURPRISE();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DB.getConnection();
            String query = "SELECT * FROM SURPRISE  ORDER BY RAND() LIMIT 1";
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                surpriseobj.setId(rs.getString("surprise_id"));
                surpriseobj.setBudget(rs.getInt(Constants.BUDGET));
                surpriseobj.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                surpriseobj.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                surpriseobj.setResources(rs.getInt(Constants.PERSONNEL));
            }

            return surpriseobj;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while generating surprise card:" + e);
            return surpriseobj;
        } finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Generate 5 random risk cards for players in the game
     * @param playersInTheGame
     * @param configId
     * @return
     */
    public static boolean generateRandomRiskCardsForPlayer(List<String> playersInTheGame, String configId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement insertStmt = null;
        try {
            conn = DB.getConnection();
            for (String player : playersInTheGame) {

                String query = "SELECT config_risk_mapping_id FROM CONFIG_RISK_MAPPING WHERE game_config_id = ? ORDER BY RAND() LIMIT 5";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, configId);
                ResultSet rs = stmt.executeQuery();
                String insertQuery = "INSERT INTO GAME_PLAYER_RISK_STATUS (game_player_id,risk_id,`status`) VALUES (?,?,?)";
                insertStmt = conn.prepareStatement(insertQuery);
                while (rs.next()) {
                    insertStmt.setString(1, player);
                    insertStmt.setString(2, rs.getString("config_risk_mapping_id"));
                    insertStmt.setBoolean(3, false);
                    insertStmt.addBatch();
                }
                int[] result = insertStmt.executeBatch();
                for (int i : result) {
                    if (i <= 0)
                        return false;//Error while inserting
                }

            }
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while generating risk card:" + e);
            return false;
        } finally {
            if(insertStmt!=null)
                try {
                    insertStmt.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error while generating oops card:" + e);
                }
            cleanUp(stmt,conn);
        }


    }

    /**
     * Get risk cards of player based on his id
     * @param gamePlayerId
     * @return
     */
    public static List<RiskCard> getRisks(String gamePlayerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<RiskCard> risks = new ArrayList<>();
        try{
            conn = DB.getConnection();
            String query = "SELECT gprs.risk_id,description,budget_to_mitigate,personnel_to_mitigate,gprs.status from RISKS r" +
                    " JOIN CONFIG_RISK_MAPPING crm on r.risk_id = crm.risk_id " +
                    "JOIN GAME_PLAYER_RISK_STATUS gprs ON gprs.risk_id = crm.config_risk_mapping_id and gprs.game_player_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            ResultSet rs = stmt.executeQuery();
            RiskCard rc = null;
            while(rs.next()){
                rc = new RiskCard();
                rc.setBudget(rs.getInt("budget_to_mitigate"));
                rc.setPersonnel(rs.getInt("personnel_to_mitigate"));
                rc.setComplete(rs.getBoolean(Constants.STATUS));
                rc.setRiskId(rs.getString("risk_id"));
                rc.setRiskDescription(rs.getString("description"));
                risks.add(rc);
            }
            return risks;

        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error while getting player risk cards:" + e);
            return risks;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Get the project steps for a phase id with player status
     * @param phaseId
     * @param gamePlayerId
     * @return
     */
    public static List<ProjectStep> getProjectSteps(String phaseId, String gamePlayerId  ){



        String query = "SELECT CPM.config_project_step_mapping_id,P.project_step_id, project_step_name, `level`," +
                " pre_requisite,budget, personnel, capability_points, capability_bonus,`status`" +
                " FROM CONFIG_PHASE_PROJECTSTEPS_MAPPING CPM" +
                " JOIN GAME_PLAYER_PROJECT_STEP_STATUS GPS on CPM.config_project_step_mapping_id = GPS.config_project_step_mapping_id" +
                " JOIN PROJECT_STEPS P on CPM.project_step_id = P.project_step_id and CPM.config_phase_mapping_id = ? and GPS.game_player_id = ?";

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<ProjectStep> projectSteps = new ArrayList<>();
        try {
            connection = DB.getConnection();
            stmt = connection.prepareStatement(query);
            stmt.setString(1,phaseId);
            stmt.setString(2,gamePlayerId);
            logger.log(Level.FINE, "Getting Project Steps");

            rs = stmt.executeQuery();



            while (rs.next()) {

                ProjectStep ps = new ProjectStep();
                ps.setProjectStepId(rs.getString(Constants.CONFIG_PROJECT_STEP_MAPPING_ID));
                ps.setProjectStepName(rs.getString(Constants.PROJECT_STEP_NAME));
                ps.setBudget(rs.getInt(Constants.BUDGET));
                ps.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                ps.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                ps.setLevel(rs.getInt(Constants.LEVEL));
                ps.setPersonnel(rs.getInt(Constants.PERSONNEL));
                ps.setPreRequisite(rs.getString(Constants.PRE_REQUISITE));
                ps.setStatus(rs.getBoolean(Constants.STATUS));
                projectSteps.add(ps);
            }
            logger.log(Level.FINE, "The project steps count:" + projectSteps.size());
            return projectSteps;
    }
    catch (Exception e){
        logger.log(Level.SEVERE, "Error while getting project steps:" + e);
        return projectSteps;
    }
    finally {
            cleanUp(stmt,connection);

        }
        }

    /**
     * Insert game data for a game
     * @param gameId
     * @param userName
     * @param timeForEachMove
     * @param stepsForEachPlayer
     * @param isTimeBound
     * @return
     */
    public static boolean insertIntoGame(String gameId, String userName, int timeForEachMove, int stepsForEachPlayer, boolean isTimeBound){
        Connection connection = null;
        PreparedStatement stmt = null;

        try{
            connection = DB.getConnection();
            String query = "INSERT INTO GAME (game_id,status,start_time,end_time,host,time_for_each_move,steps_for_each_player,isTimeBound,company_id,product_id) values (?,?,?,?,?,?,?,?,?,?)";
            stmt = connection.prepareStatement(query);
            stmt.setString(1,gameId);
            stmt.setString(2, Constants.HOSTED_STATUS);
            stmt.setNull(3, Types.DATE);
            stmt.setNull(4, Types.DATE);
            stmt.setString(5,userName);
            stmt.setInt(6,timeForEachMove);
            stmt.setInt(7,stepsForEachPlayer);
            stmt.setBoolean(8,isTimeBound);
            stmt.setInt(9,1);
            stmt.setInt(10,1);

            int res = stmt.executeUpdate();
            return res > 0; // Query success result

        }
        catch(Exception e){
            logger.log(Level.SEVERE, "Error while inseting game:" + e);
            return false;
        }
        finally {
            cleanUp(stmt,connection);
        }
    }

    /**
     * Get phases present in a game
     * @param configId
     * @return
     */
    public static List<Phase> getPhases(String configId){

        String query = "SELECT C.config_phase_mapping_id, P.phase_id,phase_name,description from PHASES P" +
                " JOIN CONFIG_PHASE_MAPPING C where P.phase_id=C.phase_id and C.game_config_id = ?";

        logger.log(Level.FINE, "ConfigId: " + configId);

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<Phase> gamePhases = null;
        try {
            connection = DB.getConnection();
            stmt = connection.prepareStatement(query);
            stmt.setString(1,configId);
            rs = stmt.executeQuery();
            Phase phase;
            gamePhases = new ArrayList<>();
            while(rs.next()){
                phase = new Phase();
                phase.setPhaseId(rs.getString("config_phase_mapping_id"));
                phase.setPhaseName(rs.getString("phase_name"));
                phase.setPhaseDescription(rs.getString("description"));
                gamePhases.add(phase);
            }

            return gamePhases;
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error while getting phases:" + e);
            return gamePhases;
        }
        finally{
            cleanUp(stmt,connection);
        }
    }

    /**
     * Get mititgation cards for a given riskId
     * @param riskId
     * @param gamePlayerId
     * @return
     */
    public static List<ProjectStep> getMitigationCards(String riskId, String gamePlayerId){
        Connection conn = null;
        PreparedStatement stmt = null;
        List<ProjectStep> mititgationSteps = new ArrayList<>();
        try{
            conn = DB.getConnection();
            String query = "SELECT CPM.config_project_step_mapping_id,P.project_step_id,phase_name, project_step_name, `level`, pre_requisite,budget, personnel, capability_points, capability_bonus,`status` FROM CONFIG_RISK_MITIGATION_MAPPING CRMM " +
                    "JOIN CONFIG_PHASE_PROJECTSTEPS_MAPPING CPM ON CRMM.project_step_id = CPM.config_project_step_mapping_id and CRMM.config_risk_mapping_id = ? " +
                    "JOIN GAME_PLAYER_PROJECT_STEP_STATUS GPS on CPM.config_project_step_mapping_id = GPS.config_project_step_mapping_id and GPS.game_player_id = ? " +
                    "JOIN PROJECT_STEPS P on CPM.project_step_id = P.project_step_id " +
                    "JOIN CONFIG_PHASE_MAPPING PM ON CPM.config_phase_mapping_id = PM.config_phase_mapping_id " +
                    "JOIN PHASES PH ON PH.phase_id = PM.phase_id";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,riskId);
            stmt.setString(2,gamePlayerId);
            ResultSet rs = stmt.executeQuery();
            ProjectStep ps = null;

            while(rs.next()){
                ps = new ProjectStep();
                ps.setPhaseName(rs.getString("phase_name"));
                ps.setProjectStepId(rs.getString(Constants.CONFIG_PROJECT_STEP_MAPPING_ID));
                ps.setProjectStepName(rs.getString(Constants.PROJECT_STEP_NAME));
                ps.setBudget(rs.getInt(Constants.BUDGET));
                ps.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                ps.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                ps.setLevel(rs.getInt(Constants.LEVEL));
                ps.setPersonnel(rs.getInt(Constants.PERSONNEL));
                ps.setPreRequisite(rs.getString(Constants.PRE_REQUISITE));
                ps.setStatus(rs.getBoolean(Constants.STATUS));
                mititgationSteps.add(ps);
            }
            return mititgationSteps;

        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error while getting mititgation cards:" + e);
            return mititgationSteps;
        }
        finally {
            cleanUp(stmt,conn);
        }

    }

    /**
     * Update the risk status of a player
     * @param gamePlayerId
     * @param riskId
     * @return
     */
    public static boolean updateRiskStatus(String gamePlayerId, String riskId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "UPDATE GAME_PLAYER_RISK_STATUS SET `status` = ? WHERE game_player_id = ? and risk_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1,true);
            stmt.setString(2,gamePlayerId);
            stmt.setString(3,riskId);
            int result = stmt.executeUpdate();
            return result > 0;

        }
        catch(Exception e){
            logger.log(Level.SEVERE, "Error while updating risk status" + e);
            return false;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Get tisk card details
     * @param riskId
     * @return
     */
    public static RiskCard getRiskDetails(String riskId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT budget_to_mitigate,personnel_to_mitigate FROM RISKS R JOIN CONFIG_RISK_MAPPING CRM ON CRM.risk_id = R.risk_id and CRM.config_risk_mapping_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,riskId);
            ResultSet rs  = stmt.executeQuery();
            RiskCard rc = null;
            while(rs.next()){
                rc = new RiskCard();
                rc.setBudget(rs.getInt("budget_to_mitigate"));
                rc.setPersonnel(rs.getInt("personnel_to_mitigate"));
            }
            return rc;


        }
        catch(Exception e){
            logger.log(Level.SEVERE, "Error while retrieving risk status" + e);
            return null;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Check if risk can be mitigated
     * @param currentStep
     * @param rc
     * @return
     */
    public static boolean mitigateRisk(Snapshot currentStep, RiskCard rc) {
        try {
            if (currentStep.getBudget() < rc.getBudget())
                return false;
            if (currentStep.getPersonnel() < rc.getPersonnel())
                return false;

            //perform risk mitigation
            currentStep.setBudget(currentStep.getBudget() - rc.getBudget());
            currentStep.setPersonnel(currentStep.getPersonnel() - rc.getPersonnel());
            return true;
        }
        catch(Exception e){
            logger.log(Level.SEVERE, "Error while checking mititgation status" + e);
            return false;
        }
    }

    /**
     * Closes the connection
     * @param stmt
     * @param conn
     */
    public static void cleanUp(PreparedStatement stmt, Connection conn){
        try {
            if(stmt!=null)
                stmt.close();
            if(conn!=null)
                conn.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"Exception while closing connection:" +  e);
        }
    }
}
