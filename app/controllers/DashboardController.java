package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Routes;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utility.Constants;
import utility.GameUtility;
import models.ActiveGames;


import java.sql.*;
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
            while (rs.next()) {
                ActiveGames actobj = new ActiveGames();
                actobj.setStatus(rs.getString("status"));
                actobj.setGametime(rs.getString("start_time"));
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
