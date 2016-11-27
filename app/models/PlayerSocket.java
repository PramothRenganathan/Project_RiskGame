package models;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSocket{
    private static List<WebSocket.Out<JsonNode>> connections = new ArrayList<WebSocket.Out<JsonNode>>();
    private static Map<String, List<WebSocket.Out<JsonNode>>> gameUsersMap = new HashMap<>();
    private static Map<WebSocket.In<JsonNode>, WebSocket.Out<JsonNode>> inoutMap = new HashMap<>();

    public static void start(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){

        connections.add(out);
        if(!inoutMap.containsKey(in)){
            inoutMap.put(in, out);
        }

        in.onMessage(new Callback<JsonNode>(){
            public void invoke(JsonNode event) throws IOException {
                int count = connections.size();

                Data data = Json.fromJson(event, Data.class);
                WebSocketData wsdata = null;

                if(data.type.equals("RegisterGameId")) {
                    if(gameUsersMap.containsKey(data.gameid)){
                        gameUsersMap.get(data.gameid).add(inoutMap.get(in));
                    }

                    else{
                        List<WebSocket.Out<JsonNode>> list = new ArrayList<>();
                        gameUsersMap.put(data.gameid, list);
                        gameUsersMap.get(data.gameid).add(inoutMap.get(in));
                    }
                }

                else if(data.type.equals("ChangeTurn")){
                    //push the list of all users so that everyone gets updated
                    wsdata = new WebSocketData();
                    wsdata.type = "ChangeTurn";

                    int turnno = Integer.parseInt(data.turnNumber);
                    int totalPlayers = SessionManager.getAllUsers(data.gameid).size();

                    if(totalPlayers==1){
                        turnno = 1;
                        wsdata.turnNumber = String.valueOf(turnno);
                    }

                    else {

                        turnno = (turnno == totalPlayers - 1) ? totalPlayers : (turnno + 1) % totalPlayers;
                        wsdata.turnNumber = String.valueOf(turnno);
                    }
                }

                else if(data.type.equals("TurnUpdate")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    wsdata = new WebSocketData();
                    wsdata.type = "TurnUpdate";
                    wsdata.currentPlayer = data.player.name;
                    wsdata.joinedUsers = activeUsers;
                }

                else if(data.type.equals("StartGame")){
                    //push the list of all users so that everyone gets updated
                    wsdata = new WebSocketData();
                    wsdata.type = "redirect";
                }

                else if(data.type.equals("joined")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    wsdata = new WebSocketData();
                    wsdata.type = "joined";
                    wsdata.joinedUsers = activeUsers;
                }

                else if(data.type.equals("PerformStep")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    wsdata = new WebSocketData();
                    wsdata.type = "UpdateActivityLog";
                    wsdata.joinedUsers = activeUsers;
                    wsdata.player = data.player.username;
                    wsdata.stepName = data.stepName;
                }

                else if(data.type.equals("Timeout")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    wsdata = new WebSocketData();
                    wsdata.type = "Timeout";
                    wsdata.joinedUsers = activeUsers;
                    wsdata.player = data.player.username;
                }

                else if(data.type.equals("SkipTurn")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    wsdata = new WebSocketData();
                    wsdata.type = "SkipTurn";
                    wsdata.joinedUsers = activeUsers;
                    wsdata.player = data.player.username;
                }

                else if(data.type.equals("Chat")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    wsdata = new WebSocketData();
                    wsdata.type = "Chat";
                    wsdata.joinedUsers = activeUsers;
                    wsdata.player = data.player.username;
                    wsdata.message = data.message;
                }

                else if(data.type.equals("leaving")){
                    //push the list of all users so that everyone gets updated
                    List<String> activeUsers = SessionManager.getAllUsers(data.gameid);
                    String userLeaving = data.player.username;

                    wsdata = new WebSocketData();
                    wsdata.type = "leaving";
                    wsdata.leavingUser = userLeaving;
                    wsdata.joinedUsers = activeUsers;
                }

                //toJson method was throwing exception
                //Since class I used was static with non public fields
                //So i think JsonSerializer was not able to serialize the fields
                if(!data.type.equals("RegisterGameId")) {
                    PlayerSocket.notifyAll(Json.toJson(wsdata), data.gameid);
                }
            }
        });

        in.onClose(new Callback0(){
            public void invoke(){

            }
        });
    }

    // Iterate connection list and write incoming message
    public static void notifyAll(JsonNode message, String gameId){
        List<WebSocket.Out<JsonNode>> outnodes = null;
        if(gameUsersMap.containsKey(gameId)){
            outnodes = gameUsersMap.get(gameId);
        }

        if(outnodes!=null){
            for (WebSocket.Out<JsonNode> out : outnodes) {
                out.write(message);
            }
        }
    }
}