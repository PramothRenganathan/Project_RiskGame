package utility;

import models.InitialGameStat;
import models.Phase;
import play.Play;
import play.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utility.GameUtility.cleanUp;

/**
 * Created by srijithkarippure on 11/26/16.
 */
public class StartGameUtility {
    public static final Logger logger = Logger.getLogger(StartGameUtility.class.getName());

    /**
     * Inserts the initial snapshots for all the players with turn 0
     * @param playersInTheGame
     * @param initialGameStat
     * @return
     */
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
                if(i<=0)
                    return false;
            }
            return true;
        }catch (Exception e){
            logger.log(Level.SEVERE,"Error while adding step" +  e);
            return false;

        }
        finally{
            cleanUp(stmt,conn);
        }

    }


    /**
     * Insert the game ordering for the players
     * @param gameId
     * @param playersInTheGame
     * @return
     */
    public static boolean insertIntoOrdering(String gameId, List<String> playersInTheGame) {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DB.getConnection();
            int count = 1;
            String query = "INSERT INTO GAME_ORDERING (game_player_id,game_id,order_number) VALUES (?,?,?)";
            stmt = conn.prepareStatement(query);
            for(String playerId : playersInTheGame){
                logger.log(Level.FINE, "Players:" + playerId);
                stmt.setString(1,playerId);
                stmt.setString(2,gameId);
                stmt.setInt(3,count);
                stmt.addBatch();
                count++;
            }
            int[] results = stmt.executeBatch();
            for(int i: results){
                if(i <= 0)
                    return false;
            }
            logger.log(Level.FINE, "Orders Inserted");
            return true;
        } catch (SQLException e) {

            logger.log(Level.SEVERE,"Error while inserting game ordering" + e);
            return false;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Returns list of projects steps for each phase provided in the list of phases
     * @param phases
     * @return
     */
    public static List<String> getAllProjectSteps(List<Phase> phases) {

        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        List<String> allProjectStepIds = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder();
            for(Phase p : phases){
                sb.append("'");
                sb.append(p.getPhaseId());
                sb.append("'");
                sb.append(",");
            }
            String phaseIds = sb.substring(0,sb.length()-1);
            logger.log(Level.FINE, "PhaseIds:" + phaseIds);
            String query = "SELECT config_project_step_mapping_id FROM CONFIG_PHASE_PROJECTSTEPS_MAPPING WHERE config_phase_mapping_id IN (" + phaseIds + ");";

            stmt = connection.prepareStatement(query);

            rs = stmt.executeQuery();
            logger.log(Level.FINE, "Retrieved project steps for all the phases");
            while (rs.next()) {
                allProjectStepIds.add(rs.getString(Constants.CONFIG_PROJECT_STEP_MAPPING_ID));
            }
            return allProjectStepIds;
        }catch(Exception e){
            logger.log(Level.SEVERE, "Error while getting project steps:" + e);
            return allProjectStepIds;
        }
        finally{
            cleanUp(stmt,connection);
        }
    }



    /**
     * Inserts new row with player project step status
     * @param playersInTheGame
     * @param projectSteps
     * @return
     */
    public static boolean insertIntoPlayerProjectStepStatus(List<String> playersInTheGame,List<String> projectSteps) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            for(String playerId: playersInTheGame){
                String query = "INSERT INTO GAME_PLAYER_PROJECT_STEP_STATUS (game_player_id,config_project_step_mapping_id,status) VALUES (?,?,?)";
                try {
                    stmt = conn.prepareStatement(query);
                    for (String projectStepId : projectSteps) {
                        stmt.setString(1, playerId);
                        stmt.setString(2, projectStepId);
                        stmt.setBoolean(3, false);
                        stmt.addBatch();
                    }
                    int[] rs = stmt.executeBatch();
                    for (int i : rs) {
                        if (i <= 0)
                            return false;
                    }
                }
                catch(Exception e){
                    logger.log(Level.SEVERE,"Error while inserting proj step status" + e);
                    return false;
                }
                finally {
                    if(stmt != null)
                        stmt.close();
                }

            }
            logger.log(Level.FINE,"Status entered for all the players");
            return true;

        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while inserting proj step status" + e);
            return false;
        }
        finally{
            cleanUp(stmt,conn);
        }

    }

    /**
     * Check if the game exists
     * @param gameId
     * @return
     */
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
                if(Integer.parseInt(rs.getString("count"))>0)
                    return true;
            }
            return false;

        }catch(Exception e){
            logger.log(Level.SEVERE, "Error while checking gameId existence" + e);
            return false;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Check if the user is host or not.
     * @param gameId
     * @param userName
     * @return
     */
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
                logger.log(Level.FINE, "Host:" + rs.getString("host"));
                logger.log(Level.FINE, "UserName:" + userName);
                if(rs.getString("host").equalsIgnoreCase(userName)){
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            logger.log(Level.SEVERE, "Error while checking of host" +e);
            return true;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Updates the start time of the game
     * @param gameId
     * @return
     */
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
            if(updateStatus > 0 )
                return true;
            return false;


        }catch (Exception e){
            logger.log(Level.SEVERE,"error while updating start time" + e);
            return false;

        }
        finally {
            cleanUp(stmt,conn);
        }
    }



    /**
     * Get the initial resources for the game
     * @param initialGameStat
     * @return
     */
    public static boolean getResources(InitialGameStat initialGameStat){
        String configId = Play.application().configuration().getString(Constants.CONFIG_ID);
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
                initialGameStat.setCapabilityBonus(rs.getInt(Constants.CAPABILITY_BONUS));
                initialGameStat.setCapabilityPoints(rs.getInt(Constants.CAPABILITY_POINTS));
                initialGameStat.setLoanAmount(rs.getInt("loan_amount"));
            }
            return true;


        }catch (Exception e){
            logger.log(Level.SEVERE, "Error while getting resources" + e);
            return false;

        }
        finally {
            cleanUp(stmt,conn);
        }

    }



    /**
     * Generates random game Id
     * @return
     */
    public static String generateGameId() {
        return getSeed();
    }

    /**
     * THe random number generator
     * @return
     */
    public static String getSeed(){
        return String.format("%d%d", Math.abs((int) System.currentTimeMillis() % 1000), (int) (Math.random() * 1000));
    }

    /**
     * Get the time bound status and steps for each player
     * @param initialGameStat
     * @param gameId
     * @return
     */
    public static boolean getTimeBound(InitialGameStat initialGameStat, String gameId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String query = "SELECT time_for_each_move,steps_for_each_player FROM GAME WHERE game_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,gameId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                initialGameStat.setTimeForEachMove(rs.getInt("time_for_each_move"));
                initialGameStat.setStepsForEachPlayer(rs.getInt("steps_for_each_player"));
            }
            return true;


        }catch (Exception e){
            logger.log(Level.SEVERE,"Error while getting time details:" +  e);
            return false;

        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Check if the project step is performed
     * @param id
     * @param gamePlayerId
     * @return
     */
    public static boolean isProjectStepPerformed(String id, String gamePlayerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        logger.log(Level.FINE, "Checking if the project step is performed");
        try{
            conn = DB.getConnection();
            String query = "SELECT `status` FROM GAME_PLAYER_PROJECT_STEP_STATUS WHERE config_project_step_mapping_id = ? and game_player_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,id);
            stmt.setString(2,gamePlayerId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next() && rs.getBoolean(Constants.STATUS)){
                return true;
            }
            return false;
        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while getting status:" +  e);
            return false;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }

    /**
     * Update project step status for player
     * @param id
     * @param gamePlayerId
     * @return
     */
    public static boolean updateProjectStepStatus(String id, String gamePlayerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        logger.log(Level.FINE, "Updating project step status to true");
        try{
            conn = DB.getConnection();
            String query = "UPDATE GAME_PLAYER_PROJECT_STEP_STATUS SET status = ? WHERE config_project_step_mapping_id = ? and game_player_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1,true);
            stmt.setString(2,id);
            stmt.setString(3,gamePlayerId);
            int result = stmt.executeUpdate();
            return result > 0 ? true: false;

        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while updating status" + e);
            return false;
        }
        finally {
            cleanUp(stmt,conn);
        }
    }
}
