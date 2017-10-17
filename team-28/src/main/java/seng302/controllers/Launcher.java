package seng302.controllers;

import com.sun.javafx.application.LauncherImpl;

/**
 * Created by Devin on 16/05/17.
 *
 */
public class Launcher {
    public static void main( String[] args )
    {
        if (args.length > 0) {
            Main.main(args);
        } else {
            LauncherImpl.launchApplication(Main.class, SplashScreenLoader.class, args);
        }
    }
}
