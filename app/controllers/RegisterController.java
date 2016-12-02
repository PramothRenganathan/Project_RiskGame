package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.db.DB;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utility.GameUtility;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 * Created by srijithkarippure on 9/27/16.
 */

public class RegisterController extends Controller {

    public static final Logger logger = Logger.getLogger(RegisterController.class.getName());
    /**
     * Registering a user
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result register(){
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement stmt = null;
        try {
            JsonNode data = request().body().asJson();
            logger.log(Level.FINE, "Data from register:" + data);
            String userName = data.get("username").asText();
            String password = data.get("password").asText();
            String firstName = data.get("firstname").asText();
            String lastName = data.get("lastname").asText();
            boolean isCmu = Integer.parseInt(data.get("iscmu").asText()) == 1 ? true : false;
            String andrewId = data.get("andrewid") != null ? data.get("andrewid").asText() : null;

            //Check if user with same username already exists.
            conn = DB.getConnection();

            String query = "SELECT first_name FROM USERS WHERE player_id = ?";
            checkStmt = conn.prepareStatement(query);
            checkStmt.setString(1 , userName);
            ResultSet rs = checkStmt.executeQuery();
            if(rs!=null && rs.next()){

                return ok("User already present with this username");
            }
            else{ // User not present, insert into the users database

                query = "INSERT INTO USERS (player_id, password, first_name, last_name, isCMU, andrew_id, isAdmin) " +
                        "VALUES (?,?,?,?,?,?,?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1,userName);
                stmt.setString(2,password);
                stmt.setString(3,firstName);
                stmt.setString(4,lastName);
                stmt.setBoolean(5,isCmu);
                if(andrewId == null)
                    stmt.setNull(6, Types.VARCHAR);
                else
                    stmt.setString(6, andrewId);

                stmt.setBoolean(7,false);//Is Admin, admins are manually added to db

                int success = stmt.executeUpdate(); // Returns 1 if successfully inserted
                return success > 0 ? ok("success") : ok("Error while inserting in the db");
            }
        }
        catch(Exception e){
            logger.log(Level.SEVERE,"Error while registering:"  +  e);
            return ok("Enter all the required fields");
        }
        finally {
            if(stmt!=null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error while closing stmt" + e);
                }
            GameUtility.cleanUp(checkStmt,conn);
        }


    }
}
