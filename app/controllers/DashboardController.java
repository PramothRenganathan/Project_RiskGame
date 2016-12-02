package controllers;

import models.ActiveGames;
import play.db.DB;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utility.Constants;
import utility.GameUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nivas on 25/9/16.
 */
public class DashboardController extends Controller {

    public static final Logger logger = Logger.getLogger(DashboardController.class.getName());

    /**
     * Session validating method
     * @return true if valid
     */
    public static boolean validateSession(){

        if(session().isEmpty() || session().get(Constants.USERNAME) == null || session().get(Constants.USERNAME).isEmpty() ){

            return false;
        }

        return true;
    }

    /**
     * THis method is invoked for viewing project dashboard
     * @return page with status 200
     */
    public static Result viewDashboard(){
       logger.log(Level.FINE,"In view dashboard controller");
        if(!validateSession())
            return ok(views.html.index.render());
        String firstname = session().get("firstname");
        Http.Context.current().args.put("firstname", firstname);
        String userName = session().get(Constants.USERNAME);
        Http.Context.current().args.put(Constants.USERNAME, userName);
        if(session().get("admin")!=null && session().get("admin").equalsIgnoreCase("true")){
            return ok(views.html.InstructorDashboard.render());

        }else{
            return ok(views.html.ProjectDashbard.render());
        }

    }

    /**
     * Renders the host game page
     * @return
     */
    public static Result viewHostGame(){
        logger.log(Level.FINE,"In Host Game method");
        String gameId = request().body().asFormUrlEncoded().get("hgameid")[0];
        logger.log(Level.FINE,"GameId:" + gameId);
        return ok(views.html.HostGame.render(gameId));
    }

    /**
     * Converts time to provided format
     * @param seconds
     * @return
     */
    public static String convertSecondsToHMmSs(long seconds) {
        long s = (seconds/ 1000) % 60;
        long m = (seconds / (60 * 1000)) % 60;
        long h = seconds / (60 * 60 * 1000);
        return String.format("%d:%02d:%02d", h,m,s);
    }

    /**
     * Active games
     * @return
     */
    public static Result activeGames(){

        String query = "select status,start_time,isTimeBound,game_id,first_name,last_name,G.host from GAME G " +
                "join USERS U on U.player_id = G.host where end_time is null";
        logger.log(Level.FINE,"Inside active games");
        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        try {
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
        List<ActiveGames> listofgames = new ArrayList<>();
            Calendar currentime = Calendar.getInstance();
            while (rs.next()) {
                ActiveGames actobj = new ActiveGames();
                if(rs.getString("status").equalsIgnoreCase("HOSTED")){
                    actobj.setStatus("Waiting for players to join");
                }
                else {
                    actobj.setStatus(rs.getString("status"));
                }
                actobj.setFullName(rs.getString("first_name") + " " + rs.getString("last_name"));
                actobj.setHostId(rs.getString("host"));
                if(rs.getTimestamp("start_time") != null)
                {
                    Calendar calobj = Calendar.getInstance();
                    calobj.setTimeInMillis(rs.getTimestamp("start_time").getTime());
                    long seconds = currentime.getTimeInMillis() - calobj.getTimeInMillis();
                    actobj.setGametime(convertSecondsToHMmSs(seconds));
                }
                else
                {
                actobj.setGametime("Not yet started");
                }
                actobj.setIstimebound(rs.getString("isTimeBound"));
                actobj.setGameid(rs.getString("game_id"));
                listofgames.add(actobj);
            }
            return ok(play.libs.Json.toJson(listofgames));
        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while retrieving active games:" + e);
            return badRequest();
        }
        finally{
            GameUtility.cleanUp(stmt,connection);
        }
    }

    /**
     * Host game method
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result hostGame()
    {

        return ok("success");
    }

    /**
     * Join game method
     * @return
     */
    public static Result joinGame()
    {
        logger.log(Level.FINE,"In join Game method");
        String userName = session().get(Constants.USERNAME);
        String gameId = request().body().asFormUrlEncoded().get("jgameid")[0];
        String observer = request().body().asFormUrlEncoded().get("hdn_observer")[0];
        boolean isObserver ="1".equals(observer)? true: false;

        List<String> parameters = new ArrayList<>();
        parameters.add(userName);
        parameters.add(gameId);
        parameters.add(String.valueOf(isObserver));

        return ok(views.html.join.render(parameters));
    }
}