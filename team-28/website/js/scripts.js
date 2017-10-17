let BOAT_ACTION = {
    VMG: {value:1},
    SAILS : {value:2},
    TACK_GYBE : {value:4},
    CLOCKWISE : {value:7},
    ANTI_CLOCKWISE : {value:8},
}

/**
 * Upon button press, sends a request game packet and changes the screen to control screen.
 */
function submitButtonPressed(){
    requestGame($("#codeBox").val());
    initButtonListeners();
}

function showWrongGameCodeMessage(string) {
    //Incorrect Room Code Entered
    $("#errorMessage").html(string);
    $("#errorMessage").fadeIn(1000);
}

/**
 * Initialises boat control buttons on website
 * Upwind, downwind have actions for holding down
 */
function initButtonListeners(){
    var timeout;
    $(".boatActionPress, .boatActionHold").click(function (event) {
        let name = $("#"+event.target.id).attr('name');
        createBoatActionMessage(name);
    })
    $(".boatActionHold").mousedown(function (event) {
        timeout = setInterval(function(){
            let name = $("#"+event.target.id).attr('name');
            createBoatActionMessage(name);
        }, 100);
    })
    $(".boatActionHold").mouseup(function(){
        clearInterval(timeout);
        return false;
    });
    $('.boatActionHold').mouseout(function () {
        clearInterval(timeout);
        return false;
    });
}

function updateStats(speed, placing, totalCompetitors, health){
    if (health > 0) {
        $("#boatSpeed").html(speed.toFixed(2) + "kn");
        $("#placing").html(placing + " / " + totalCompetitors);
        $("#boatHealth").html(health + "%");
    } else {
        loadInfoScreen("You are dead.");
    }
}

function initControls(teamName, color){
    $("#boatNameText").html(teamName);
    changeColor(color);
    $("#boatSpeed").html("0kn");
    $("#placing").html("-");
    $("#boatHealth").html("100%");
}

function loadControls(){
  $("#codeForm").fadeOut(1000);
    $("#controls").fadeIn(1000);
}

function loadInfoScreen(message){
    $("#infoScreenText").html(message);
    $("#controls").fadeOut(1000);
    $("#infoScreen").fadeIn(1000);
}

function changeColor(color){
    $("#controlsPage").css("background-color", color);
    $(".directionArrow").css("color", color);
    $("#infoScreen").css("background-color", color);
    $("body").css("background-color", color);
}

/**
 * Checks code of button pressed, sends corresponding boat action message
 * @param name
 */
function createBoatActionMessage(name){
    switch(name){
        case "vmg":
            sendBoatActionMessage(BOAT_ACTION.VMG.value, clientId);
            break;
        case "sails":
            sendBoatActionMessage(BOAT_ACTION.SAILS.value, clientId);
            break;
        case "tackGybe":
            sendBoatActionMessage(BOAT_ACTION.TACK_GYBE.value, clientId);
            break;
        case "antiClockwise":
            sendBoatActionMessage(BOAT_ACTION.ANTI_CLOCKWISE.value, clientId);
            break;
        case "clockwise":
            sendBoatActionMessage(BOAT_ACTION.CLOCKWISE.value, clientId);
            break;
        default:
            console.log("Unknown Button Pressed");
            break;
    }
};
