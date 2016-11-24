package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import controllers.*;
import play.db.DB;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static play.mvc.Http.Context.Implicit.request;
import static play.mvc.Http.Context.Implicit.session;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import views.html.*;

/**
 * Created by srijithkarippure on 9/25/16.
 */


public class LoginController {
    /**
     * Method called when login route is hit
     * Creates a user session and returns a cookie as part of response
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result login(){

        JsonNode data = request().body().asJson();
        //System.out.println(data);
        String userName = data.get("username").asText();
        String password = data.get("password").asText();
       // System.out.println(request().body());

  //      String userName = request().body().asFormUrlEncoded().get("username")[0];
//        String password = request().body().asFormUrlEncoded().get("password")[0];
        System.out.println("Username:" + userName);
        System.out.println("password:" + password);


        //System.out.println("Session value:" + value);
        if (session().isEmpty() ) {

            System.out.println("User not logged in. Proceed with login");
            Connection conn = DB.getConnection();
            PreparedStatement stmt = null;
            try {
                String query = "SELECT first_name from USERS where player_id = ? and password = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, userName);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                boolean login =false;
                while (rs.next()) {
                    System.out.println("Creating session");
                    session().put("firstname",rs.getString("first_name"));
                    login = true;
                    session().put("username",userName);

                    //change -- to main dashboard page
                   // return redirect(controllers.routes.DashboardController.viewDashboard());
                    return ok("success");
                }
                if(!login) return ok("Credentials wrong");
            } catch (Exception e) {
                System.out.println("Exception while login :" + e.getMessage());
            }
            finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        else {
            String value = session().get("username");
            if(value.equalsIgnoreCase(userName)) {
                System.out.println("User already logged in");
               // return ok("Already Logged in");
                return ok("success");
              //  return redirect(controllers.routes.DashboardController.viewDashboard());
            }
        }

            return ok("Contact System Admin");

        }


    /**
     * Method called when logout route is hit
     * @return
     */
    public static Result logout(){

        if(session().isEmpty()){
            System.out.println("Please login to use this logout function");
            return badRequest("You are not logged in to perform this action");
        }
        else{
            session().clear();
            System.out.println("Username:" + session().get("username"));
            System.out.println("Successfully logged out");
            return ok(views.html.index.render());
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result testMethod1()
    {
        String node = request().body().asJson().get("firstname").toString();
        System.out.println("in testmethod");
        // response().setContentType("");

        return ok(node);
    }

    }





