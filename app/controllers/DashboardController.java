package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Routes;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import java.util.*;

import java.util.*;

/**
 * Created by nivas on 25/9/16.
 */
public class DashboardController extends Controller {



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
