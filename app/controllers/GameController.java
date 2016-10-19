package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.Play;
import play.db.DB;
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





        String gameId = GameUtility.generateGameId();
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
        result.put("gameid",gameId);
        result.put("message","success");

        /**
         *  WEB SOCKETS CODE
         *
         */
        SessionManager.addUser(gameId,gamePlayerId);
        return ok(result);
    }


  //  @BodyParser.Of(BodyParser.Json.class)
    public static Result joinGame(){
        //Check if user is logged in
        if(!validateSession()){
            return badRequest("Login Required");
        }
        String userName = session().get("username");

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

        /**
         * CREATE SOCKET FOR THE USER AND REDIRECT TO THE HOSTED PAGE
         */
        if(SessionManager.getAllUsers(gameId).size()>=5) return badRequest("Already 5 players in the game");
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
            if(!validateSession())return badRequest("Not logged in");

            String configId = Play.application().configuration().getString("config_id");
            InitialGameStat initialGameStat = new InitialGameStat();
            String userName = session().get("username");
            initialGameStat.setUserName( userName );
            List<Phase> gamePhases = getPhases(configId);
            initialGameStat.setPhases(gamePhases);
            List<String> allProjectStepIds = GameUtility.getAllProjectSteps(gamePhases);
            if (gamePhases == null || gamePhases.size() == 0) return badRequest("Error while retrieving phases");

            System.out.println("User:" + userName);

            String gameId = request().body().asFormUrlEncoded().get("gameid")[0];
            initialGameStat.setGameId(gameId);
            if(!GameUtility.getResources(initialGameStat)) return badRequest("Error while retrieving resources");
            System.out.println("GAME ID FOUND:" + gameId);

            List<String> playersInTheGame = SessionManager.getAllUsers(gameId);
            System.out.println("Players in the game:" + playersInTheGame.toString());

            if (gameId == null || playersInTheGame == null || gameId.isEmpty() || playersInTheGame.size() == 0)
                return badRequest("Illegal start of the game");
            //Get TimeforEachMove and No of Steps
            if(!GameUtility.getTimeBound(initialGameStat,gameId)) return badRequest("Error while getting time bound");

            //Set turn for each player
            String gamePlayerId = session().get("gameplayerid");
            initialGameStat.setTurnNo(SessionManager.getAllUsers(gameId).indexOf(gamePlayerId) + 1);
            initialGameStat.setSkipTurn(false);
            initialGameStat.setOneTurn(0);//Resources to be back after one turn
            initialGameStat.setTwoTurn(0);//Resources to be back after one turn
            //If not host, just redirect to the game page.
            if(!GameUtility.isHost(gameId,userName)){
                return ok(views.html.ProjectStep.render(initialGameStat));
            }
            System.out.println("IM HOST ONLY:" + userName);
            //update startTime in GAME TABLE
            if(!GameUtility.updateStartTimeInGameTable(gameId))return badRequest("Error while updating start time");


            //Get List of Observers

            //Insert Player Project Steps Status into table
            if (!GameUtility.insertIntoPlayerProjectStepStatus(playersInTheGame, allProjectStepIds))
                return badRequest("Error while entering project step status");

            //Enter players with their specific order for taking turns during the game
            if (!GameUtility.insertIntoOrdering(gameId, playersInTheGame))
                return badRequest("Error while inserting order");

            if(!GameUtility.insertSnapshots(playersInTheGame,initialGameStat))
                return badRequest("Error while inserting into Snapshots");
            //Generate Random Risk cards for players
            //if(!generateRandomRiskCardsForPlayer(playersInTheGame))return badRequest("Error while mapping risks to players");
            //Enter risk for individual players with status in some table

            //Identify whose turn is first

            //Make him play the game

            //Map<Phase,List<ProjectStep>> phaseProjectStepMapping = getProjectSteps(configId,phases);

            //Update the game table with start time and list of players
            return ok(views.html.ProjectStep.render(initialGameStat));
        }catch(Exception e){
            System.out.println("Error while starting the game");
            return badRequest("Error while starting the game");
        }
    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Result performStep(){

        if(!validateSession())return badRequest("Login to perform steps in the game");

        String gamePlayerId = session().get("gameplayerid");

        JsonNode body = request().body().asJson();
        String gameId = body.get("gameid").asText();
        Snapshot currentStep = GameUtility.getCurrentDetailsFromTheUser(gamePlayerId,body);

        String type = currentStep.getMoveType();
        String projectStepId = currentStep.getProjectStepId();
        int turnNo = currentStep.getTurnNo();

        Snapshot previousStep = GameUtility.getPreviousSnapshot(gamePlayerId,turnNo - 1);
        if(!GameUtility.validateStep(previousStep,currentStep))return badRequest("User tampered the data on the frontend");

        //In case of skip step, just update the database and return
        if(type.equalsIgnoreCase("skipstep")) {
            if(!GameUtility.performStep(gamePlayerId,currentStep))return badRequest("Error while updating status");
            GameUtility.addReturningResources(currentStep);
        }
        else if(type.equalsIgnoreCase("projectstep")) {
            if (GameUtility.isProjectStepPerformed(projectStepId, gamePlayerId))
                return badRequest("You already performed this step");
            ProjectStep projectStep = GameUtility.getProjectStepDetails(projectStepId);
            if (projectStep == null) return badRequest("Error while retrieving project step detais");
            if (!GameUtility.canProjectStepBePerformed(currentStep, projectStep))
                return badRequest("The project step cannot be performed with current budget,personnel,capabilityPoints, capabilityBonus");
            if (!GameUtility.performStep(gamePlayerId, currentStep)) return badRequest("Error while updating status");
            if (!GameUtility.updateProjectStepStatus(projectStepId, gamePlayerId))
                return badRequest("Error while updating project step status");
            GameUtility.addReturningResources(currentStep);
        }
            ObjectNode result = play.libs.Json.newObject();
            if(GameUtility.isGameComplete(currentStep.getTurnNo(),gameId))result.put("complete","true");
            result.put("message","success");
            result.put("budget",currentStep.getBudget());
            result.put("personnel",currentStep.getPersonnel());
            result.put("capabilitybonus",currentStep.getCapabilityBonus());
            result.put("capabilitypoints",currentStep.getCapabilityPoints());
            result.put("currentturn",currentStep.getTurnNo());
            result.put("skipturn",currentStep.isSkipTurnStatus());
            result.put("oneturn",currentStep.getOneTurn());
            result.put("twoturn",currentStep.getTwoTurn());

            return ok(result);


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
}