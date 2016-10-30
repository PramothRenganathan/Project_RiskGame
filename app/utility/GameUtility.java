package utility;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import play.Play;
import play.db.DB;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by srijithkarippure on 9/5/16.
 */
public class GameUtility {

    public static Map<String,List<String>> webSocketMapping = new HashMap<>();

    public static List<String> getAllProjectSteps(List<Phase> phases) {

        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        List<String> allProjectStepIds = new ArrayList<>();
        try {

//            String query = "SELECT PH.phase_name,P.project_step_id, project_step_name, description, `level`, pre_requisite,budget, personnel, capability_points, capability_bonus  from PROJECT_STEPS P " +
//                    "JOIN CONFIG_PHASE_PROJECTSTEPS_MAPPING CPS on P.project_step_id = CPS.project_step_id " +
//                    "JOIN CONFIG_PHASE_MAPPING CPM on CPS.config_phase_mapping_id = CPM.config_phase_mapping_id and game_config_id = ? " +
//                    "JOIN PHASES PH on PH.phase_id = CPM.phase_id;" ;
            StringBuilder sb = new StringBuilder();
            for(Phase p : phases){
                sb.append("'");
                sb.append(p.getPhaseId());
                sb.append("'");
                sb.append(",");
            }
            String phaseIds = sb.substring(0,sb.length()-1);
            System.out.println("PhaseIds:" + phaseIds);
            String query = "SELECT config_project_step_mapping_id FROM CONFIG_PHASE_PROJECTSTEPS_MAPPING WHERE config_phase_mapping_id IN (" + phaseIds + ");";

            stmt = connection.prepareStatement(query);
            //stmt.setString(1, configId);
            System.out.println("Im here");

            rs = stmt.executeQuery();
            System.out.print("DONE QUERY");

            //List<ProjectStep> projectSteps = new ArrayList<>();
            //System.out.println(rs.getRow());
            while (rs.next()) {
//                String phaseName = rs.getString("phase_name");
//                ProjectStep ps = new ProjectStep();
//                ps.setProjectStepId(rs.getString("project_step_id"));
//                ps.setProjectStepName(rs.getString("project_step_name"));
//                ps.setProjectStepDescription(rs.getString("description"));
//                ps.setBudget(rs.getInt("budget"));
//                ps.setCapabilityBonus(rs.getInt("capability_bonus"));
//                ps.setCapabilityPoints(rs.getInt("capability_points"));
//                ps.setLevel(rs.getInt("level"));
//                ps.setPersonnel(rs.getInt("personnel"));
//                ps.setPreRequisite(rs.getString("pre_requisite"));
//                projectSteps.add(ps);
                //String phaseId = rs.getString("config_phase_mapping_id");
                allProjectStepIds.add(rs.getString("config_project_step_mapping_id"));


//                        if(phaseProjectStepMapping.containsKey(phaseId))
//                    phaseProjectStepMapping.get(phaseId).add(projectStepId);
//
//                        else{
//                    ArrayList<String> projectSteps = new ArrayList<>();
//                    projectSteps.add(projectStepId);
//                    phaseProjectStepMapping.put(phaseId, projectSteps);
//                }





            }
            return allProjectStepIds;
        }catch(Exception e){
            //logger.log(Level.SEVERE,"Error while retrieving projectSteps");
            System.out.println(e.getMessage());
            return allProjectStepIds;
        }
        finally{
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean insertIntoOrdering(String gameId, List<String> playersInTheGame) {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DB.getConnection();
            //stmt = conn.prepareStatement();
            int count = 1;
            String query = "INSERT INTO GAME_ORDERING (game_player_id,game_id,order_number) VALUES (?,?,?)";
            stmt = conn.prepareStatement(query);
            for(String playerId : playersInTheGame){
                System.out.println("Players:" + playerId);

                stmt.setString(1,playerId);
                stmt.setString(2,gameId);
                stmt.setInt(3,count);
                stmt.addBatch();
                count++;
            }
            int[] results = stmt.executeBatch();
            for(int i: results){
                if(i <= 0) return false;
            }
            System.out.println("Orders Inserted");
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean insertIntoPlayerProjectStepStatus(List<String> playersInTheGame,List<String> projectSteps) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            for(String playerId: playersInTheGame){
                String query = "INSERT INTO GAME_PLAYER_PROJECT_STEP_STATUS (game_player_id,config_project_step_mapping_id,status) VALUES (?,?,?)";
                stmt = conn.prepareStatement(query);
                for(String projectStepId : projectSteps) {
                    stmt.setString(1, playerId);
                    stmt.setString(2, projectStepId);
                    stmt.setBoolean(3, false);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            //int[] result = stmt.executeBatch();
            //for(int i = 0; i< result.length; i++){
             //   if(result[i]<=0) return  false;
            //}
            System.out.println("Status entered for all the players");
            return true;

        }catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean gameExists(String gameId) {

        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT COUNT(1) AS `count` FROM GAME_PLAYER WHERE game_id=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gameId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                if(Integer.parseInt(rs.getString("count"))>0)return true;
            }
            return false;

        }catch(Exception e){
            System.out.println("Error while checking gameId existence");
            return false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isHost(String gameId, String userName) {

        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT host from GAME where game_id = ?";
            stmt =conn.prepareStatement(query);
            stmt.setString(1,gameId);

            ResultSet rs =stmt.executeQuery();
            while(rs.next()){
                System.out.println("Host:" + rs.getString("host"));
                System.out.println("UserName:" + userName);
                if(rs.getString("host").equalsIgnoreCase(userName)){
                    System.out.println("Inside");
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            System.out.println("Error while checking of host");
            return true;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean updateStartTimeInGameTable(String gameId) {
        Connection conn = DB.getConnection();
        PreparedStatement stmt = null;
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
        try{
            conn = DB.getConnection();
            String query = "UPDATE GAME SET start_time=?,status=? WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1,date);
            stmt.setString(2,Constants.RUNNING_STATUS);
            stmt.setString(3,gameId);
            int updateStatus = stmt.executeUpdate();
            if(updateStatus > 0 ) return true;
            return false;


        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;

        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }
    public static boolean getResources(InitialGameStat initialGameStat){
        String configId = Play.application().configuration().getString("config_id");
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT initial_budget,initial_resources,capability_bonus,capability_points,loan_amount FROM GAME_CONFIGURATIONS WHERE game_config_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,configId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                initialGameStat.setResources(rs.getInt("initial_resources"));
                initialGameStat.setBudget(rs.getInt("initial_budget"));
                initialGameStat.setCapabilityBonus(rs.getInt("capability_bonus"));
                initialGameStat.setCapabilityPoints(rs.getInt("capability_points"));
                initialGameStat.setLoanAmount(rs.getInt("loan_amount"));
            }
            return true;


        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;

        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    public static boolean insertSnapshots(List<String> playersInTheGame, InitialGameStat initialGameStat) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "INSERT INTO GAME_MOVES_SNAPSHOT (game_player_id,turn_no,budget,personnel,capability_bonus,time_taken,move_type,move_status,skip_turn_status,project_step_id,risk_id,oops_id,surprise_id,oops_impact_id,surprise_impact_id,loan_amount,isProduction,capability_points) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(query);
            for(String playerId : playersInTheGame){
                stmt.setString(1,playerId);
                stmt.setInt(2,0);//Turn number
                stmt.setInt(3,initialGameStat.getBudget());
                stmt.setInt(4,initialGameStat.getResources());
                stmt.setInt(5,initialGameStat.getCapabilityBonus());
                stmt.setInt(6,0);//time taken
                stmt.setNull(7, Types.VARCHAR);//move type
                stmt.setNull(8,Types.TINYINT);//move Status
                stmt.setNull(9,Types.TINYINT);//skip turn status
                stmt.setNull(10,Types.VARCHAR);//project Step Id
                stmt.setNull(11,Types.VARCHAR);//risk id
                stmt.setNull(12,Types.VARCHAR);//oops id
                stmt.setNull(13,Types.VARCHAR);//surprise id
                stmt.setNull(14,Types.VARCHAR);//oops impact id
                stmt.setNull(15,Types.VARCHAR);//surprise impact id
                stmt.setInt(16,initialGameStat.getLoanAmount());
                stmt.setNull(17,Types.TINYINT);//Is Production
                stmt.setInt(18,initialGameStat.getCapabilityPoints());//capability_points
                stmt.addBatch();
            }
            int[] result = stmt.executeBatch();
            for(int i : result){
                if(i<=0)return false;
            }
            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;

        }
        finally{
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static String generateGameId() {
        return getSeed();
    }

    public static String getSeed(){
        return String.format("%d%d", Math.abs((int) System.currentTimeMillis() % 1000), (int) (Math.random() * 1000));
    }

    public static boolean getTimeBound(InitialGameStat initialGameStat, String gameId) {
        String configId = Play.application().configuration().getString("config_id");
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT time_for_each_move,steps_for_each_player FROM GAME WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,configId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                initialGameStat.setTimeForEachMove(rs.getInt("time_for_each_move"));
                initialGameStat.setStepsForEachPlayer(rs.getInt("steps_for_each_player"));
            }
            return true;


        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;

        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isProjectStepPerformed(String id, String gamePlayerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        System.out.println("Checking if the project step is performed");
        try{
            conn = DB.getConnection();
            String query = "SELECT `status` FROM GAME_PLAYER_PROJECT_STEP_STATUS WHERE config_project_step_mapping_id = ? and game_player_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,id);
            stmt.setString(2,gamePlayerId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                if(rs.getBoolean("status"))return true;
            }
            return false;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean updateProjectStepStatus(String id, String gamePlayerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        System.out.println("Updating project step status to true");
        try{
            conn = DB.getConnection();
            String query = "UPDATE GAME_PLAYER_PROJECT_STEP_STATUS SET status = ? WHERE config_project_step_mapping_id = ? and game_player_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1,true);
            stmt.setString(2,id);
            stmt.setString(3,gamePlayerId);
            int result = stmt.executeUpdate();
            if(result > 0) return true;
            return false;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Snapshot getPreviousSnapshot(String gamePlayerId, int turnNo) {
        System.out.println("In Getting previous step snapshot");
        Connection conn = null;
        PreparedStatement stmt = null;
        System.out.println("Updating project step status to true");
        try{
            conn = DB.getConnection();
            String query = "SELECT budget,personnel,capability_bonus,capability_points,skip_turn_status,turn_no,isProduction FROM GAME_MOVES_SNAPSHOT WHERE game_player_id = ? and turn_no = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            stmt.setInt(2,turnNo);
            ResultSet rs = stmt.executeQuery();
            Snapshot step = new Snapshot();
            while(rs.next()){
                step.setBudget(rs.getInt("budget"));
                step.setCapabilityPoints(rs.getInt("capability_points"));
                step.setCapabilityBonus(rs.getInt("capability_bonus"));
                step.setPersonnel(rs.getInt("personnel"));
                step.setSkipTurnStatus(rs.getBoolean("skip_turn_status"));
                step.setProduction(rs.getBoolean("isProduction"));
                step.setTurnNo(turnNo);
            }
            System.out.println("Done getting prevous snapshot");
            System.out.println(step.getBudget() + " "+ step.getPersonnel());
            return step;

        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Snapshot getCurrentDetailsFromTheUser(String gamePlayerId, JsonNode body) {

        String type = body.get("type").asText();
        String id = body.get("id").asText();
        int turnNo = body.get("turnno").asInt();
        int budget = body.get("budget").asInt();
        int capabilityBonus = body.get("capabilitybonus").asInt();
        int capabilityPoints = body.get("capabilitypoints").asInt();
        int personnel = body.get("personnel").asInt();
        int timeTaken = body.get("timetaken").asInt();
        boolean skipTurn = body.get("skipturn").asBoolean();
        int oneTurn = body.get("oneturn").asInt();
        int twoTurn = body.get("twoturn").asInt();

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

        return receivedSnapShot;
    }

   // public static int

    public static boolean validateStep(Snapshot previousStep, Snapshot currentStep) {
        if(previousStep.getBudget() != currentStep.getBudget())return false;
//        if(previousStep.getPersonnel() != currentStep.getPersonnel()){
//            System.out.println("Previous Resource:" + previousStep.getPersonnel());
//            System.out.println("Previous Resource:" + currentStep.getPersonnel());
//            return false;
//        }
        if(previousStep.getCapabilityBonus() != currentStep.getCapabilityBonus()) return false;
        if(previousStep.getCapabilityPoints() != currentStep.getCapabilityPoints()) return false;
        if(previousStep.getTurnNo() != currentStep.getTurnNo()-1) return false;


        return true;
    }

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
            stmt.setBoolean(8,true);//move Status
            stmt.setBoolean(9,currentStep.isSkipTurnStatus());//skip turn status
            if(moveType == Constants.PerformStep.PROJECTSTEP){
                stmt.setString(10,currentStep.getProjectStepId());//project Step Id
            }
            else
            {
                stmt.setNull(10,Types.VARCHAR);//risk id
            }

            if(moveType == Constants.PerformStep.OOPS){
                stmt.setString(12,currentStep.getOopsId());//project Step Id
            }
            else
            {
                stmt.setNull(12,Types.VARCHAR);//oops id
            }

            if(moveType == Constants.PerformStep.SURPRISE){
                stmt.setString(13,currentStep.getSurpriseId());//surprise id
            }
            else
            {
                stmt.setNull(13,Types.VARCHAR);//surprise id
            }

            stmt.setNull(11,Types.VARCHAR);//risk id


            stmt.setNull(14,Types.VARCHAR);//oops impact id
            stmt.setNull(15,Types.VARCHAR);//surprise impact id
            stmt.setInt(16,currentStep.getLoanAmount());
            stmt.setNull(17,Types.TINYINT);//Production status
            stmt.setInt(18,currentStep.getCapabilityPoints());

            int result = stmt.executeUpdate();
            return result > 0 ? true: false;

        }
        catch(Exception e){
            System.out.println("Exception while retrieving project step");
            System.out.println(e.getMessage());
            return false;
        }finally {
            try {
                conn.close();
            } catch (SQLException e) {

            }
        }

    }



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

            Calendar cal = Calendar.getInstance();
            stmt.setNull(5,Types.DATE);
            stmt.setNull(6,Types.DATE);
            int rs = stmt.executeUpdate();
            if(rs > 0)return gamePlayerId;
            else return null;


        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean getGameRules(InitialGameStat gameStat)
    {

        String configId = Play.application().configuration().getString("config_id");
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
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
            System.out.println(e.getMessage());
            return false;

        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Constants.PerformStep getActiontobeTaken(int level,int capabilityBonus)
    {

        InitialGameStat gamerules = new InitialGameStat();
        Random rand = new Random();
        int randomnumber = rand.nextInt(100) + 1;
        //randomnumber = 21;
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
                ps.setBudget(rs.getInt("budget"));
                ps.setCapabilityPoints(rs.getInt("capability_points"));
                ps.setCapabilityBonus(rs.getInt("capability_bonus"));
                ps.setPersonnel(rs.getInt("personnel"));
                ps.setProjectStepName(rs.getString("project_step_name"));
                ps.setLevel(rs.getInt("level"));
                ps.setPreRequisite(rs.getString("pre_requisite"));
            }
            return ps;

        }
        catch(Exception e){
            System.out.println("Exception while retrieving project step");
            System.out.println(e.getMessage());
            return null;
        }finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean canProjectStepBePerformed(Snapshot currentStep, ProjectStep projectStep) {
        if(currentStep.getBudget() - projectStep.getBudget() < 0) return false;
        //if(currentStep.getCapabilityPoints() - projectStep.getCapabilityPoints() < 0 ) return false;
        //if(currentStep.getCapabilityBonus() - projectStep.getCapabilityBonus() < 0) return false;
        if(currentStep.getPersonnel() - projectStep.getPersonnel() < 0) return false;

        currentStep.setCapabilityBonus(currentStep.getCapabilityBonus() + projectStep.getCapabilityBonus());
        currentStep.setBudget(currentStep.getBudget() - projectStep.getBudget());
        currentStep.setCapabilityPoints(currentStep.getCapabilityPoints() + projectStep.getCapabilityPoints());
        currentStep.setPersonnel(currentStep.getPersonnel() - projectStep.getPersonnel());
//        currentStep.setTwoTurn(projectStep.getPersonnel());//Resources will be back in two turns

        //System.out.println("Im here");
        //Add pre-requisite step here
        return true;
    }



    public static boolean performOOPS(Snapshot currentStep,OOPS currentOOPS) {

        OOPS oops = new OOPS();
        List<OOPS> oopsList = new ArrayList<>();
        oopsList = generateOOPSCard();

        for(int i=0;i<oopsList.size();i++){

            if(currentStep.getBudget() - oopsList.get(i).getBudget() < 0) continue;
            if(currentStep.getCapabilityPoints() - oopsList.get(i).getCapabilityPoints() < 0 ) continue;
            if(currentStep.getCapabilityBonus() - oopsList.get(i).getCapabilityBonus() < 0) continue;
            if(currentStep.getPersonnel() - oopsList.get(i).getResources() < 0) continue;
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
        currentOOPS = oops;
//        currentStep.setTwoTurn(projectStep.getPersonnel());//Resources will be back in two turns

        //System.out.println("Im here");
        //Add pre-requisite step here
        return true;
    }

    public static boolean performSurprise(Snapshot currentStep,SURPRISE currentSurprise) {

        SURPRISE surprise = new SURPRISE();
        surprise = generateSurpriseCard();

        currentStep.setCapabilityBonus(currentStep.getCapabilityBonus() + surprise.getCapabilityBonus());
        currentStep.setBudget(currentStep.getBudget() + surprise.getBudget());
        currentStep.setCapabilityPoints(currentStep.getCapabilityPoints() + surprise.getCapabilityPoints());
        currentStep.setPersonnel(currentStep.getPersonnel() + surprise.getResources());
        currentStep.setMoveType("SURPRISE");
        currentStep.setSurpriseId(surprise.getId());
        currentStep.setCurrentStepResource(surprise.getResources());
        currentSurprise = surprise;
//        currentStep.setTwoTurn(projectStep.getPersonnel());//Resources will be back in two turns

        //System.out.println("Im here");
        //Add pre-requisite step here
        return true;
    }


    public static boolean isGameComplete(int turnNo, String gameId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT steps_for_each_player FROM GAME WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next() && rs.getInt("steps_for_each_player") == turnNo)return true;
            return false;
        }catch (Exception e){
            System.out.println("Error while checking game complete");
            System.out.println(e.getMessage());
            return false;
        }finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static void addReturningResources(Snapshot currentStep) {
        currentStep.setPersonnel(currentStep.getPersonnel() + currentStep.getOneTurn());//Resource already back
        currentStep.setOneTurn(currentStep.getTwoTurn());//Resources to be back in one turn
    }

    public static List<OOPS> generateOOPSCard()
    {
        List<OOPS> oopslist = new ArrayList<>();
        OOPS oopsobj = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement insertStmt = null;
        try {
            conn = DB.getConnection();


                String query = "SELECT * FROM OOPS ORDER BY RAND()";
                stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    oopsobj = new OOPS();
                    oopsobj.setId(rs.getString("oops_id"));
                    oopsobj.setBudget(rs.getInt("budget"));
                    oopsobj.setCapabilityBonus(rs.getInt("capability_bonus"));
                    oopsobj.setCapabilityPoints(rs.getInt("capability_points"));
                    oopsobj.setResources(rs.getInt("personnel"));
                    oopslist.add(oopsobj);
                }

                return oopslist;

        } catch (Exception e) {
            return oopslist;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static SURPRISE generateSurpriseCard()
    {
        SURPRISE surpriseobj = new SURPRISE();
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement insertStmt = null;
        try {
            conn = DB.getConnection();


            String query = "SELECT * FROM SURPRISE  ORDER BY RAND() LIMIT 1";
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                surpriseobj.setId(rs.getString("surprise_id"));
                surpriseobj.setBudget(rs.getInt("budget"));
                surpriseobj.setCapabilityBonus(rs.getInt("capability_bonus"));
                surpriseobj.setCapabilityPoints(rs.getInt("capability_points"));
                surpriseobj.setResources(rs.getInt("personnel"));
            }

            return surpriseobj;

        } catch (Exception e) {
            return surpriseobj;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

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
                    if (i <= 0) return false;//Error while inserting
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static List<RiskCard> getRisks(String gamePlayerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<RiskCard> risks = new ArrayList<>();
        try{
            conn = DB.getConnection();
            String query = "SELECT gprs.risk_id,description,budget_to_mitigate,personnel_to_mitigate,gprs.status from RISKS r JOIN CONFIG_RISK_MAPPING crm on r.risk_id = crm.risk_id" +
                    "JOIN GAME_PLAYER_RISK_STATUS gprs ON gprs.risk_id = crm.config_risk_mapping_id and gprs.game_player_id = ?";
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            RiskCard rc = null;
            while(rs.next()){
                rc = new RiskCard();
                rc.setBudget(rs.getInt("budget_to_mitigate"));
                rc.setPersonnel(rs.getInt("personnel_to_mitigate"));
                rc.setComplete(rs.getBoolean("status"));
                rc.setRiskId(rs.getString("risk_id"));
                rc.setRiskDescription(rs.getString("description"));
                risks.add(rc);
            }
            return risks;

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ProjectStep> getProjectSteps(String phaseId, String gamePlayerId  ){



        String query = "SELECT CPM.config_project_step_mapping_id,P.project_step_id, project_step_name, `level`, pre_requisite,budget, personnel, capability_points, capability_bonus,`status` FROM CONFIG_PHASE_PROJECTSTEPS_MAPPING CPM" +
                " JOIN GAME_PLAYER_PROJECT_STEP_STATUS GPS on CPM.config_project_step_mapping_id = GPS.config_project_step_mapping_id" +
                " JOIN PROJECT_STEPS P on CPM.project_step_id = P.project_step_id and CPM.config_phase_mapping_id = ? and GPS.game_player_id = ?";

        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;

        try {
            stmt = connection.prepareStatement(query);
            //stmt.setString(1, configId);
            stmt.setString(1,phaseId);
            stmt.setString(2,gamePlayerId);
            System.out.println("In Project Steps");

            rs = stmt.executeQuery();
            System.out.print("DONE QUERY");

            List<ProjectStep> projectSteps = new ArrayList<>();
            //System.out.println(rs.getRow());
            while (rs.next()) {
                //String phaseName = rs.getString("phase_name");
                ProjectStep ps = new ProjectStep();
                ps.setProjectStepId(rs.getString("config_project_step_mapping_id"));
                ps.setProjectStepName(rs.getString("project_step_name"));
                //ps.setProjectStepDescription(rs.getString("description"));
                ps.setBudget(rs.getInt("budget"));
                ps.setCapabilityBonus(rs.getInt("capability_bonus"));
                ps.setCapabilityPoints(rs.getInt("capability_points"));
                ps.setLevel(rs.getInt("level"));
                ps.setPersonnel(rs.getInt("personnel"));
                ps.setPreRequisite(rs.getString("pre_requisite"));
                ps.setStatus(rs.getBoolean("status"));
                projectSteps.add(ps);


            }
            System.out.println(projectSteps.toString());
            return projectSteps;
    }
    catch (Exception e){
        System.out.println(e.getMessage());
        return null;
    }
    finally {
            try {
                connection.close();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }
        }

    public static boolean insertIntoGame(String gameId, String userName, int timeForEachMove, int stepsForEachPlayer, boolean isTimeBound){
        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;

        try{
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
            System.out.println(e.getMessage());
            return false;
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public static List<Phase> getPhases(String configId){

        String query = "SELECT C.config_phase_mapping_id, P.phase_id,phase_name,description from PHASES P JOIN CONFIG_PHASE_MAPPING C where P.phase_id=C.phase_id and C.game_config_id = ?";

        System.out.println(configId);

        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        List<Phase> gamePhases = null;
        try {
            stmt = connection.prepareStatement(query);
            stmt.setString(1,configId);
            rs = stmt.executeQuery();
            Phase phase;
            gamePhases = new ArrayList<>();
            while(rs.next()){
                phase = new Phase();
                //phase.setId(rs.getInt("id"));
                phase.setPhaseId(rs.getString("config_phase_mapping_id"));
                phase.setPhaseName(rs.getString("phase_name"));
                phase.setPhaseDescription(rs.getString("description"));
                gamePhases.add(phase);
            }

            return gamePhases;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        finally{
            try {
                connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ProjectStep> getMitigationCards(String riskId, String gamePlayerId){
        Connection conn = null;
        PreparedStatement stmt = null;
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
            List<ProjectStep> mititgationSteps = new ArrayList<>();
            while(rs.next()){
                ps = new ProjectStep();
                ps.setPhaseName(rs.getString("phase_name"));
                ps.setProjectStepId(rs.getString("config_project_step_mapping_id"));
                ps.setProjectStepName(rs.getString("project_step_name"));
                ps.setBudget(rs.getInt("budget"));
                ps.setCapabilityBonus(rs.getInt("capability_bonus"));
                ps.setCapabilityPoints(rs.getInt("capability_points"));
                ps.setLevel(rs.getInt("level"));
                ps.setPersonnel(rs.getInt("personnel"));
                ps.setPreRequisite(rs.getString("pre_requisite"));
                ps.setStatus(rs.getBoolean("status"));
                mititgationSteps.add(ps);
            }
            return mititgationSteps;

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    return null;
    }
}
