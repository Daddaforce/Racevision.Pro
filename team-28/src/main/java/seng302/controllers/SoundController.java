package seng302.controllers;

import seng302.data.BoatStatus;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.DisplaySwitcher;

import java.util.Iterator;

/**
 * Created by cjd137 on 16/08/17.
 *
 */
public class SoundController implements Runnable {

    private boolean finalGameSound = false;
    private boolean firstGameSound = true;
    private boolean firstPlaceTaken = false;
    private boolean lastPlace = false;
    private int clientID;
    private Race race = GameClient.getRace();
    private int noOfBoats = race.getCompetitors().size();
    private boolean running;

    public SoundController(int clientID) {
        this.clientID = clientID;
    }


    /**
     * Plays sounds based on boat placing. If boat has won, first place sound is played.
     * If boat has lost, last place sound is played. If boat is finished, and not last or first a sound is played
     */
    public void checkPlacing(Boat boat) {
        if (!finalGameSound && boat.getId().equals(clientID)) {
            if (boat.getCurrPlacing() == 1 && boat.getStatus().equals(BoatStatus.FINISHED) && !firstPlaceTaken) {
                DisplaySwitcher.getGameSounds().firstPlace();
                DisplaySwitcher.getGameSounds().playGameSound();
                finalGameSound = true;
            } else if (boat.getCurrPlacing() != 1 && boat.getCurrPlacing() != noOfBoats - 1 &&
                    boat.getStatus().equals(BoatStatus.FINISHED) && firstPlaceTaken) {
                DisplaySwitcher.getGameSounds().everyoneButFirstPlace();
                DisplaySwitcher.getGameSounds().playGameSound();
                finalGameSound = true;
            }
            if ((boat.getCurrPlacing() == noOfBoats && boat.getStatus().equals(BoatStatus.FINISHED)) && firstPlaceTaken ||
                    (boat.getStatus().equals(BoatStatus.DISQUALIFIED) || boat.getStatus().equals(BoatStatus.DNF)) && !lastPlace) {
                DisplaySwitcher.getGameSounds().lastPlace();
                DisplaySwitcher.getGameSounds().playGameSound();
                lastPlace = true;
            }
        }
        if (!firstPlaceTaken && boat.getStatus().equals(BoatStatus.FINISHED)) {
            firstPlaceTaken = true;
        }
    }

    /**
     * Plays prerace game sound
     */
    public void initialReadySound() {
        if(firstGameSound) {
            DisplaySwitcher.getGameSounds().preRace();
            DisplaySwitcher.getGameSounds().playGameSound();
            firstGameSound = false;
        }
    }

    /**
     * When boat hits another boat, boat damage sound and hitting boat sounds are played
     * @param boat boat that has been hit
     */
    public void hasHitBoat(Boat boat) {
        if(boat.getId().equals(clientID)) {
            if ((System.currentTimeMillis() - boat.getTimeSinceLastCollision() > 5000) && boat.isBoatCollideSound()) {
                if (boat.getBoatHealth() <= 20) {
                    DisplaySwitcher.getGameSounds().boatDamage();
                    DisplaySwitcher.getGameSounds().playGameSound();
                } else {
                    DisplaySwitcher.getGameSounds().hitBoat();
                    DisplaySwitcher.getGameSounds().playGameSound();
                }
                boat.setBoatCollideSound(false);
                boat.setTimeSinceLastCollision(System.currentTimeMillis());
            }
        }
    }

    /**
     * When boat hits a mark, boat damage sound and hitting a mark sound plays
     * @param boat boat that has hit a mark
     */
    public void hasHitMark(Boat boat) {
        if(boat.getId().equals(clientID)) {
            if ((System.currentTimeMillis() - boat.getTimeSinceLastCollision() > 5000) && boat.isMarkCollideSound()) {
                if (boat.getBoatHealth() <= 20) {
                    DisplaySwitcher.getGameSounds().boatDamage();
                    DisplaySwitcher.getGameSounds().playGameSound();
                } else {
                    DisplaySwitcher.getGameSounds().playBuoySound();
                    DisplaySwitcher.getGameSounds().playGameSound();
                    DisplaySwitcher.getGameSounds().hitMark();
                    DisplaySwitcher.getGameSounds().playGameSound();
                }
                boat.setMarkCollideSound(false);
                boat.setTimeSinceLastCollision(System.currentTimeMillis());
            }
        }
    }

    /**
     * When a boat is out of bounds, boat damage sound and boundary sound plays
     * @param boat boat that is out of bounds
     */
    private void isOutOfBounds(Boat boat){
        if(boat.getId().equals(clientID)) {
            if ((System.currentTimeMillis() - boat.getTimeSinceLastCollision() > 5000) && boat.isOutOfBoundsSound()) {
                if (boat.getBoatHealth() <= 20) {
                    DisplaySwitcher.getGameSounds().boatDamage();
                    DisplaySwitcher.getGameSounds().playGameSound();
                } else {
                    DisplaySwitcher.getGameSounds().hitBoundary();
                    DisplaySwitcher.getGameSounds().playGameSound();
                }
                boat.setOutOfBoundsSound(false);
                boat.setTimeSinceLastCollision(System.currentTimeMillis());
            }
        }
    }

    @Override
    public void run() {
        while(running) {
            Iterator<Boat> iter = race.getCompetitors().iterator();
            while (iter.hasNext()) {
                Boat boat = iter.next();
                initialReadySound();
                if (boat.getStatus() != BoatStatus.DNF && boat.getStatus() != BoatStatus.DISQUALIFIED) {
                    checkPlacing(boat);
                    hasHitBoat(boat);
                    hasHitMark(boat);
                    isOutOfBounds(boat);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
