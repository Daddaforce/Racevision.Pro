/**
 * Created by mjt169 on 28/09/17.
 */

let AC35_SYNC_BYTE_1 = 71;
let AC35_SYNC_BYTE_2 = 131;
let HEADER_LENGTH = 15;

let HEADER_FIELDS = {
    MESSAGE_TYPE: {index: 2, length: 1},
    MESSAGE_LENGTH : {index: 13, length:2},
};

let MESSAGE_TYPE = {
    GAME_REQUEST: {type:114, length:2},
    REGISTRATION_REQUEST:  {type:55, length:4},
    REGISTRATION_RESPONSE: {type:56, length:5},
    HOST_GAME_MESSAGE: {type:108, length:14},
    BOAT_ACTION_MESSAGE:{type:100, length:5},
    WEB_CLIENT_INIT:{type:120, length:37},
    WEB_CLIENT_UPDATE:{type:121, length:9}
};

let MESSAGE_FIELD = {
    GAME_CODE: {index:0, length:2},
    REGISTRATION_SOURCE_ID: {index:0, length:4},
    REGISTRATION_STATUS: {index:4, length:1},
    HOST_GAME_IP: {index:0, length:4},
    HOST_GAME_PORT: {index:4, length:4},
    HOST_GAME_IS_PARTY_MODE: {index:13, length:1},
    BOAT_ACTION_SOURCE_ID:{index:0, length:4},
    BOAT_ACTION_BODY:{index:4, length:1},
    WEB_CLIENT_ID:{index:0, length:4},
    WEB_CLIENT_NAME:{index:4, length:30},
    WEB_CLIENT_COLOUR:{index:34, length:3},
    WEB_CLIENT_SPEED:{index:4, length:2},
    WEB_CLIENT_POSITION:{index:6, length:1},
    WEB_CLIENT_TOTAL_COMPETITORS:{index:7, length:1},
    WEB_CLIENT_HEALTH:{index:8, length:1}
};