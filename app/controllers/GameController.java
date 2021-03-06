package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Phase;
import models.ProjectStep;
import models.SessionManager;
import play.Play;
import play.db.DB;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utility.Constants;
import utility.GameUtility;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public class GameController extends Controller {

    public static final Logger logger = Logger.getLogger(GameController.class.getName());


    @BodyParser.Of(BodyParser.Json.class)
    public static Result hostGame(){
        //Check if session is set for the user.
        //for every game thats created generate a gameId and insert into the db
        //Number of players to be invited, number of turns, etc
        // Insert the details into the db


        if(!validateSession()) return badRequest("Login Required for this action");
        String userName = session().get("username");

        //Get all the necessary request parameters
        JsonNode json = request().body().asJson();
        //String name = json.get("name").asText();
        boolean isTimeBound = json.get("istimebound").asBoolean();
        int timeForEachMove = json.get("timeforeachmove").asInt();
        int stepsForEachPlayer = json.get("stepsforeachplayer").asInt();




            String seed = getSeed();
            System.out.println("Seed:" + seed);
            String gameId = userName.split("@")[0] +  seed;
        //Insert gameId into table
        System.out.println("Inserting into game table");
        if(!insertIntoGame(gameId,userName,timeForEachMove,stepsForEachPlayer,isTimeBound))return badRequest("Error while starting a GAME");
        System.out.println("Inserted into game table");
        System.out.println("Inserting into game player table");
        String gamePlayerId = insertIntoGamePlayer(gameId,userName,false);//Host cannot be observer
        if( gamePlayerId == null  || gamePlayerId.isEmpty()) return badRequest("Error while hosting a game");
        System.out.println("Inserted into game player table");

        session().put("gameplayerid",gamePlayerId);
        ObjectNode result = play.libs.Json.newObject();
        //result.put("gamePlayerId",gamePlayerId);
        result.put("gameid",gameId);
        result.put("message","success");

        /**
         * ADD WEB SOCKETS CODE
         *
         */
        SessionManager.addUser(gameId,gamePlayerId);
        //return ok(views.html.HostGame.render());
        //session().put("gameid",gameId);
        return ok(result);
    }


  //  @BodyParser.Of(BodyParser.Json.class)
    public static Result joinGame(){
        //Check if user is logged in
        if(!validateSession()){
            return badRequest("Login Required");
        }
        String userName = session().get("username");
       // String userName = "srijith";
        //Check if gameId is sent through the request
        JsonNode json = request().body().asJson();
        String gameId =json.get("gameid").asText();
        System.out.println("GameId:" + gameId);
        boolean isObserver = json.get("isobserver").asBoolean();
        if(gameId == null || gameId.isEmpty())return badRequest("Error with the request, gameId not found");

        //CHECK IF THE GAME EXISTS
        System.out.println("Checking of game Id :" + gameId + " exists");
        /*
        TO DO -- CHECK IF THE GAME IS IN HOSTED STATUS
        IF GAME IS NOT TIME BOUND, ALLOW PLAYERS TO JOIN EVEN IF GAME IS RUNNING
        ELSE DONT ALLOW
         */

        if(!GameUtility.gameExists(gameId))return badRequest("Game Id doesnt exist");
        System.out.println("Game Id exists");
        //If host of the game tries to join the game, REJECT THE request
        System.out.println("Checking if requested person is host again");
        if(GameUtility.isHost(gameId,userName))return badRequest("You cannot do that as host");

        //INSERT ENTRY INTO GAME_PLAYER TABLE
        String gamePlayerid = insertIntoGamePlayer(gameId,userName,isObserver);
        session().put("gameplayerid",gamePlayerid);

        //Check if gameId is present in websocket map
       /* List<String> webSockets = GameUtility.webSocketMapping.get(gameId);
        if(webSockets == null || webSockets.size() == 0){
            return badRequest("Game is not hosted");
        }
        //add user into the existing sockets
        GameUtility.webSocketMapping.get(gameId).add(userName);*/

        /**
         * CREATE SOCKET FOR THE USER AND REDIRECT TO THE HOSTED PAGE
         */
        //String gamePlayerid = request().body().asFormUrlEncoded().get("username")[0];
//        String gamePlayerid = "srijith",
          //String   gameid = "1";

        if(!SessionManager.hasUser(gameId, gamePlayerid)){
            SessionManager.addUser(gameId, gamePlayerid);
        }
        ObjectNode result = play.libs.Json.newObject();
        //result.put("gamePlayerId",gamePlayerId);
        result.put("gameplayerid",gamePlayerid);
        result.put("message","success");
        //return ok(views.html.join.render(gamePlayerid));
        return ok(result);
    }







    public static Result startGame(){
        try {
            //Based on the configuration - > load the phases, projectSteps, Risks, MitigationSteps
            String configId = Play.application().configuration().getString("config_id");
            String username = session().get("username");
            List<Phase> gamePhases = getPhases(configId);
            List<String> allProjectStepIds = GameUtility.getAllProjectSteps(gamePhases);
            if (gamePhases == null || gamePhases.size() == 0) return badRequest("Error while retrieving phases");

            System.out.println("USer:" + username);

            String gameId = request().body().asFormUrlEncoded().get("gameid")[0];
            System.out.println("GAME ID FOUND:" + gameId);
            //JsonNode playersInTheGame = json.withArray("players");
            List<String> playersInTheGame = SessionManager.getAllUsers(gameId);
            System.out.println("Players in the game:" + playersInTheGame.toString());

            if (gameId == null || playersInTheGame == null || gameId.isEmpty() || playersInTheGame.size() == 0)
                return badRequest("Illegal start of the game");


            //If not host, just redirect to the game page.

            if(!GameUtility.isHost(gameId,username)){
                return ok(views.html.ProjectStep.render(gamePhases));
            }
            System.out.println("IM HOST ONLY:" + username);
            //update startTime in GAME TABLE
            if(!GameUtility.updateStartTimeInGameTable(gameId))return badRequest("Error while updating start time");


            //Get List of Observers

            //Insert Player Project Steps Status into table
            if (!GameUtility.insertIntoPlayerProjectStepStatus(playersInTheGame, allProjectStepIds))
                return badRequest("Error while entering project step status");

            //Enter players with their specific order for taking turns during the game
            if (!GameUtility.insertIntoOrdering(gameId, playersInTheGame))
                return badRequest("Error while inserting order");


            //Generate Random Risk cards for players
            //if(!generateRandomRiskCardsForPlayer(playersInTheGame))return badRequest("Error while mapping risks to players");


            //Send Initial game resources -- USE GET FROM HTML

            //Identify whose turn is first

            //Make him play the game

            //Map<Phase,List<ProjectStep>> phaseProjectStepMapping = getProjectSteps(configId,phases);

            //Update the game table with start time and list of players
            return ok(views.html.ProjectStep.render(gamePhases));
        }catch(Exception e){
            System.out.println("Error while starting the game");
            return badRequest("Error while starting the game");
        }
    }

    public static Result getResources(){
        String configId = Play.application().configuration().getString("config_id");
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            String query = "SELECT initial_budget,initial_resources FROM GAME_CONFIGURATIONS WHERE game_config_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1,configId);
            ResultSet rs = stmt.executeQuery();
            int resources = 0, budget = 0;
            while(rs.next()){
                resources = rs.getInt("initial_resources");
                budget = rs.getInt("initial_budget");
            }
            ObjectNode result = Json.newObject();
            result.put("resources", resources);
            result.put("budget",budget);
            return ok(result);

        }catch (Exception e){
            System.out.println(e.getMessage());
            return badRequest("Error while retrieving resources");
        }

    }


    public static Result performStep(){


        return ok();
    }



    public static List<Phase> getPhases(String configId){
//        if(!validateSession())return badRequest("Login Required");
        //String configId = Play.application().configuration().getString("config_id");
        String query = "SELECT C.config_phase_mapping_id, P.phase_id,phase_name,description from PHASES P JOIN CONFIG_PHASE_MAPPING C where P.phase_id=C.phase_id and C.game_config_id = ?";
        //String query = "SELECT C.id,P.phaseId,phaseName,description from PHASES P,CONFIG_PHASE_MAPPING C where game_config_id = ? and P.phaseId = C.phaseId)";
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
            logger.log(Level.FINE,"Phases retrieved and populated");
           // return ok(views.html.ProjectStep.render(gamePhases));
            return gamePhases;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            logger.log(Level.SEVERE,"Error while retrieving phases");
            //return badRequest();
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

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getProjectSteps(){
        //String configId = Play.application().configuration().getString("config_id");
        String phaseId = request().body().asJson().get("phaseId").asText();
        String gamePlayerId = session().get("gameplayerid");

        //Make sure to add player status table and return the status along with the project Steps


        //String query =  "SELECT P.project_step_id, project_step_name, `level`, pre_requisite,budget, personnel, capability_points, capability_bonus  from PROJECT_STEPS P " +
          //      "JOIN CONFIG_PHASE_PROJECTSTEPS_MAPPING CPS on P.project_step_id = CPS.project_step_id " +
           //     "JOIN CONFIG_PHASE_MAPPING CPM on CPS.config_phase_mapping_id = CPM.config_phase_mapping_id and game_config_id = ? and phase_id=?";

        String query = "SELECT CPM.config_project_step_mapping_id,P.project_step_id, project_step_name, `level`, pre_requisite,budget, personnel, capability_points, capability_bonus,`status` FROM CONFIG_PHASE_PROJECTSTEPS_MAPPING CPM" +
                " JOIN GAME_PLAYER_PROJECT_STEP_STATUS GPS on CPM.config_project_step_mapping_id = GPS.config_project_step_mapping_id" +
                " JOIN PROJECT_STEPS P where CPM.project_step_id = P.project_step_id and CPM.config_phase_mapping_id = ? and GPS.game_player_id = ?";

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
            return ok(play.libs.Json.toJson(projectSteps));
        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while retrieving projectSteps");
            System.out.println(e.getMessage());
            return badRequest();
        }
        finally{
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    public static boolean validateSession(){
        System.out.println("In validate session");
        System.out.println(session().get("username"));
        if(session().isEmpty() || session().get("username") == null || session().get("username").isEmpty() ){
            System.out.println("Login to host a game");
            return false;
        }

        return true;
    }

    public static String getSeed(){
       return String.format("%d%d", (int) System.currentTimeMillis() % 1000, (int) (Math.random() * 1000));
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
            logger.log(Level.SEVERE,e.getMessage());
            return false;
        }
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }


    private static String insertIntoGamePlayer(String gameId, String userName, boolean isObserver) {

        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = DB.getConnection();
            String gamePlayerId = userName + getSeed();
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
}