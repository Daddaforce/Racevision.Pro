package seng302.utilities;

import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.*;

/**
 * Created by Chris on 28/07/2017.
 */

public class GameSounds {

    private HashMap hitMark = new HashMap();
    private HashMap hitBoat = new HashMap();
    private HashMap hitBoundary = new HashMap();
    private HashMap firstPlace = new HashMap();
    private HashMap everyoneButFirstPlace = new HashMap();
    private HashMap everyone = new HashMap();
    private HashMap lastPlace = new HashMap();
    private HashMap boatDamage = new HashMap();
    private HashMap preRace = new HashMap();

    private Clip clip;
    private AudioInputStream inputStream;
    private String selectedVoiceOver;
    private String selectedMusic;
    private Random random = new Random();
    private boolean endless = false;
    private MediaPlayer mediaPlayer;
    private int randomSeaGull;
    private double volume = 0.9;
    private boolean isMusicOn = true;
    private double fxVolume = 1.0;
    private double volumeThresholdReduction = 15;
    private FloatControl gainControl;
    private boolean playingSound = false;
    private boolean samFX = false;

    //Directories for voice overs
    private String chrisVoiceOver = "/musicFiles/gameVoiceOvers/ChrisVoiceOvers/";
    private String louisVoiceOver = "/musicFiles/gameVoiceOvers/LouisVoiceOvers/";
    private String samVoiceOver = "/musicFiles/gameVoiceOvers/SamVoiceOvers/";

    //Chris Voice overs
    private String chris_collided = chrisVoiceOver + "collided.mp3";
    private String chris_sail_ebration = chrisVoiceOver + "sail_ebration.mp3";
    private String chris_scream = chrisVoiceOver + "scream.mp3";

    //Louis Voice overs
    private String louis_buoy = louisVoiceOver + "buoy.mp3";
    private String louis_never_there = louisVoiceOver + "never_there.mp3";
    private String louis_sail_ebration = louisVoiceOver + "sail_ebration.mp3";
    private String louis_try_that = louisVoiceOver + "try_that.mp3";
    private String louis_water = louisVoiceOver + "water.mp3";

    //Sam Voice Overs
    private String sam_never_there = samVoiceOver + "never_there.mp3";
    private String sam_try_that = samVoiceOver + "try_that.mp3";

    /**
     * Sets up the initial game sounds into their own HashMaps for easy recall
     */
    public GameSounds() {

        //hitMark
        hitMark.put(1, louisVoiceOver + "over_that.mp3");
        hitMark.put(2, louisVoiceOver + "over_that_matey.mp3");
        hitMark.put(3, chrisVoiceOver + "holly_heck.mp3");
        hitMark.put(4, chrisVoiceOver + "maties.mp3");
        hitMark.put(5, samVoiceOver + "over_that.mp3");

        //hitBoat
        hitBoat.put(1, louisVoiceOver + "get_disqualified.mp3");
        hitBoat.put(2, louisVoiceOver + "board.mp3");
        hitBoat.put(3, samVoiceOver + "get_disqualified.mp3");
        hitBoat.put(4, samVoiceOver + "board.mp3");

        //hitBoundary
        hitBoundary.put(1, louisVoiceOver + "cant_leave.mp3");
        hitBoundary.put(2, louisVoiceOver + "be_shot.mp3");
        hitBoundary.put(3, samVoiceOver + "cant_leave.mp3");
        hitBoundary.put(4, samVoiceOver + "be_shot.mp3");

        //firstPlace
        firstPlace.put(1, louisVoiceOver + "pointless_race.mp3");
        firstPlace.put(2, samVoiceOver + "pointless_race.mp3");

        //everyoneButFirstPlace
        everyoneButFirstPlace.put(1, louisVoiceOver + "been_taken.mp3");
        everyoneButFirstPlace.put(2, samVoiceOver + "been_taken.mp3");
        everyoneButFirstPlace.put(3, chrisVoiceOver + "lost_this_race.mp3");

        //everyone
        everyone.put(1, louisVoiceOver + "youre_last.mp3");
        everyone.put(2, samVoiceOver + "youre_last.mp3");

        //lastPlace
        lastPlace.put(1, louisVoiceOver + "boat_time.mp3");
        lastPlace.put(2, chrisVoiceOver + "boat_time.mp3");

        //boatDamage
        boatDamage.put(1, louisVoiceOver + "rubber_dingy.mp3");
        boatDamage.put(2, samVoiceOver + "rubber_dingy.mp3");
        boatDamage.put(3, chrisVoiceOver + "rubber_dingy.mp3");
        boatDamage.put(4, louisVoiceOver + "live_by.mp3");
        boatDamage.put(5, samVoiceOver + "live_by.mp3");
        boatDamage.put(6, louisVoiceOver + "oh_ship.mp3");

        //preRace
        preRace.put(1,louisVoiceOver + "ready_race.mp3");
        preRace.put(2, chrisVoiceOver + "ready_race.mp3");

    }

    public void mainMenuMusic() {
        selectedMusic = "/musicFiles/gameMusic/MainMenuMusic.wav";
    }

    public void singlePlayerMusic() {
        selectedMusic = "/musicFiles/gameMusic/SinglePlayerMusic.wav";
    }

    public void tutorialMusic() {
        selectedMusic = "/musicFiles/gameMusic/TutorialMusic.wav";
    }

    public void oceanWaves() {
        selectedMusic = "/musicFiles/gameSounds/CrispOceanWaves.wav";
    }

