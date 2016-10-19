$(function() {

    //this flag will be used to know if the player performed any step after timeout
    var timeout = false,
        stopped = false;
    var clock = $('.mytimer').FlipClock({
        clockFace: 'MinuteCounter',
        countdown:true,
        stop: function() {

            if (!timeout && !stopped) {
                 timeout = true;
                 sendTimeoutMessage();

                //In case of timeout, next player should get his turn
                publishTurnNumber();
            }
        }
    });

    //This function should be called whenever each player gets his/her turn
    var startTimer = function(){
        clock.setTime(240);
        clock.start();
    }

    var setTimer = function(time){
        clock.setTime(time);
    }

    var stopTimer = function(){
        stopped = true;
        clock.stop();
    }

    var resetTimer = function(){
        stopped = false;
        timeout = false;
    }

    // startTimer();

    var sendTimeoutMessage = function(){
        stopTimer();
        resetTimer();
        timer.hide();
        var data = JSON.stringify({"gameid": gameid, "type": "Timeout", "player": {"username": username}});
        socket.send(data);
    }

    var username = $('#username').val();
    var gameid = $('#gameid').val();
    var activePlayersList = $('#activePlayersList');
    var timer = $('.mytimer');
    var turn = $('#turn').val();

    // get websocket class, firefox has a different way to get it
    var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;

    // open socket on page load
    var socket = new WS('@routes.Application.wsInterface().webSocketURL()');

    //this list will be used to append joining players
    var userList = $('#list-active-players');

    var writeMessages = function (event) {
    var model = event.data;

    //model is not coming as Json so have to use this jquery json parser
    model = jQuery.parseJSON(model);

    if (model.type == "joined") {

       var tmp_username = username.split("@@")[0];

        var users = model.joinedUsers;
        activePlayersList.html("");
        activePlayersList.append('<b>' + tmp_username + ' (you)' + '</b><br/>');

        for (i = 0; i < users.length; i++) {
            if (users[i].split("-")[0] != tmp_username) {
                activePlayersList.append(users[i].split("-")[0] + '<br/>');
                }//if
            }//for
        }//joined

        else if (model.type == "leaving") {
            var users = model.joinedUsers;
            var leavinguser = model.leavingUser;

            var listItemToBeRemoved = $(userList).find('#' + leavinguser).remove();
        }

        else if (model.type == "redirect") {
           $('#frmHidden').submit();
        }

        else if (model.type == "UpdateActivityLog") {
            var stepName = model.stepName,
                playerName = model.player;

                var activityLog = $('#activity-log');

                var txt = playerName + " performed step: " + stepName;

                activityLog.prepend('<p>' + txt + '</p><hr/>');
        }//UpdateActivityLog

        else if (model.type == "Timeout") {
            var playerName = model.player;

            var activityLog = $('#activity-log');

            var txt = playerName + " missed their turn due to timeout!";

            activityLog.prepend('<p>' + txt + '</p><hr/>');
        }//Timeout

        else if (model.type == "Chat") {
            var playerName = model.player,
                    message = model.message;

            var chatArea = $('#chatArea');

            var txt = "<b>" + playerName + "</b>: " + message;

            chatArea.append('<p>' + txt + '</p><hr/>');
        }//Chat

        else if (model.type == "ChangeTurn") {
            var turnNumber = model.turnNumber,
                playerTurn = $('#turn').val();

            //alert("CTurn: " + turnNumber + ", PTurn: " + playerTurn + ", EStatus: " + (turnNumber == playerTurn));
            if(turnNumber == playerTurn){
                enableMyTurn();
            }

            publishMyTurn();

        }//ChangeTurn

        else if (model.type == "TurnUpdate") {
            var currentPlayer = model.currentPlayer;

        //alert("Username: " + username + ", Current Player: " + currentPlayer);
            if(username != currentPlayer){
                //alert(currentPlayer + "'s " + "turn now..");
            }

        }//TurnUpdate

        else {
            var name = model.name;
            $('#lblCount').text(count);
            $('#divJoined').prepend('<p>' + name + '</p>');
        }
    }

    var publishMyTurn = function (event) {
        var data = JSON.stringify({"gameid":gameid, "type": "TurnUpdate", "player": {"name": username, "team": "somecode"}});
        socket.send(data);
    }

    var initConnection = function (event) {
        registerGameId();
        publishMyPresence();
        checkIfFirstTurn();
    }

    var checkIfFirstTurn = function (event) {
        if(turn=="1"){
            enableMyTurn();
        }
    }

    //put all code inside this method to setup the stage for player who is having the current turn
    var enableMyTurn = function (event) {
        timer.show();
        setTimer(240);
        resetTimer();
        startTimer();
    }

    var publishMyPresence = function (event) {
        //push to socket on first page load of any user
        var data = JSON.stringify({"gameid":gameid, "type": "joined", "player": {"name": username, "team": "somecode"}});
        socket.send(data);
    }

    var registerGameId = function (event) {
        //push to socket on first page load of any user
        var data = JSON.stringify({"gameid":gameid, "type": "RegisterGameId"});
        socket.send(data);
        }

    //read message from socket
    socket.onmessage = writeMessages;

    //without this I am getting exception that CONNECTION NOT ESTABLISHED
    socket.onopen = initConnection;


    $('#btnJoin').click(function (event) {

        var name = $('#txtName').val();
        var code = $('#txtCode').val();


        var data = JSON.stringify({"name": name, "team": code});

        //push to socket
        socket.send(data);

        $('#txtName').val('');
        $('#txtCode').val('');

        $('#txtName').attr('hidden', true);
        $('#txtCode').attr('hidden', true);
        $('#divPlayerStatus').attr('hidden', false);
        $('#btnLeave').attr('hidden', false);
        $('#btnJoin').attr('hidden', true);
    });


    //Event handler for start game event
    $('#btnStart').click(function(e){

        var username = $('#username').val();
        var data = JSON.stringify({"gameid": gameid, "type": "StartGame", "player": {"username": username}});
        socket.send(data);
    });

    //Perform step button event handler
    $('.perform-step').on("click", function(){

        var     topModal = $(this).closest('div.modal'),
                parentModal = topModal.find('div.modal-content'),
                header = parentModal.find('div.modal-header'),
                title = header.find('.modal-title').html();

        //this will hide the modal
        topModal.modal('toggle');
        updateActivityLogs(title);
        publishTurnNumber();

        stopTimer();

        setTimer(240);
        resetTimer();
        timer.hide();
    });

    var publishTurnNumber = function(){
        var turnNumber = $('#turn').val();
        var data = JSON.stringify({"gameid": gameid, "turnNumber":turnNumber, "type": "ChangeTurn"});
        socket.send(data);
    }

    var updateActivityLogs = function (title) {
        var data = JSON.stringify({"gameid": gameid, "stepName":title, "type": "PerformStep", "player": {"username": username}});
        socket.send(data);
    }

    $('#btnSend').click(function () {
        var message = $('#txtMessage').val();
        $('#txtMessage').val('');
        if(message!=""){
            var data = JSON.stringify({"gameid": gameid, "message":message, "type": "Chat", "player": {"username": username}});
            socket.send(data);
        }
    });

    $('#btnLeave').click(function (e) {

        var data = JSON.stringify({"type": "leaving", "player": {"username": username}});
        socket.send(data);

        $.ajax({
            type: "GET",
            url: '/leave',
            data: {'username': username},
            success:function(data)
            {
                window.location.replace("/");
            }
        });
    });
});