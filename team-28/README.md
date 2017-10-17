<h1>Objective Sea</h1>
 
 
<h2>Starting the Game</h2>
On opening Objective Sea, players may choose to play in either Single Player, Practice or Live Game (multiplayer) mode. <br>
To play a live game, players must currently either enter a port and click the 'Host' button to start a server, or enter the IP and port of an already running server. When hosting, you will be able to select which course you would like to race on.
<br>
Players may either click 'Join' to control a boat, or 'Spectate' to simply watch other players in the game. When hosting, you will always be controlling the first boat in the race.

Live Game:

    On selecting live game, the user is allowed to choose to host, join or spectate a game.
    
    Host: User needs to enter a port number (e.g 2828).  The user then has the option of 6 maps,
    each map offers a unique racing experience.  Once selected the user enters the game.
    
    Join: User must enter a valid IP address (that of a host) and the corresponding port.
    Once selected, the user enters a game with other players (at least one) and begins the game.
    
    Spectate: User must enter a valid IP address (that of a host) and the corresponding port.
    Once selected, the user enters a game with other players (at least one) and watches the game.

Practice:

    On selecting practice the user is allowed to choose to practice start or single play.
    
    Practice start: The user enters a starting practice game, with only a start line and
    the goal is to start on time.  Pop ups are available to inform user of their progress
    
    Single Play: the user enters a map selection, with 6 courses to choose from.  The courses
    are each unique.  On entering a course, the user has the option to race against a computer
    simulated boat (easy or hard level).

Tutorial:

    Tutorial mode takes the user instantly into a game screen (no maps or marks) and teaches the
    user the keys one by one, providing valuable feedback via the pop-ups.

Party Mode:
    
    When Party Mode is selected, a room code will be displayed. Head to racevision.pro on your devices
    and enter the code displayed to join the game! You will be able to control a boat from the website!
    
<br>
Once connected as a player, one is assigned a boat (ID, colour and country).  This is the boat that the connected player can control.
The given boat is shown via a circular highlight, allowing for a player to easily identify themselves.
<br>
<h2>Gameplay</h2>
To control a boat refer to the below key.  A boat, will have a speed based on it's current heading, wind speed and wind direction.
Wind speed is shown via the coloured and moving wind arrow, depicted in the top right hand corner of the course screen.
<br>
<h3>Keys:</h3> 

<ul>
    <br>   
   <li><strong>PgUp:</strong> Upwind Key - The heading of the boat it slightly adjusted towards upwind</li>
   <li><strong>PgDn:</strong> Downwind Key - The heading of the boat it slightly adjusted towards downwind</li>
   <li><strong>Shift: </strong>Sails in/out Key - toggles the sails in or out depending on sails current status.  Sails in will result in luffing of the sail
    and the boat will slow to a gradule halt.  Conversely, moving your sails out into a powered up position will result in a sail which
    is catching all avaliable wind and the boat will now travel at maximum speed (gradually increase to it if not at it already)</li>
   <li><strong>Enter:</strong> Tack/gybe Key - The boat will tack or gybe based on boats current heading.  If the boat's heading is in the "dead zone"
    (a zone in which the boat's heading is within the range of optimum TWA already) then this key will have no effect on the 
     boats current heading.  Else the boat will either tack or gybe to the optimum angles.</li>
   <li><strong>SpaceBar:</strong> VMG key - This key is an autopilot key, which will take the boat to the optimum VMG angle a automatically.</li>

   <li><strong>Control:</strong> Multiple selection - Holding down control allows for a user to select multiple boats. This can be
     used to toggle additional annotations such as the distance between two boats.</li>
   </ul>

<h3>Touch Controls:</h3>
If a user has a touch capable device they are able to use the following gestures to control their boat.

<ul>
    <br>   
   <li><strong>Touch:</strong> The boat's heading will move in the direction of the user's finger.</li>
   <li><strong>Swipe across:</strong> User can swipe perpendicularly to the boat to tack and gybe.</li>
   <li><strong>Swipe up/down:</strong> User can swipe parallel to the boat to put the boats sails up or down.</li>
</ul>
<br>

<strong>Collisions:</strong>  When a players boat collides with a mark or other boat, they are visually identified of this via a flashing red circle
appearing around their boat.  Collisions also incur a penalty (based on what was hit and who hit what) and a damage to the overall
boats health.
<br>
<strong>Boat Health:</strong> A boats health affects the overall maximum speed of a boat, if a boat has a health of  0, they are effectively disqualified
with an incurring penalty of a max speed of zero.
<br>
<strong>Sound:</strong> Sound settings can be found on the main menu in the bottom left of the screen. Music and SoundFX can be toggled using sliders and mute buttons.
<br>


<h2>Running a Server Without UI</h2>

The command to run the jar as a headless server is as follows:<br>
<code>java -jar [jar location] server [options...]</code>
<br><br>

<strong>Server Options</strong>
<ul>
<li><strong>-p [PORT_NUMBER]</strong> - starts the server on the specified port number. Defaults to 2828.</li>
<li><strong>-n [MIN_PARTICIPANTS]</strong> - specifies the minimum number of clients the server will wait for before beginning the race. Defaults to 1.</li>
<li><strong>-s [SPEED_SCALE]</strong> - scales the speed the race runs at by the specified factor. Defaults to 1, which gives realistic sailing speeds.</li>
<li><strong>-m [RACE_XML]</strong> - sets the map to use by specifying a race xml. Defaults to 'Race.xml'</li>
<li><strong>-r [NUM_RACES]</strong> - sets the number of times the server will restart after a race has been completed, so that a new race can be run. A value of -1 will cause the server to restart infinitely until the process is killed, this is the default.</li>
</ul>
<br>
Example usage:
<code>java -jar target/app-0.0.jar server -p 4941 -n 2 -s 30 -m Race.xml -r 3</code>


<h2>Running a Game Recorder Server UI</h2>

A Game Recorder server will keep track of currently available games and inform clients of them.
There is a GAME_RECORDER_IP and PORT in ConnectionUtils which should point to a running Game Recorder<br>
The game can be used normally if there is no Game Recorder running, however you won't be able to see any other games on the join game screen<br>

The command to run the jar as a game recorder server is as follows:<br>
<code>java -jar [jar location] server -g</code>
<br><br>
