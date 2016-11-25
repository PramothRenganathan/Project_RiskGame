package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import play.db.DB;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static play.mvc.Http.Context.Implicit.request;
import static play.mvc.Http.Context.Implicit.session;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;


import utility.Constants;
import utility.GameUtility;
import views.html.*;

/**
 * Created by srijithkarippure on 9/25/16.
 */


public class LoginController {

    public static final Logger logger = Logger.getLogger(LoginController.class.getName());

    /**
     * Constructor
     */
    public LoginController(){

    }
    /**
     * Method called when login route is hit
     * Creates a user session and returns a cookie as part of response
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result login(){

        JsonNode data = request().body().asJson();
        String userName = data.get(Constants.USERNAME).asText();
        String password = data.get("password").asText();
        if (session().isEmpty() ) {


            Connection conn = DB.getConnection();
            PreparedStatement stmt = null;
            try {
                String query = "SELECT first_name from USERS where player_id = ? and password = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, userName);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();


                while (rs.next()) {
                    logger.log(Level.FINE, "Creating session");
                    session().put("firstname",rs.getString("first_name"));

                    session().put("username",userName);

                    //change -- to main dashboard page
                    return ok("success");
                }
                 return ok("Credentials wrong");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while login :" + e.getMessage());
            }
            finally {
                GameUtility.cleanUp(stmt,conn);
            }

        }
        else {
            String value = session().get(Constants.USERNAME);
            if(value.equalsIgnoreCase(userName)) {
                logger.log(Level.FINE, "User already logged in");
                return ok("success");
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
            logger.log(Level.FINE, "Please login to use this logout function");
            return badRequest("You are not logged in to perform this action");
        }
        else{
            session().clear();
            logger.log(Level.FINE, "Username:" + session().get(Constants.USERNAME));
            logger.log(Level.FINE, "Successfully logged out");
            return ok(views.html.index.render());
        }
    }

    /**
     * Used for testing purpose.
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result testMethod1()
    {
        String node = request().body().asJson().get("firstname").toString();
        return ok(node);
    }

    }





