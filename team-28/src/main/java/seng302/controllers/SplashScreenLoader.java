package seng302.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Preloader;
import javafx.scene.paint.Color;

import java.io.IOException;


public class SplashScreenLoader extends Preloader {

    private Stage splashScreen;

    @Override
    public void start(Stage stage) throws Exception {
        splashScreen = stage;
        splashScreen.initStyle(StageStyle.UNDECORATED);
        Scene scene = createScene();
        splashScreen.setScene(scene);
        splashScreen.show();
    }

    public Scene createScene() throws IOException{
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("splash_screen.fxml"));
        return new Scene(parent);
    }

    @Override
    public void handleApplicationNotification(Preloader.PreloaderNotification notification) {
        if (notification instanceof Preloader.StateChangeNotification) {
            splashScreen.hide();
        }
    }
}