    /**
     * Plays seagull sound at an interval between 30 to 60 seconds
     */
    public void flockSeagulls() {
        randomSeaGull = (int)(random.nextDouble() * 10000) + 20000; //10-30 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(endless) {
                    selectedVoiceOver = "/musicFiles/gameSounds/FlockOfSeagulls.mp3";
                    playGameSound();
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, randomSeaGull);
    }

    /**
     * Plays a random voiceover when a player has hit a mark
     */
    public void hitMark() {
        int randomNumber = (int)(random.nextDouble() * hitMark.size()) + 1; //1-5
        selectedVoiceOver = hitMark.get(randomNumber).toString();
        System.out.println(hitMark.size());
        if(randomNumber == 5) {
            samFX = true;
        }
    }


    /**
     * Plays a random voice over when a boat has hit another boat
     */
    public void hitBoat() {
        int randomNumber = (int)(random.nextDouble() * hitBoat.size()) + 1; //1-4
        selectedVoiceOver = hitBoat.get(randomNumber).toString();
        if(randomNumber == 4) {
            samFX = true;
        }
    }

    /**
     * Plays a random voice over when a boat hits the boundary
     */
    public void hitBoundary() {
        int randomNumber = (int)(random.nextDouble() * hitBoundary.size()) + 1; //1-4
        selectedVoiceOver = hitBoundary.get(randomNumber).toString();
        if(randomNumber == 3 || randomNumber == 4) {
            samFX = true;
        }
    }

    /**
     * Plays a random voice over when a boat comes in first
     */
    public void firstPlace() {
        int randomNumber = (int)(random.nextDouble() * firstPlace.size()) + 1; //1-2
        selectedVoiceOver = firstPlace.get(randomNumber).toString();
        if(randomNumber == 2) {
            samFX = true;
        }
    }

    /**
     * Plays a random voice over when a boat finishes the race
     */
    public void everyoneButFirstPlace() {
        int randomNumber = (int)(random.nextDouble() * everyoneButFirstPlace.size()) + 1; //1-3
        selectedVoiceOver = everyoneButFirstPlace.get(randomNumber).toString();
        if(randomNumber == 2) {
            samFX = true;
        }
    }

    public void everyone() {
        int randomNumber = (int)(random.nextDouble() * everyone.size()) + 1; //1-2
        selectedVoiceOver = everyone.get(randomNumber).toString();
        if(randomNumber == 2) {
            samFX = true;
        }
    }

    /**
     * Plays a random voice over when a boat finishes in last place
     */
    public void lastPlace() {
        int randomNumber = (int)(random.nextDouble() * lastPlace.size()) + 1; //1-2
        selectedVoiceOver = lastPlace.get(randomNumber).toString();
    }

    /**
     * Plays a random voice over when a boat loses health
     */
    public void boatDamage() {
        int randomNumber = (int)(random.nextDouble() * boatDamage.size()) + 1; //1-6
        selectedVoiceOver = boatDamage.get(randomNumber).toString();
        if(randomNumber == 2 || randomNumber == 5) {
            samFX = true;
        }
    }

    /**
     * Plays a random voiceover during the race prestart
     */
    public void preRace() {
        int randomNumber = (int)(random.nextDouble() * preRace.size()) + 1; //1-2
        selectedVoiceOver = preRace.get(randomNumber).toString();
    }

    public void playBuoySound() {
        selectedVoiceOver = "/musicFiles/gameSounds/BuoySound.mp3";
    }

    /**
     * Plays single instance sounds (e.g.voice overs)
     */
    public void playGameSound() {
        URL resource = getClass().getResource(selectedVoiceOver);
        mediaPlayer = new MediaPlayer(new Media(resource.toString()));
        if(samFX) {
            mediaPlayer.setVolume(fxVolume*1.1);
            samFX = false;
        } else {
            mediaPlayer.setVolume(fxVolume*0.9);
        }
        playingSound = true;
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                playingSound = false;
            }
        });
        mediaPlayer.play();
    }

    /**
     * Plays looping isMusicOn (e.g. lobby isMusicOn)
     * @throws IOException
     * @throws LineUnavailableException
     * @throws UnsupportedAudioFileException
     */
    public void playEndlessMusic() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        clip = AudioSystem.getClip();
        inputStream = AudioSystem.getAudioInputStream(DisplayUtils.class.getResource(selectedMusic));
        clip.open(inputStream);
        gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        setSoundTrackVolume();
        if(!isMusicOn) {
            BooleanControl muteControl = (BooleanControl) clip
                    .getControl(BooleanControl.Type.MUTE);
            muteControl.setValue(true);
        } else {
            BooleanControl muteControl = (BooleanControl) clip
                    .getControl(BooleanControl.Type.MUTE);
            muteControl.setValue(false);
        }
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void startSeaGullNoise() {
        endless = true;
        flockSeagulls();
    }

    public void stopEndlessMusic() throws LineUnavailableException {
        if (clip != null) {
            clip.stop();
            endless = false;
            clip.setFramePosition(0);
        }
    }

    public void setVolume(double volume) {
        this.volume = volume;
        setSoundTrackVolume();
    }

    private void setSoundTrackVolume(){
        if (System.getProperty("os.name").startsWith("Linux")) {
            volumeThresholdReduction = 0;
        }

        double range = gainControl.getMaximum() - gainControl.getMinimum() - volumeThresholdReduction;
        double gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue((float)gain);
    }

    public void setFxVolume(double volume){
        fxVolume = volume;
    }

    public void playFXSound() {
        if(!playingSound){
            firstPlace();
            playGameSound();
        }
    }

    public double getVolume(){return volume;}

    public double getFxVolume(){return fxVolume;}

}

