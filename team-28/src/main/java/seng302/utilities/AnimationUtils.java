package seng302.utilities;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import seng302.controllers.Controller;
import seng302.models.CanvasCoordinate;
import seng302.views.Arrow;

/**
 * Created by Devin on 25/07/17.
 */
public class AnimationUtils {

    private static boolean transitionFinished = true;

    /**
     * scales a collision node shape in and out to create explosion effect
     * @param node a circle representing the collision area
     * @param duration the length to run the explosion for
     * @param amount amount to scale explosion by
     * @return
     */
    public static ScaleTransition scaleTransitionCollision(Node node, int duration, double amount){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(duration), node);
        scaleTransition.setByY(amount);
        scaleTransition.setByX(amount);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        return scaleTransition;
    }

    /**
     * fades a node out of a scene
     * @param node the node to be faded out
     * @param duration the length of fade
     * @return
     */
    public static FadeTransition fadeOutTransition(Node node, int duration){
        FadeTransition fadeTransition = new FadeTransition(new Duration(duration), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        return fadeTransition;
    }

    /**
     * fades a node in our out from the screen
     * @param node the node to be faded
     * @param visible whether or not the node is currently visible
     */
    public static void fadeNode(Node node, boolean visible){
        node.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition(new Duration(150), node);
        if(visible){
            fadeTransition.setFromValue(node.getOpacity());
            fadeTransition.setToValue(0);
            fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }else{
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
        }
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }

    /**
     * scales a node up when mouse hovers over it
     * @param node the node to be scaled
     * @return the transition
     */
    public static ScaleTransition scaleButtonHover(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setByY(0.1);
        scaleTransition.setByX(0.1);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        return scaleTransition;
    }

    /**
     * scales a node back to defualt size once mouse exits
     * @param node the node to scale back
     * @return
     */
    public static ScaleTransition scaleButtonHoverExit(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        return scaleTransition;
    }

    /**
     * highlights an on-screen element by poping it out and in momentarily
     * @param node the element to be popped
     */
    public static void scalePop(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(100), node);
        scaleTransition.setByY(0.05);
        scaleTransition.setByX(0.05);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        scaleTransition.setOnFinished(event -> scaleButtonHoverExit(node));


    }

    /**
     * scales and fades a node to remove it from the scene
     * @param node the node to scale and fade
     */
    public static void slideOutTransition(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setByY(-1);
        scaleTransition.setByX(-1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(fadeTransition, scaleTransition);
        pt.play();
        fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){public void handle(ActionEvent AE){node.setVisible(false); }});
    }

    /**
     * scales and fades a node to remove it from the scene
     * @param node1 the node to fade out
     * @param node2 the node to fade in
     */
    public static void switchPaneFade(Node node1, Node node2){
        if(transitionFinished) {
            transitionFinished = false;
            FadeTransition fadeTransition1 = new FadeTransition(new Duration(300), node1);
            fadeTransition1.setFromValue(1);
            fadeTransition1.setToValue(0);
            fadeTransition1.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeTransition2 = new FadeTransition(new Duration(300), node2);
            fadeTransition2.setFromValue(0);
            fadeTransition2.setToValue(1);
            fadeTransition2.setInterpolator(Interpolator.EASE_OUT);
            fadeTransition1.setOnFinished(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent actionEvent) {
                    node1.setVisible(false);
                }
            });
            fadeTransition2.setOnFinished(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent actionEvent) {
                    transitionFinished = true;
                }
            });
            node2.setVisible(true);
            ParallelTransition pt = new ParallelTransition(fadeTransition1, fadeTransition2);
            pt.play();
        }
    }

    /**
     * scales and fades in a node to the scene
     * @param node the node to bring in
     */
    public static void slideInTransition(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(fadeTransition, scaleTransition);
        pt.play();
    }

    /**
     * shifts a label from a text field to above it for input
     * @param label the label to slide
     */
    public static void shiftPromptLabel(Label label, int direction){
        TranslateTransition translateTransition = new TranslateTransition(new Duration(80), label);
        translateTransition.setByX(18 * direction);
        translateTransition.setByY(26 * direction);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition scaleTransition = new ScaleTransition(new Duration(80), label);
        scaleTransition.setByX(0.15 * direction);
        scaleTransition.setByY(0.15 * direction);

        ParallelTransition pt = new ParallelTransition(translateTransition, scaleTransition);
        pt.play();

    }

    /**
     * fades in a new FXML
     * @param newScene the new scene to be faded in
     */
    public static void transitionFXML(Node newScene){
        FadeTransition ft2 = new FadeTransition(Duration.millis(1000), newScene);
        ft2.setFromValue(0.0);
        ft2.setToValue(1.0);
        ft2.play();
    }

    /**
     * lowers the opacity of a ndoe on the screen
     * @param node the node to be faded
     */
    public static void dullNode(Node node){
        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0.7);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }

    /**
     * sets a nodes opcaity to full
     * @param node the node to be made fully opaque
     */
    public static void focusNode(Node node){
        FadeTransition fadeTransition = new FadeTransition(new Duration(100), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }

    /**
     * shifts a node that is hidden when scoreboard visible
     * @param node the node to be shifted
     * @param amount the amount to shift the node by
     * @param visible whether or not the node is visible
     */
    public static void shiftPaneNodes(Node node, int amount, boolean visible){
        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setByX(amount);
        translateTransition.setInterpolator(Interpolator.EASE_IN);
        if(!visible){
            translateTransition.setOnFinished(AE -> node.setVisible(false));
        }
        translateTransition.play();
    }

    /**
     * shifts the side pane arrow in or out
     * @param node the arrow to be shifted
     * @param amount the amount ot shift it by
     * @param rotation the amount to rotate it by
     */
    public static void shiftPaneArrow(Node node, int amount, int rotation){
        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setByX(amount);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        RotateTransition rotateTransition = new RotateTransition(new Duration(200), node);
        rotateTransition.setByAngle(180 * rotation);
        rotateTransition.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition pt = new ParallelTransition(translateTransition, rotateTransition);
        pt.play();

    }

    /**
     *  shifts a hidden board node when panel toggled
     * @param node the node to be shifted
     * @param visible whether or not it is currently visible
     */
    public static void toggleHiddenBoardNodes(Node node, boolean visible, double endOpacity){
        FadeTransition fadeTransition = new FadeTransition(new Duration(200), node);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        if(visible){
            translateTransition.setFromY(node.getLayoutY());
            translateTransition.setToY(node.getLayoutY() - 10);
            fadeTransition.setFromValue(endOpacity);
            fadeTransition.setToValue(0);
            fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }else{
            node.setVisible(true);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(endOpacity);
            translateTransition.setFromY(node.getLayoutY() - 10);
            translateTransition.setToY(node.getLayoutY());
        }
        ParallelTransition pt = new ParallelTransition(translateTransition, fadeTransition);
        pt.play();
    }

    /**
     * mades a node grow and shrink to draw attention to it
     * @param node the node to grow and shrink
     */
    public static void drawAttentionToNode(Node node){
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setByX(1.2);
        st.setByY(1.2);
        st.setAutoReverse(true);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.setCycleCount(2);
        st.play();
    }

    /**
     * sets a nodes opcaity to the desried opacity
     * @param node the node to be made fully opaque
     * @param endOpacity the end opacity
     */
    public static void fadeNodeCustom(Node node, double endOpacity){
        FadeTransition fadeTransition = new FadeTransition(new Duration(200), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(endOpacity);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }


    /**
     * fades a course arrow in or out
     * @param node the node to be faded
     * @param visible whether or not the node is currently visible
     */
    public static FadeTransition changeCourseRouteNode(Node node, boolean visible){
        node.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition(new Duration(150), node);
        if(visible){
            fadeTransition.setFromValue(node.getOpacity());
            fadeTransition.setToValue(0);
        }else{
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
        }
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        return fadeTransition;
    }

    /**
     *  toggles a quick menu title in and out
     * @param node the node to be shifted
     * @param visible whether or not it is currently visible
     */
    public static void toggleQuickMenuNodes(Node node, boolean visible){
        FadeTransition fadeTransition = new FadeTransition(new Duration(200), node);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition translateTransition = new TranslateTransition(new Duration(200), node);
        translateTransition.setInterpolator(Interpolator.EASE_IN);

        if(visible){
            translateTransition.setFromX(node.getLayoutX());
            translateTransition.setToX(node.getLayoutX() + 5);
            fadeTransition.setFromValue(0.8);
            fadeTransition.setToValue(0);
            fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }else{
            node.setVisible(true);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(0.8);
            translateTransition.setFromX(node.getLayoutX() + 5);
            translateTransition.setToX(node.getLayoutX());
        }
        ParallelTransition pt = new ParallelTransition(translateTransition, fadeTransition);
        pt.play();
    }

    /**
     * animates a portion of the swipe trail
     * @param pane the pane on which the swipe is added to and removed from
     * @param node the portion of swipe trail
     */
    public static void swipeAnimation(Pane pane, Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setByX(-0.5);
        scaleTransition.setByY(-0.5);
        FadeTransition fadeTransition = new FadeTransition(new Duration(200), node);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(0);

        ParallelTransition pt = new ParallelTransition(scaleTransition, fadeTransition);
        pane.getChildren().add(node);
        pt.play();

        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pane.getChildren().remove(node);
            }
        });
    }


    /**
     * changes the colour of the stroke of a node to
     * @param node the node to change
     * @param color the colour of the new stroke
     */
    public static void changeStrokeColor(Shape node, Color color){
        StrokeTransition strokeTransition = new StrokeTransition();
        strokeTransition.setShape(node);
        strokeTransition.setToValue(color);
        strokeTransition.setDuration(new Duration(200));
        strokeTransition.play();
    }

    /**
     * changes the fill colour of a node
     * @param node the node to change the fill colour of
     * @param color the end colour to change to
     */
    public static void changeFillColor(Shape node, Color color){
        FillTransition fillTransition = new FillTransition();
        fillTransition.setShape(node);
        fillTransition.setToValue(color);
        fillTransition.setDuration(new Duration(200));
        fillTransition.play();
    }

    /**
     * adds an infinite hover effect to a node
     * @param node the node that the transition is applied to
     */
    public static void nodeHover(Node node) {
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(1700), node);
        scaleTransition.setByX(0.04);
        scaleTransition.setByY(0.04);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        scaleTransition.setCycleCount(Animation.INDEFINITE);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }

    public static void slideUpNode(Node node){
        TranslateTransition translateTransition = new TranslateTransition(new Duration(300), node);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        translateTransition.setByY(60);
        translateTransition.setByX(200);

        ScaleTransition scaleTransition = new ScaleTransition(new Duration(300), node);
        scaleTransition.setByX(-0.1);
        scaleTransition.setByY(-0.1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(translateTransition, scaleTransition);
        pt.play();
    }
/**
     * fades in a menu pane
     * @param node the pane to be faded
     */
    public static void fadeMenuPane(Node node){
        node.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition(new Duration(300), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();}

    /**
     * sets a nodes opcaity to the desried opacity
     * @param node the node to be made fully opaque
     * @param endOpacity the end opacity
     */
    public static void removeMenuButton(Node node, double endOpacity){
        FadeTransition fadeTransition = new FadeTransition(new Duration(200), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(endOpacity);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        if(endOpacity == 0){
            fadeTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }else{
            node.setVisible(true);
        }
        fadeTransition.play();
    }

    /**
     * scales a pan up from the bottom as it opens
     */
    public static void openScaleOpenPane(Node node, boolean closed){
        ScaleTransition scaleTransition = new ScaleTransition( new Duration(200), node);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        if(closed){
            scaleTransition.setFromX(0);
            scaleTransition.setFromY(0);
            scaleTransition.setToX(327);
            scaleTransition.setToY(400);
            node.setVisible(true);
        }else{scaleTransition.setFromX(327);
            scaleTransition.setFromY(400);
            scaleTransition.setToX(0);
            scaleTransition.setToY(0);

            scaleTransition.setOnFinished(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent AE){
                    node.setVisible(false);
                }});
        }
        scaleTransition.play();
    }
}
