package utility;

import com.fasterxml.jackson.databind.JsonNode;

import models.InitialGameStat;
import models.Phase;

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
        Date date = new Date(new java.util.Date().getTime());
        try{
            conn = DB.getConnection();
            String query = "UPDATE GAME SET start_time=? WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setDate(1,date);
            stmt.setString(2,gameId);
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
        }
        finally{
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String generateGameId() {
        return getSeed();
    }

    public static String getSeed(){
        return String.format("%d%d", Math.abs((int) System.currentTimeMillis() % 1000), (int) (Math.random() * 1000));
    }
}
