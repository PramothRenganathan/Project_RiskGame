package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.db.DB;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.sql.*;

import static play.mvc.Http.Context.Implicit.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;



/**
 * Created by srijithkarippure on 9/27/16.
 */

public class RegisterController {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result register(){
        Connection conn = null;
        try {
            JsonNode data = request().body().asJson();
            System.out.println(data);
            String userName = data.get("username").asText();
            String password = data.get("password").asText();
            String firstName = data.get("firstname").asText();
            String lastName = data.get("lastname").asText();
            boolean isCmu = Integer.parseInt(data.get("iscmu").asText()) == 1 ? true : false;
            String andrewId = data.get("andrewid") != null ? data.get("andrewid").asText() : null;

            System.out.println(andrewId);


            //Check if user with same username already exists.
            conn = DB.getConnection();
            PreparedStatement checkStmt = null;
            String query = "SELECT first_name FROM USERS WHERE player_id = ?";
            checkStmt = conn.prepareStatement(query);
            checkStmt.setString(1 , userName);
            ResultSet rs = checkStmt.executeQuery();
            System.out.print("Im here");
            if(rs!=null && rs.next()){

                return ok("User already present with this username");
            }
            else{ // User not present, insert into the users database
                PreparedStatement stmt = null;
                query = "INSERT INTO USERS (player_id, password, first_name, last_name, isCMU, andrew_id) " +
                        "VALUES (?,?,?,?,?,?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1,userName);
                stmt.setString(2,password);
                stmt.setString(3,firstName);
                stmt.setString(4,lastName);
                stmt.setBoolean(5,isCmu);
                if(andrewId == null)stmt.setNull(6, Types.VARCHAR);
                else stmt.setString(6, andrewId);

                int success = stmt.executeUpdate(); // Returns 1 if successfully inserted
                if(success > 0)return ok("success");
                else return ok("Error while inserting in the db");
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return ok("Enter all the required fields");
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
