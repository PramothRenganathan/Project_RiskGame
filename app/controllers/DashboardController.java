package controllers;

import models.ActiveGames;
import play.db.DB;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static Result ViewDashboard(){
       System.out.println("im here");
       // response().setContentType("text/html");
        String firstname = session().get("firstname");
        Http.Context.current().args.put("firstname", firstname);
        String userName = session().get("username");
        Http.Context.current().args.put("username", userName);
        //return ok(views.html.ProjectDashbard.render());
        return ok(views.html.ProjectDashbard.render());
    }

    public static Result ViewHostGame(){
        System.out.println(request().body());
        System.out.println("im here");
        String gameId = request().body().asFormUrlEncoded().get("hgameid")[0];
        System.out.println("GAME:" + gameId);
        return ok(views.html.HostGame.render(gameId));
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = (seconds/ 1000) % 60;
        long m = (seconds / (60 * 1000)) % 60;
        long h = (seconds / (60 * 60 * 1000));
        return String.format("%d:%02d:%02d", h,m,s);
    }

   // @BodyParser.Of(BodyParser.Json.class)
    public static Result ActiveGames(){

        String query = "SELECT * FROM GAME where end_time is null";
        System.out.println("Inside Active Games");
        Connection connection = DB.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;

        try {
            stmt = connection.prepareStatement(query);
            //stmt.setString(1, configId);

            System.out.println("In Active Games");

            rs = stmt.executeQuery();
            System.out.print("DONE QUERY");



        List<ActiveGames> listofgames = new ArrayList<>();
            Calendar currentime = Calendar.getInstance();
            while (rs.next()) {
                ActiveGames actobj = new ActiveGames();
                actobj.setStatus(rs.getString("status"));
              //  ((rs.getTimestamp("start_time")
                if(rs.getTimestamp("start_time") != null)
                {
                    Calendar calobj = Calendar.getInstance();

                    calobj.setTimeInMillis(rs.getTimestamp("start_time").getTime());
                    //Date d1 = calobj.getTime();
                  //  long seconds = currentime.getTimeInMillis() - calobj.getTimeInMillis();
                    long seconds = (currentime.getTimeInMillis() - calobj.getTimeInMillis());
                 //   long seconds = (d2.getTime() - d1.getTime());
                 //   String test = convertSecondsToHMmSs(seconds);
                    actobj.setGametime(convertSecondsToHMmSs(seconds));
                   // actobj.setGametime(String.valueOf(seconds));
                 //   actobj.setGametime(d1.toString());
                   //actobj.setStatus(currentime.getTime().toString());
                }
                else
                {
                actobj.setGametime("Not yet started");
                }

             //  actobj.setGametime((rs.getString("start_time")));
                actobj.setIstimebound(rs.getString("isTimeBound"));
                actobj.setGameid(rs.getString("game_id"));
                listofgames.add(actobj);
            }

            return ok(play.libs.Json.toJson(listofgames));
        }catch(Exception e){
            logger.log(Level.SEVERE,"Error while retrieving active games");
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



    @BodyParser.Of(BodyParser.Json.class)
    public static Result HostGame()
    {
        //String node = request().body().asJson().get("istimebound").toString();
        // System.out.println(node);
       // response().setContentType("");

        return ok("success");
    }

    public static Result JoinGame()
    {
        String userName = session().get("username");
        String gameId = request().body().asFormUrlEncoded().get("jgameid")[0];
        List<String> parameters = new ArrayList<>();
        parameters.add(userName);
        parameters.add(gameId);

        return ok(views.html.join.render(parameters));
    }

}
