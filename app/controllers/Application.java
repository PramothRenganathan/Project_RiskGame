package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.PlayerSocket;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    public static Result playerJoin() {

        return ok(views.html.player.render());
    }

    public static Result hostJs() {
        return ok(views.html.hostjs.render()).as("application/javascript");
    }
    public static Result wsJs() {
        return ok(views.html.ws.render()).as("application/javascript");
    }

    public static WebSocket<JsonNode> wsInterface(){
        return new WebSocket<JsonNode>(){

            // called when web socket handshake is done
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                PlayerSocket.start(in, out);
            }
        };
    }
}