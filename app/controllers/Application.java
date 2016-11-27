package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.PlayerSocket;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;

/**
 * Takes care of the socket connections
 */
public class Application extends Controller {
    /**
     * Index rendering
     * @return
     */
    public static Result index() {
        return ok(index.render());
    }

    /**
     * When player joins, establishes socket connection
     * @return
     */
    public static Result playerJoin() {

        return ok(views.html.player.render());
    }

    /**
     * When game is hosted, socket connection establishment call
     * @return
     */
    public static Result hostJs() {
        return ok(views.html.hostjs.render()).as("application/javascript");
    }

    /**
     * Sockets code
     * @return
     */
    public static Result wsJs() {
        return ok(views.html.ws.render()).as("application/javascript");
    }

    /**
     * Sockets code
     * @return
     */
    public static Result gameJs() {
        return ok(views.html.gamejs.render()).as("application/javascript");
    }

    /**
     * Socket establishing code
     * @return
     */
    public static WebSocket<JsonNode> wsInterface(){
        return new WebSocket<JsonNode>(){

            // called when web socket handshake is done
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                PlayerSocket.start(in, out);
            }
        };
    }
}