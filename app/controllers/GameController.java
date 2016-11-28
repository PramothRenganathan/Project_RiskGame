package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.Play;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utility.Constants;
import utility.GameUtility;
import utility.StartGameUtility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * All game related routes implemented in this class
 */
public class GameController extends Controller {

    public static final Logger logger = Logger.getLogger(GameController.class.getName());


    /**
     * When a host creates a game, this route is hit
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result hostGame(){
        //Check if session is set for the user.
        //for every game thats created generate a gameId and insert into the db
        //Number of players to be invited, number of turns, etc
        // Insert the details into the db

        ObjectNode result = play.libs.Json.newObject();
        if(!validateSession())
            return ok(views.html.index.render());
        String userName = session().get(Constants.USERNAME);

        //Get all the necessary request parameters
        JsonNode json = request().body().asJson();
        boolean isTimeBound = json.get("istimebound").asBoolean();
        int timeForEachMove = json.get("timeforeachmove").asInt();
        int stepsForEachPlayer = json.get("stepsforeachplayer").asInt();

        String gameId = StartGameUtility.generateGameId();
        //Insert gameId into table
        logger.log(Level.FINE, "Inserting into game table");

        if(!GameUtility.insertIntoGame(gameId,userName,timeForEachMove,stepsForEachPlayer,isTimeBound)) {
        result.put(Constants.ERRORMSG,"Error while starting the game. Contact system admin");
            result.put(Constants.MESSAGE, Constants.FAILURE);
            return ok(result);

        }
        logger.log(Level.FINE, "Inserted into game table");
        logger.log(Level.FINE, "Inserting into game player table");
        String gamePlayerId = GameUtility.insertIntoGamePlayer(gameId,userName,false);//Host cannot be observer
        if( gamePlayerId == null  || gamePlayerId.isEmpty()) {
            result.put(Constants.ERRORMSG,"Error while hosting the game. Contact system admin");
            result.put(Constants.MESSAGE, Constants.FAILURE);
            return ok(result);

        }
        logger.log(Level.FINE, "Inserted into game player table");

        session().put(Constants.GAMEPLAYERID,gamePlayerId);
        result.put(Constants.GAMEID,gameId);
        result.put(Constants.MESSAGE, Constants.SUCCESS);

        /**
         *  WEB SOCKETS CODE
         *
         */
        SessionManager.addUser(gameId,gamePlayerId);
        return ok(result);
    }

    /**
     * Every snapshot is retrieved using this route.
     * @return
     * @throws IOException
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSnapshotDetails() throws IOException {
        String gameid = request().body().asJson().get(Constants.GAMEID).asText();
        File file = new File(Constants.PUBLIC_IMAGES + gameid);
        String[] playerDirectories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });


        List<Directory> model = new ArrayList<>();

        if(playerDirectories!=null && playerDirectories.length!=0) {
            for (String directory : playerDirectories) {
                String[] snapshots;
                file = new File(Constants.PUBLIC_IMAGES + gameid + "/" + directory);
                snapshots = file.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isFile();
                    }
                });
                if(snapshots!=null && snapshots.length>0){
                    model.add(new Directory(directory, snapshots));
                }

            }//for
        }

        return ok(play.libs.Json.toJson(model));
    }

    /**
     * Route hit to save image of the games snapshots
     * @return
     * @throws IOException
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result saveImageSnapshot() throws IOException {
        String imageData = request().body().asJson().get("image-data").asText();
        String username = request().body().asJson().get(Constants.USERNAME).asText();
        String gameid = request().body().asJson().get(Constants.GAMEID).asText();
        String turn = request().body().asJson().get("turnNo").asText();

        String base64Image = imageData.split(",")[1];

        // Convert the image code to bytes.
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        StringBuilder sb = new StringBuilder();
        sb.append(Constants.PUBLIC_IMAGES);
        sb.append(gameid + "/");
        sb.append(username.split("@")[0] + "/");
        File imageDirectory = new File(sb.toString());

        if(!imageDirectory.exists()){
            imageDirectory.mkdirs();
        }

        sb = new StringBuilder();
        sb.append(Constants.PUBLIC_IMAGES);
        sb.append(gameid + "/" + username.split("@")[0] + "/" + turn);
        sb.append(".png");

        File imageFile = new File(sb.toString());

        ImageIO.write(bufferedImage, "png", imageFile);

        return ok("hello");
    }


    /**
     * When a player tries to join a game, this route is hit
     * @return
     */
    public static Result joinGame(){
        //Check if user is logged in
        if(!validateSession()){
            ok(views.html.index.render());
        }
        ObjectNode result = play.libs.Json.newObject();
        String userName = session().get(Constants.USERNAME);

        //Check if gameId is sent through the request
        JsonNode json = request().body().asJson();
        String gameId =json.get(Constants.GAMEID).asText();
        logger.log(Level.FINE, "GameId:" + gameId);
        boolean isObserver = json.get("isobserver").asBoolean();
        if(gameId == null || gameId.isEmpty()){
            result.put(Constants.ERRORMSG,"Error with the request, gameId not found. Contact system admin");
            result.put(Constants.MESSAGE, Constants.FAILURE);
            return ok(result);

        }

        //CHECK IF THE GAME EXISTS
        logger.log(Level.FINE, "Checking of game Id :" + gameId + " exists");
        /*
        TO DO -- CHECK IF THE GAME IS IN HOSTED STATUS
        IF GAME IS NOT TIME BOUND, ALLOW PLAYERS TO JOIN EVEN IF GAME IS RUNNING
        ELSE DONT ALLOW
         */

        if(!StartGameUtility.gameExists(gameId)){
            result.put(Constants.ERRORMSG,"Game Id doesnt exist. Contact system admin");
            result.put(Constants.MESSAGE, Constants.FAILURE);
            return ok(result);

        }
        logger.log(Level.FINE, "Game Id exists");
        //If host of the game tries to join the game, REJECT THE request
        logger.log(Level.FINE, "Checking if requested person is host again");
        if(StartGameUtility.isHost(gameId,userName)){
            result.put(Constants.ERRORMSG,"You cannot do that as host. Contact system admin");
            result.put(Constants.MESSAGE, Constants.FAILURE);
            return ok(result);

        }

        //INSERT ENTRY INTO GAME_PLAYER TABLE
        String gamePlayerid = GameUtility.insertIntoGamePlayer(gameId,userName,isObserver);
        session().put(Constants.GAMEPLAYERID,gamePlayerid);

        /**
         * CREATE SOCKET FOR THE USER AND REDIRECT TO THE HOSTED PAGE
         */
        if(SessionManager.getAllUsers(gameId).size()>=5){
            result.put(Constants.ERRORMSG,"Already 5 players in the game.");
            result.put(Constants.MESSAGE, Constants.FAILURE);
            return ok(result);

        }
        if(!SessionManager.hasUser(gameId, gamePlayerid)){
            SessionManager.addUser(gameId, gamePlayerid);
        }

        result.put(Constants.GAMEPLAYERID,gamePlayerid);
        result.put(Constants.MESSAGE, Constants.SUCCESS);
        return ok(result);
    }

    /**
     * Method for observe game
     * @return
     */
    public static Result observeGame()
    {
        String userName = session().get(Constants.USERNAME);
        String gameId = request().body().asFormUrlEncoded().get(Constants.GAMEID)[0];
        List<String> parameters = new ArrayList<>();
        parameters.add(userName);
        parameters.add(gameId);
        return ok(views.html.observer.render(parameters));
    }

    /**
     * Initial game stats are persisted and game is started
     * @return
     */
    public static Result startGame(){
        try {
            //Based on the configuration - > load the phases, projectSteps, Risks, MitigationSteps
            if(!validateSession())
                return ok(views.html.index.render());

            String configId = Play.application().configuration().getString("config_id");
            InitialGameStat initialGameStat = new InitialGameStat();
            String userName = session().get(Constants.USERNAME);
            initialGameStat.setUserName( userName );
            List<Phase> gamePhases = GameUtility.getPhases(configId);
            initialGameStat.setPhases(gamePhases);
            List<String> allProjectStepIds = StartGameUtility.getAllProjectSteps(gamePhases);
            if (gamePhases == null || gamePhases.isEmpty()){
                logger.log(Level.SEVERE,"Error while retrieving phases");
                return ok(views.html.error.render());

            }
            logger.log(Level.FINE, "User:" + userName);
            String gameId = request().body().asFormUrlEncoded().get(Constants.GAMEID)[0];
            initialGameStat.setGameId(gameId);
            if(!StartGameUtility.getResources(initialGameStat)) {
                logger.log(Level.SEVERE,"Error while retrieving resources");
                return ok(views.html.error.render());

            }
            logger.log(Level.FINE, "GAME ID FOUND:" + gameId);

            List<String> playersInTheGame = SessionManager.getAllUsers(gameId);
            logger.log(Level.FINE, "Players in the game:" + playersInTheGame.toString());

            if (gameId.isEmpty() || playersInTheGame.isEmpty()){
                logger.log(Level.SEVERE,"Illegal start of the game");
                return ok(views.html.error.render());

            }

            //Get TimeforEachMove and No of Steps
            if(!StartGameUtility.getTimeBound(initialGameStat,gameId)) {
                logger.log(Level.SEVERE,"Error while getting time bound");
                return ok(views.html.error.render());

            }

            //Set turn for each player
            String gamePlayerId = session().get(Constants.GAMEPLAYERID);
            initialGameStat.setTurnNo(SessionManager.getAllUsers(gameId).indexOf(gamePlayerId) + 1);
            initialGameStat.setSkipTurn(false);
            initialGameStat.setOneTurn(0);//Resources to be back after one turn
            initialGameStat.setTwoTurn(0);//Resources to be back after one turn
            //If not host, just redirect to the game page.
            if(!StartGameUtility.isHost(gameId,userName)){
                return ok(views.html.ProjectStep.render(initialGameStat));
            }
            logger.log(Level.FINE, "IM HOST ONLY:" + userName);
            //update startTime in GAME TABLE
            if(!StartGameUtility.updateStartTimeInGameTable(gameId)){
                logger.log(Level.SEVERE,"Error while updating start time");
                return ok(views.html.error.render());

            }


            //Get List of Observers

            //Insert Player Project Steps Status into table
            if (!StartGameUtility.insertIntoPlayerProjectStepStatus(playersInTheGame, allProjectStepIds)){
                logger.log(Level.SEVERE,"Error while entering project step status");
                return ok(views.html.error.render());

            }


            //Enter players with their specific order for taking turns during the game
            if (!StartGameUtility.insertIntoOrdering(gameId, playersInTheGame)){
                logger.log(Level.SEVERE,"Error while inserting order");
                return ok(views.html.error.render());

            }


            if(!StartGameUtility.insertSnapshots(playersInTheGame,initialGameStat)){
                logger.log(Level.SEVERE,"Error while inserting into Snapshots");
                return ok(views.html.error.render());

            }

            //Generate Random Risk cards for players
            if(!GameUtility.generateRandomRiskCardsForPlayer(playersInTheGame,configId)){
                logger.log(Level.SEVERE,"Error while mapping risks to players");
                return ok(views.html.error.render());

            }
            //Enter risk for individual players with status in some table

            //Identify whose turn is first

            //Make him play the game


            //Update the game table with start time and list of players
            return ok(views.html.ProjectStep.render(initialGameStat));
        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while starting the game:" + e);
            return ok(views.html.error.render());

        }
    }

    /**
     * When every step is performed in the game, this route is called
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result performStep(){
        if(!validateSession())
            return ok(views.html.index.render());

        String gamePlayerId = session().get(Constants.GAMEPLAYERID);

        JsonNode body = request().body().asJson();
        String gameId = body.get(Constants.GAMEID).asText();
        Snapshot currentStep = GameUtility.getCurrentDetailsFromTheUser(body);
        OOPS currentOOPS = new OOPS();
        ObjectNode result = play.libs.Json.newObject();
        SURPRISE currentSurprise = new SURPRISE();

        String type = currentStep.getMoveType();
        String projectStepId = currentStep.getProjectStepId();
        int turnNo = currentStep.getTurnNo();
        RiskCard rc;


        //In case of skip step, just update the database and return
        if("skipstep".equalsIgnoreCase(type)) {
            currentStep.setMoveStatus(true);
            currentStep.setSkipTurnStatus(true);
            if(!GameUtility.performStep(gamePlayerId,currentStep, Constants.PerformStep.PROJECTSTEP)){
                logger.log(Level.SEVERE,"Error while updating step data");
                return ok(views.html.error.render());

            }
            GameUtility.addReturningResources(currentStep);
            currentStep.setTwoTurn(currentStep.getCurrentStepResource());
        }
        else if("projectstep".equalsIgnoreCase(type)) {
            currentStep.setMoveStatus(true);
            if (StartGameUtility.isProjectStepPerformed(projectStepId, gamePlayerId))
                return badRequest("You already performed this step");
            ProjectStep projectStep = GameUtility.getProjectStepDetails(projectStepId);
            if (projectStep == null){
                logger.log(Level.SEVERE,"Error while retrieving project step details");
                return ok(views.html.error.render());
            }



            Constants.PerformStep performAction = GameUtility.getActiontobeTaken(projectStep.getLevel(),currentStep.getCapabilityBonus());

            if(performAction == Constants.PerformStep.OOPS)
            {
                GameUtility.performOOPS(currentStep,currentOOPS);
                if (!GameUtility.performStep(gamePlayerId, currentStep,performAction)) {
                    logger.log(Level.SEVERE,"Error while updating oops status");
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
                GameUtility.performSurprise(currentStep,currentSurprise);
                if (!GameUtility.performStep(gamePlayerId, currentStep,performAction)) {
                    logger.log(Level.SEVERE,"Error while updating surprise status");
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
                if (!GameUtility.canProjectStepBePerformed(currentStep, projectStep)) {
                    logger.log(Level.SEVERE, "The project step cannot be performed with current budget,personnel,capabilityPoints, capabilityBonus");
                    return ok(views.html.error.render());
                }
                if (!GameUtility.performStep(gamePlayerId, currentStep,performAction)) {
                    logger.log(Level.SEVERE,"Error while updating proj step status");
                    return ok(views.html.error.render());

                }

                if (!StartGameUtility.updateProjectStepStatus(projectStepId, gamePlayerId)){
                    logger.log(Level.SEVERE,"Error while updating project step status");
                    return ok(views.html.error.render());

                }
                GameUtility.addReturningResources(currentStep);
                currentStep.setTwoTurn(projectStep.getPersonnel());
            }

        }
        //When the player performs risk mitigation
        else if("risk".equalsIgnoreCase(type)){
            String riskId = body.get("riskid").asText();
            currentStep.setRiskId(riskId);
            double performedSteps = currentStep.getPerformedSteps();
            double totalSteps = currentStep.getTotalSteps();

            double successValue = (performedSteps*100)/totalSteps;
            logger.log(Level.FINE, "Probability:" + successValue);

            boolean success = false;

            Random rand = new Random();


            if(successValue >= rand.nextInt(100) ){
                    success = true;
            }

            if(success){

                //Get Risk details
                rc = GameUtility.getRiskDetails(riskId);
                //Mitigate the risk
                if(!GameUtility.mitigateRisk(currentStep,rc)){
                    logger.log(Level.SEVERE,"Error while mitigating risk");
                    return ok(views.html.error.render());
                }
                //update risk status for the player
                if(!GameUtility.updateRiskStatus(gamePlayerId,riskId)){
                    logger.log(Level.SEVERE,"Error while updating risk status");
                    return ok(views.html.error.render());

                }
                GameUtility.performStep(gamePlayerId,currentStep,Constants.PerformStep.RISK);
                GameUtility.addReturningResources(currentStep);
                currentStep.setTwoTurn(rc.getPersonnel());

            }else{
                currentStep.setMoveStatus(false);
                GameUtility.performStep(gamePlayerId,currentStep,Constants.PerformStep.RISK);
                GameUtility.addReturningResources(currentStep);
            }


        }
            //Check if game is complete
            if(GameUtility.isGameComplete(currentStep.getTurnNo(),gameId, gamePlayerId)) {
                result.put("complete", "true");
            }
            //Check if game is complete for all players in the game
            if(StartGameUtility.isGameCompleteForAllPlayers(SessionManager.getAllUsers(gameId))){
                if(!StartGameUtility.updateCompletionOfGame(gameId)) {
                    logger.log(Level.SEVERE, "Error while updating completion of game");
                }
                SessionManager.removeGame(gameId);
            }
            currentStep.setTurnNo(currentStep.getTurnNo() + 1);
            result.put(Constants.MESSAGE, Constants.SUCCESS);
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

    /**
     * Get list of project steps given a phase Id
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getProjectSteps(){

        String phaseId = request().body().asJson().get("phaseId").asText();
        String gamePlayerId = session().get(Constants.GAMEPLAYERID);
        List<ProjectStep> projectSteps = GameUtility.getProjectSteps(phaseId,gamePlayerId);
            return ok(play.libs.Json.toJson(projectSteps));

    }

    /**
     * Session validator
     * @return
     */
    public static boolean validateSession(){
        logger.log(Level.FINE, "In validate session");
        logger.log(Level.FINE, session().get(Constants.USERNAME));
        if(session().isEmpty() || session().get(Constants.USERNAME) == null || session().get(Constants.USERNAME).isEmpty() ){
            logger.log(Level.FINE, "Login to host a game");
            return false;
        }

        return true;
    }

    /**
     * Get list of risk cards for a given player
     * @return
     */
    public static Result getRiskCards(){
        String gamePlayerId = session().get(Constants.GAMEPLAYERID);
        List<RiskCard> risks = GameUtility.getRisks(gamePlayerId);
        if(risks == null || risks.isEmpty()) {
            logger.log(Level.SEVERE,"Error while retrieving risk cards");
            return ok(views.html.error.render());

        }
        logger.log(Level.FINE, "RISKS:" + risks.toString());
        return ok(play.libs.Json.toJson(risks));
    }

    /**
     * Gets mitigation steps for a given risk id
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getMitigationSteps(){
        String riskId = request().body().asJson().get("riskid").asText();
        String gamePlayerId = session().get(Constants.GAMEPLAYERID);
        List<ProjectStep> mitigationCards = GameUtility.getMitigationCards(riskId,gamePlayerId);
        logger.log(Level.FINE, "Mitigation Cards:"+ mitigationCards);
        return ok(play.libs.Json.toJson(mitigationCards));
    }
}