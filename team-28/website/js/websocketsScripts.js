/**
 * Created by cba62 on 21/09/17.
 */

let gameRecorderSocket = null;
let gameServerSocket = null;
let clientId = null;

createGameRecorderSocket();

/**
 * Creates the header for the AC35 protocol messages
 * @param type The type of the message
 * @param messageLength The length of the payload
 */
createHeader = function (type, messageLength) {
    let header = new Uint8Array(HEADER_LENGTH);
    header[0] = AC35_SYNC_BYTE_1;
    header[1] = AC35_SYNC_BYTE_2;
    this.addIntIntoByteArray(header, HEADER_FIELDS.MESSAGE_TYPE.index, HEADER_FIELDS.MESSAGE_TYPE.length, type);
    this.addIntIntoByteArray(header, HEADER_FIELDS.MESSAGE_LENGTH.index, HEADER_FIELDS.MESSAGE_TYPE.length, messageLength);
    return header;
}


/**
 * Creates a WebSocket connection to the Game Recorder Server
 */
function createGameRecorderSocket() {
    gameRecorderSocket = new WebSocket("ws://132.181.16.17:2827"); // 2827 is the port game recorder runs on
    gameRecorderSocket.binaryType = 'arraybuffer';

    gameRecorderSocket.onerror = function (event) {
        console.log(event);
    }

    gameRecorderSocket.onopen = function () {
        console.log("Game Recorder connection established.");
    };

    gameRecorderSocket.onclose = function () {
        loadInfoScreen("No connection to server. Refresh to try again");
    }

    gameRecorderSocket.onmessage = function (event) {
        decodePacket(new Uint8Array(event.data));
    }
}

function sendRegistrationPacket() {
    let header = createHeader(MESSAGE_TYPE.REGISTRATION_REQUEST.type, MESSAGE_TYPE.REGISTRATION_REQUEST.length);
    let body = new Uint8Array(MESSAGE_TYPE.REGISTRATION_REQUEST.length);
    addIntIntoByteArray(body, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.index, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.length, 1);
    let crc = createCrc(header, body);
    let packet = concatUint8ByteArrays(concatUint8ByteArrays(header, body), crc);
    let byteArray = new Uint8Array(packet);
    gameServerSocket.send(byteArray.buffer);
}

/**
 * Creates a WebSocket connection to the Game Recorder Server
 */
function createGameServerSocket(/*String*/ip, port) {
    if (ip === "127.0.1.1") ip = "localhost";
    gameServerSocket = new WebSocket("ws://" + ip + ":" + port);
    gameServerSocket.binaryType = 'arraybuffer';

    gameServerSocket.onerror = function (event) {
        console.log(event);
    }

    gameServerSocket.onopen = function () {
        console.log("Game Server connection established.");
        sendRegistrationPacket();
    };

    gameServerSocket.onclose = function () {
        loadInfoScreen("Race has ended. Refresh to join a new race");
    }

    gameServerSocket.onmessage = function (event) {
        decodePacket(new Uint8Array(event.data));
    }
}

/**
 * Sends a request game packet to the Game Recorder
 * @param code The room code entered
 */
requestGame = function(code) {
    if(gameRecorderSocket == null){
        createGameRecorderSocket();
    }
    let header = createHeader(MESSAGE_TYPE.GAME_REQUEST.type, MESSAGE_TYPE.GAME_REQUEST.length);
    let body = new Uint8Array(2);
    addIntIntoByteArray(body, MESSAGE_FIELD.GAME_CODE.index, MESSAGE_FIELD.GAME_CODE.length, code);
    let crc = createCrc(header, body);
    let packet = concatUint8ByteArrays(concatUint8ByteArrays(header, body), crc);
    let byteArray = new Uint8Array(packet);
    gameRecorderSocket.send(byteArray.buffer);
}


/**
 * Sends a boat action message to the Game Recorder
 * @param actionCode id of boat action
 * @param boatId id of user's boat
 */
sendBoatActionMessage = function(actionCode, boatId){
    let header = createHeader(MESSAGE_TYPE.BOAT_ACTION_MESSAGE.type, MESSAGE_TYPE.BOAT_ACTION_MESSAGE.length);
    let body = [0, 0, 0, 0, 0];
    addIntIntoByteArray(body, MESSAGE_FIELD.BOAT_ACTION_SOURCE_ID.index, MESSAGE_FIELD.BOAT_ACTION_SOURCE_ID.length, boatId);
    addIntIntoByteArray(body, MESSAGE_FIELD.BOAT_ACTION_BODY.index, MESSAGE_FIELD.BOAT_ACTION_BODY.length, actionCode);
    let crc = createCrc(header, body);
    let packet = concatUint8ByteArrays(concatUint8ByteArrays(header, body), crc);
    let byteArray = new Uint8Array(packet);
    gameServerSocket.send(byteArray.buffer);
}



/**
 * Calculates and return the CRC over the header and body
 * @param header The header of the packet
 * @param body The body of the packet
 * @returns {[number,number,number,number]} The CRC of the packet
 */
