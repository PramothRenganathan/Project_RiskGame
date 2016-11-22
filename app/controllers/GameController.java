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
import utility.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
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

        ObjectNode result = play.libs.Json.newObject();
        if(!validateSession()) return ok(views.html.index.render());
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

        if(!GameUtility.insertIntoGame(gameId,userName,timeForEachMove,stepsForEachPlayer,isTimeBound)) {
       // if(1==1){
        result.put("errormsg","Error while starting the game. Contact system admin");
            result.put("message","failure");
            return ok(result);

        }
        System.out.println("Inserted into game table");
        System.out.println("Inserting into game player table");
        String gamePlayerId = GameUtility.insertIntoGamePlayer(gameId,userName,false);//Host cannot be observer
        if( gamePlayerId == null  || gamePlayerId.isEmpty()) {
            // if(1==1){
            result.put("errormsg","Error while hosting the game. Contact system admin");
            result.put("message","failure");
            return ok(result);

        }
        System.out.println("Inserted into game player table");

        session().put("gameplayerid",gamePlayerId);



      //  ObjectNode result = play.libs.Json.newObject();
        result.put("gameid",gameId);
        result.put("message","success");

        /**
         *  WEB SOCKETS CODE
         *
         */
        SessionManager.addUser(gameId,gamePlayerId);
        return ok(result);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSnapshotDetails() throws IOException {
        String gameid = request().body().asJson().get("gameid").asText();
        //String playerid = request().body().asJson().get("playerid").asText();

        File file = new File("/public/images/" + gameid);
        String[] playerDirectories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        String[] snapshots = null;
        if(playerDirectories!=null && playerDirectories.length!=0) {
            for (String directory : playerDirectories) {
                file = new File("/public/images/" + gameid + "/" + directory);
                snapshots = file.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isFile();
                    }
                });
            }
        }

        return ok(play.libs.Json.toJson(snapshots));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result saveImageSnapshot() throws IOException {
        String image_data = request().body().asJson().get("image-data").asText();
        String username = request().body().asJson().get("username").asText();
        String gameid = request().body().asJson().get("gameid").asText();
        String turn = request().body().asJson().get("turnNo").asText();

        String base64Image = image_data.split(",")[1];

        // Convert the image code to bytes.
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        StringBuilder sb = new StringBuilder();
        sb.append("public/images/");
        sb.append(gameid + "/");
        sb.append(username.split("@")[0] + "/");
        File imageDirectory = new File(sb.toString());

        if(!imageDirectory.exists()){
            imageDirectory.mkdirs();
        }

        sb = new StringBuilder();
        sb.append("public/images/");
        sb.append(gameid + "/" + username.split("@")[0] + "/" + turn);
        sb.append(".png");

        File imageFile = new File(sb.toString());

        ImageIO.write(bufferedImage, "png", imageFile);

        return ok("hello");
    }


  //  @BodyParser.Of(BodyParser.Json.class)
    public static Result joinGame(){
        //Check if user is logged in
        if(!validateSession()){
            ok(views.html.index.render());
        }
        ObjectNode result = play.libs.Json.newObject();
        String userName = session().get("username");

        //Check if gameId is sent through the request
        JsonNode json = request().body().asJson();
        String gameId =json.get("gameid").asText();
        System.out.println("GameId:" + gameId);
        boolean isObserver = json.get("isobserver").asBoolean();
        if(gameId == null || gameId.isEmpty()){
            // if(1==1){
            result.put("errormsg","Error with the request, gameId not found. Contact system admin");
            result.put("message","failure");
            return ok(result);

        }

        //CHECK IF THE GAME EXISTS
        System.out.println("Checking of game Id :" + gameId + " exists");
        /*
        TO DO -- CHECK IF THE GAME IS IN HOSTED STATUS
        IF GAME IS NOT TIME BOUND, ALLOW PLAYERS TO JOIN EVEN IF GAME IS RUNNING
        ELSE DONT ALLOW
         */

        if(!GameUtility.gameExists(gameId)){
         //    if(1==1){
            result.put("errormsg","Game Id doesnt exist. Contact system admin");
            result.put("message","failure");
            return ok(result);

        }
        System.out.println("Game Id exists");
        //If host of the game tries to join the game, REJECT THE request
        System.out.println("Checking if requested person is host again");
        if(GameUtility.isHost(gameId,userName)){
            // if(1==1){
            result.put("errormsg","You cannot do that as host. Contact system admin");
            result.put("message","failure");
            return ok(result);

        }

        //INSERT ENTRY INTO GAME_PLAYER TABLE
        String gamePlayerid = GameUtility.insertIntoGamePlayer(gameId,userName,isObserver);
        session().put("gameplayerid",gamePlayerid);

        /**
         * CREATE SOCKET FOR THE USER AND REDIRECT TO THE HOSTED PAGE
         */
        if(SessionManager.getAllUsers(gameId).size()>=5){
            // if(1==1){
            result.put("errormsg","Already 5 players in the game.");
            result.put("message","failure");
            return ok(result);

        }
        if(!SessionManager.hasUser(gameId, gamePlayerid)){
            SessionManager.addUser(gameId, gamePlayerid);
        }

        //result.put("gamePlayerId",gamePlayerId);
        result.put("gameplayerid",gamePlayerid);
        result.put("message","success");
        //return ok(views.html.join.render(gamePlayerid));
        return ok(result);
    }

    public static Result observeGame()
    {
        String userName = session().get("username");
        String gameId = request().body().asFormUrlEncoded().get("gameid")[0];

        List<String> parameters = new ArrayList<>();
        parameters.add(userName);
        parameters.add(gameId);

        return ok(views.html.observer.render(parameters));
    }

    public static Result startGame(){
        try {
            //Based on the configuration - > load the phases, projectSteps, Risks, MitigationSteps
            if(!validateSession())return ok(views.html.index.render());

            String configId = Play.application().configuration().getString("config_id");
            InitialGameStat initialGameStat = new InitialGameStat();
            String userName = session().get("username");
            initialGameStat.setUserName( userName );
            List<Phase> gamePhases = GameUtility.getPhases(configId);
            initialGameStat.setPhases(gamePhases);
            List<String> allProjectStepIds = GameUtility.getAllProjectSteps(gamePhases);
            if (gamePhases == null || gamePhases.size() == 0){
                logger.log(Level.SEVERE,"Error while retrieving phases");
               // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }


            System.out.println("User:" + userName);

            String gameId = request().body().asFormUrlEncoded().get("gameid")[0];
            initialGameStat.setGameId(gameId);
            if(!GameUtility.getResources(initialGameStat)) {
                logger.log(Level.SEVERE,"Error while retrieving resources");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }
            System.out.println("GAME ID FOUND:" + gameId);

            List<String> playersInTheGame = SessionManager.getAllUsers(gameId);
            System.out.println("Players in the game:" + playersInTheGame.toString());

            if (gameId == null || playersInTheGame == null || gameId.isEmpty() || playersInTheGame.size() == 0){
                logger.log(Level.SEVERE,"Illegal start of the game");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }

            //Get TimeforEachMove and No of Steps
            if(!GameUtility.getTimeBound(initialGameStat,gameId)) {
                logger.log(Level.SEVERE,"Error while getting time bound");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }

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
            if(!GameUtility.updateStartTimeInGameTable(gameId)){
                logger.log(Level.SEVERE,"Error while updating start time");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }


            //Get List of Observers

            //Insert Player Project Steps Status into table
            if (!GameUtility.insertIntoPlayerProjectStepStatus(playersInTheGame, allProjectStepIds)){
                logger.log(Level.SEVERE,"Error while entering project step status");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }


            //Enter players with their specific order for taking turns during the game
            if (!GameUtility.insertIntoOrdering(gameId, playersInTheGame)){
                logger.log(Level.SEVERE,"Error while inserting order");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }


            if(!GameUtility.insertSnapshots(playersInTheGame,initialGameStat)){
                logger.log(Level.SEVERE,"Error while inserting into Snapshots");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }

            //Generate Random Risk cards for players
            if(!GameUtility.generateRandomRiskCardsForPlayer(playersInTheGame,configId)){
                logger.log(Level.SEVERE,"Error while mapping risks to players");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }
            //Enter risk for individual players with status in some table

            //Identify whose turn is first

            //Make him play the game

            //Map<Phase,List<ProjectStep>> phaseProjectStepMapping = getProjectSteps(configId,phases);


            //Update the game table with start time and list of players
            return ok(views.html.ProjectStep.render(initialGameStat));
        }catch(Exception e){
            System.out.println("Error while starting the game");
            logger.log(Level.SEVERE,"Error while starting the game");
            return ok(views.html.error.render());

        }
    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Result performStep(){



        String steptype = "projectstep";
        if(!validateSession()) return ok(views.html.index.render());

        String gamePlayerId = session().get("gameplayerid");

        JsonNode body = request().body().asJson();
        String gameId = body.get("gameid").asText();
        Snapshot currentStep = GameUtility.getCurrentDetailsFromTheUser(gamePlayerId,body);
        OOPS currentOOPS = new OOPS();
        ObjectNode result = play.libs.Json.newObject();
        SURPRISE currentSurprise = new SURPRISE();

        String type = currentStep.getMoveType();
        String projectStepId = currentStep.getProjectStepId();
        int turnNo = currentStep.getTurnNo();
        RiskCard rc = null;

        Snapshot previousStep = GameUtility.getPreviousSnapshot(gamePlayerId,turnNo - 1);


//        if(!GameUtility.validateStep(previousStep,currentStep))return badRequest("User tampered the data on the frontend");


        //In case of skip step, just update the database and return
        if(type.equalsIgnoreCase("skipstep")) {
            if(!GameUtility.performStep(gamePlayerId,currentStep, Constants.PerformStep.PROJECTSTEP)){
                logger.log(Level.SEVERE,"Error while updating status");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());

            }
            GameUtility.addReturningResources(currentStep);
        }
        else if(type.equalsIgnoreCase("projectstep")) {


            if (GameUtility.isProjectStepPerformed(projectStepId, gamePlayerId))
                return badRequest("You already performed this step");
            ProjectStep projectStep = GameUtility.getProjectStepDetails(projectStepId);
            if (projectStep == null){
                logger.log(Level.SEVERE,"Error while retrieving project step details");
                // System.out.println("Error while retrieving phases.");
                return ok(views.html.error.render());
            }



            Constants.PerformStep performAction = GameUtility.getActiontobeTaken(projectStep.getLevel(),currentStep.getCapabilityBonus());

            if(performAction == Constants.PerformStep.OOPS)
            {
                steptype = "oops";

                GameUtility.performOOPS(currentStep,currentOOPS);
                if (!GameUtility.performStep(gamePlayerId, currentStep,performAction)) {
                    logger.log(Level.SEVERE,"Error while updating status");
                    // System.out.println("Error while retrieving phases.");
                    return ok(views.html.error.render());

                }

                result.put("oops_resource",currentOOPS.getResources());
                result.put("oops_budget",currentOOPS.getBudget());
                result.put("oops_points",currentOOPS.getCapabilityPoints());
                result.put("oops_bonus",currentOOPS.getCapabilityBonus());

                GameUtility.addReturningResources(currentStep);
                currentStep.setTwoTurn(currentStep.getCurrentStepResource());
            }
            else if(performAction == Constants.PerformStep.SURPRISE){
                steptype = "surprise";
                GameUtility.performSurprise(currentStep,currentSurprise);
                if (!GameUtility.performStep(gamePlayerId, currentStep,performAction)) {
                    logger.log(Level.SEVERE,"Error while updating status");
                    // System.out.println("Error while retrieving phases.");
                    return ok(views.html.error.render());

                }

                result.put("surprise_resource",currentSurprise.getResources());
                result.put("surprise_budget",currentSurprise.getBudget());
                result.put("surprise_points",currentSurprise.getCapabilityPoints());
                result.put("surprise_bonus",currentSurprise.getCapabilityBonus());

                GameUtility.addReturningResources(currentStep);
                currentStep.setTwoTurn(currentStep.getCurrentStepResource());
            }
            else{
                if (!GameUtility.canProjectStepBePerformed(currentStep, projectStep))
                    return badRequest("The project step cannot be performed with current budget,personnel,capabilityPoints, capabilityBonus");

                if (!GameUtility.performStep(gamePlayerId, currentStep,performAction)) {
                    logger.log(Level.SEVERE,"Error while updating status");
                    // System.out.println("Error while retrieving phases.");
                    return ok(views.html.error.render());

                }

                if (!GameUtility.updateProjectStepStatus(projectStepId, gamePlayerId)){
                    logger.log(Level.SEVERE,"Error while updating project step status");
                    // System.out.println("Error while retrieving phases.");
                    return ok(views.html.error.render());

                }



                GameUtility.addReturningResources(currentStep);
                currentStep.setTwoTurn(projectStep.getPersonnel());
            }


          //  GameUtility.addReturningResources(currentStep);
           // currentStep.setTwoTurn(projectStep.getPersonnel());
        }
        else if(type.equalsIgnoreCase("risk")){
            String riskId = body.get("riskid").asText();
            currentStep.setRiskId(riskId);
            double performedSteps = currentStep.getPerformedSteps();
            double totalSteps = currentStep.getTotalSteps();
            double successProbability = (performedSteps/totalSteps) *  100;
            System.out.println("Probability:" + successProbability);

            Random rand = new Random();
            int randomnumber = rand.nextInt(100) + 1;
            boolean success = false;

            if(successProbability >= 0.0    ){
                success = true;
            }

            if(success){

                //Get Risk details
                rc = GameUtility.getRiskDetails(riskId);
                //Mitigate the risk
                if(!GameUtility.mitigateRisk(currentStep,rc)){

                    return badRequest("Error while mitigating risk");
                }
                //update risk status for the player
                if(!GameUtility.updateRiskStatus(gamePlayerId,riskId)){
                    return badRequest("Error while updating risk status");
                }
                GameUtility.performStep(gamePlayerId,currentStep,Constants.PerformStep.RISK);
                GameUtility.addReturningResources(currentStep);
                currentStep.setTwoTurn(rc.getPersonnel());

            }else{
                GameUtility.addReturningResources(currentStep);
            }
            //Add step to project snapshot


        }


            //ObjectNode result = play.libs.Json.newObject();
            if(GameUtility.isGameComplete(currentStep.getTurnNo(),gameId))result.put("complete","true");
            currentStep.setTurnNo(currentStep.getTurnNo() + 1);
            result.put("message","success");
            result.put("budget",currentStep.getBudget());
            result.put("personnel",currentStep.getPersonnel());
            result.put("capabilitybonus",currentStep.getCapabilityBonus());
            result.put("capabilitypoints",currentStep.getCapabilityPoints());
            result.put("currentturn",currentStep.getTurnNo());
            result.put("skipturn",currentStep.isSkipTurnStatus());
            result.put("oneturn",currentStep.getOneTurn());
            result.put("twoturn",currentStep.getTwoTurn());
            result.put("steptype",currentStep.getMoveType());





            return ok(result);


    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getProjectSteps(){

        String phaseId = request().body().asJson().get("phaseId").asText();
        String gamePlayerId = session().get("gameplayerid");
        List<ProjectStep> projectSteps = GameUtility.getProjectSteps(phaseId,gamePlayerId);
            return ok(play.libs.Json.toJson(projectSteps));

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


    public static Result getRiskCards(){
        String gamePlayerId = session().get("gameplayerid");
        List<RiskCard> risks = GameUtility.getRisks(gamePlayerId);
        if(risks == null || risks.size()==0) {
            logger.log(Level.SEVERE,"Error while retrieving risk cards");
            // System.out.println("Error while retrieving phases.");
            return ok(views.html.error.render());

        }
        System.out.println("RISKS:" + risks.toString());
        return ok(play.libs.Json.toJson(risks));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMitigationSteps(){
        String riskId = request().body().asJson().get("riskid").asText();
        String gamePlayerId = session().get("gameplayerid");
        List<ProjectStep> mitigationCards = GameUtility.getMitigationCards(riskId,gamePlayerId);
        System.out.println("Mitigation Cards:"+ mitigationCards);
        return ok(play.libs.Json.toJson(mitigationCards));
    }
}