package utility;

import com.fasterxml.jackson.databind.JsonNode;
import models.InitialGameStat;
import models.Phase;
import models.ProjectStep;
import models.Snapshot;
import play.Play;
import play.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        java.sql.Date date = new java.sql.Date(new java.util.Date().getTime());
        try{
            conn = DB.getConnection();
            String query = "UPDATE GAME SET start_time=?,status=? WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setDate(1,date);
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
            String query = "INSERT INTO GAME_MOVES_SNAPSHOT (game_player_id,turn_no,budget,personnel,capability_bonus,time_taken,move_type,move_status,skip_turn_status,project_step_id,risk_id,oops_id,surprise_id,oops_impact_id,surprise_impact_id,loan_amount,isProduction) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                stmt.setNull(17,Types.TINYINT);
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
            String query = "SELECT status FROM GAME_PLAYER_PROJECT_STEP_STATUS WHERE config_project_step_mapping_id = ? and game_player_id = ?";
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
            String query = "SELECT budget,personnel,capability_bonus,skip_turn_status,isProduction FROM GAME_MOVES_SNAPSHOT WHERE game_player_id = ? and turn_no = (SELECT max(turn_no) from GAME_MOVES_SNAPSHOT where game_player_id=?) ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            stmt.setString(2,gamePlayerId);
            ResultSet rs = stmt.executeQuery();
            Snapshot step = new Snapshot();
            while(rs.next()){
                step.setBudget(rs.getInt("budget"));
                step.setCapabilityBonus(rs.getInt("capability_bonus"));
                step.setPersonnel(rs.getInt("personnel"));
                step.setSkipTurnStatus(rs.getBoolean("skip_turn_status"));
                step.setProduction(rs.getBoolean("isProduction"));
            }
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

        Snapshot receivedSnapShot = new Snapshot();
        receivedSnapShot.setBudget(budget);
        receivedSnapShot.setPersonnel(personnel);
        receivedSnapShot.setCapabilityBonus(capabilityBonus);
        receivedSnapShot.setCapabilityPoints(capabilityPoints);
        receivedSnapShot.setTurnNo(turnNo);
        receivedSnapShot.setProjectStepId(id);
        receivedSnapShot.setMoveType(type);
        receivedSnapShot.setTimeTaken(timeTaken);

        return receivedSnapShot;
    }

    public static boolean validateStep(Snapshot previousStep, Snapshot currentStep) {
        if(previousStep.getBudget() != currentStep.getBudget())return false;
        if(previousStep.getPersonnel() != currentStep.getPersonnel()) return false;
        if(previousStep.getCapabilityBonus() != currentStep.getCapabilityBonus()) return false;
        if(previousStep.getCapabilityPoints() != currentStep.getCapabilityPoints()) return false;

        //Try to check turn number if needed


        return true;
    }

    public static boolean performStep(String gamePlayerId, Snapshot currentStep, ProjectStep projectStep) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "INSERT INTO GAME_MOVES_SNAPSHOT (game_player_id,turn_no,budget,personnel,capability_bonus,time_taken,move_type,move_status,skip_turn_status,project_step_id,risk_id,oops_id,surprise_id,oops_impact_id,surprise_impact_id,loan_amount,isProduction) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gamePlayerId);
            stmt.setInt(2,0);//Turn number
            stmt.setInt(3,currentStep.getBudget()-projectStep.getBudget());
            stmt.setInt(4,currentStep.getPersonnel() - projectStep.getPersonnel());
            stmt.setInt(5,currentStep.getCapabilityBonus() - projectStep.getCapabilityBonus());
            stmt.setInt(6,currentStep.getTimeTaken());//time taken
            stmt.setString(7, currentStep.getMoveType());//move type
            stmt.setBoolean(8,true);//move Status
            stmt.setBoolean(9,false);//skip turn status
            stmt.setString(10,currentStep.getProjectStepId());//project Step Id
            stmt.setNull(11,Types.VARCHAR);//risk id
            stmt.setNull(12,Types.VARCHAR);//oops id
            stmt.setNull(13,Types.VARCHAR);//surprise id
            stmt.setNull(14,Types.VARCHAR);//oops impact id
            stmt.setNull(15,Types.VARCHAR);//surprise impact id
            stmt.setInt(16,currentStep.getLoanAmount());
            stmt.setNull(17,Types.TINYINT);//Production status

            int result = stmt.executeUpdate();
            return result > 0 ? true: false;

        }
        catch(Exception e){
            System.out.println("Exception while retrieving project step");
            System.out.println(e.getMessage());
            return false;
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
        }
    }

    public static boolean canProjectStepBePerformed(Snapshot currentStep, ProjectStep projectStep) {
        if(currentStep.getBudget() - projectStep.getBudget() < 0) return false;
        if(currentStep.getCapabilityPoints() - projectStep.getCapabilityPoints() < 0 ) return false;
        if(currentStep.getCapabilityBonus() - projectStep.getCapabilityBonus() < 0) return false;
        if(currentStep.getPersonnel() - projectStep.getPersonnel() < 0) return false;
        //Add pre-requisite step here
        return true;
    }
}