createCrc = function(header, body){
    let crcLength = 4;
    let both = concatUint8ByteArrays(header, body);
    let crc = parseInt(crc32(both), 16);
    let array = [0, 0, 0, 0];
    addIntIntoByteArray(array, 0, crcLength, crc);
    return array;
}

function decodeRegistrationResponse(body) {
    let statusByte = body[MESSAGE_FIELD.REGISTRATION_STATUS.index];
    if (statusByte === 1) { //PLAYER SUCCESS
        clientId = byteArrayRangeToInt(body, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.index, MESSAGE_FIELD.REGISTRATION_SOURCE_ID.length);
        console.log("My Id: " + clientId);
    } else {
        if (statusByte === 11) { //OUT OF SLOTS
            showWrongGameCodeMessage("Apologies, the race is full. Cheer for your friend instead");

        } else {
            console.log("Registration failed.");
        }
    }
}

function decodeHostGameMessage(body) {
    let longIp = ToUint32(byteArrayRangeToInt(body, MESSAGE_FIELD.HOST_GAME_IP.index, MESSAGE_FIELD.HOST_GAME_PORT.length));
    let port = byteArrayRangeToInt(body, MESSAGE_FIELD.HOST_GAME_PORT.index, MESSAGE_FIELD.HOST_GAME_PORT.length);
    let isPartyMode = body[MESSAGE_FIELD.HOST_GAME_IS_PARTY_MODE.index];
    let ip = ipLongToString(longIp);
    console.log("Ip: " + ip + " Port: " + port);
    if(longIp === 0){
        showWrongGameCodeMessage("Incorrect Room Code Entered");
    } else if (isPartyMode === 1) {
        createGameServerSocket(ip, port);
    }
}


function decodeWebClientInit(body) {
    let nameEndIndex = MESSAGE_FIELD.WEB_CLIENT_NAME.index + MESSAGE_FIELD.WEB_CLIENT_NAME.length;
    let boatNameArray = body.subarray(MESSAGE_FIELD.WEB_CLIENT_NAME.index, nameEndIndex);
    let boatName = charArrayToString(boatNameArray);
    let boatColor = body.subarray(nameEndIndex, nameEndIndex + MESSAGE_FIELD.WEB_CLIENT_COLOUR.length);
    let colorString = rgbToHex(boatColor[0], boatColor[1], boatColor[2]);

    initControls(boatName, colorString);
    loadControls();
}

function decodeWebClientUpdate(body) {
    let boatSpeed = byteArrayRangeToInt(body, MESSAGE_FIELD.WEB_CLIENT_SPEED.index, MESSAGE_FIELD.WEB_CLIENT_SPEED.length);
    let speedInKnots = boatSpeed / 514.444;
    let placing = byteArrayRangeToInt(body, MESSAGE_FIELD.WEB_CLIENT_POSITION.index, MESSAGE_FIELD.WEB_CLIENT_POSITION.length);
    let totalCompetitors = byteArrayRangeToInt(body,MESSAGE_FIELD.WEB_CLIENT_TOTAL_COMPETITORS.index, MESSAGE_FIELD.WEB_CLIENT_TOTAL_COMPETITORS.length);
    let boatHealth = byteArrayRangeToInt(body, MESSAGE_FIELD.WEB_CLIENT_HEALTH.index, MESSAGE_FIELD.WEB_CLIENT_HEALTH.length);
    updateStats(speedInKnots, placing, totalCompetitors, boatHealth);
}

decodePacket = function(packet) {
    let header = packet.subarray(0, HEADER_LENGTH);
    let bodyLength = byteArrayRangeToInt(packet, HEADER_FIELDS.MESSAGE_LENGTH.index, HEADER_FIELDS.MESSAGE_LENGTH.length);
    let bodyEnd = HEADER_LENGTH + bodyLength;
    let body = packet.subarray(HEADER_LENGTH, bodyEnd);
    let crc = packet.subarray(bodyEnd);
    let messageType = byteArrayRangeToInt(header, HEADER_FIELDS.MESSAGE_TYPE.index, HEADER_FIELDS.MESSAGE_TYPE.length);
    if (checkCRC(header, body, crc)){
        switch (messageType) {
            case MESSAGE_TYPE.HOST_GAME_MESSAGE.type:
                decodeHostGameMessage(body);
                break;
            case MESSAGE_TYPE.REGISTRATION_RESPONSE.type:
                decodeRegistrationResponse(body);
                break;
            case MESSAGE_TYPE.WEB_CLIENT_INIT.type:
                decodeWebClientInit(body);
                break;
            case MESSAGE_TYPE.WEB_CLIENT_UPDATE.type:
                decodeWebClientUpdate(body);
                break;
            default:
                console.log("Message type: " + messageType);
        }
    } else {
        console.error("CRC check failed")
    }
}

checkCRC = function (header, body, crc) {
    let expectedCRC = createCrc(header, body);
    for (let i = 0; i < 4; i++){
        if (expectedCRC[i] !== crc[i]){
            return false;
        }
    }
    return true;
}
