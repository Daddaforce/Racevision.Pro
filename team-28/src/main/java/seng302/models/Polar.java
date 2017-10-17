package seng302.models;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Created by Gemma on 10/05/17.
 * Class to hold the boat readPolars neatly
 */
public class Polar {

    private int TWS;
    private WindAngleAndSpeed upWindOptimum = new WindAngleAndSpeed(0.0,0.0);
    private WindAngleAndSpeed downWindOptimum = new WindAngleAndSpeed(0.0,0.0);
    private ArrayList<WindAngleAndSpeed> TWAandBSP = new ArrayList<>();

    public Polar(int TWS) {
        this.TWS = TWS;
    }

    public void addTWAandBSP(Double TWA, Double BSP){
        WindAngleAndSpeed angleAndSpeed = new WindAngleAndSpeed(TWA,BSP);
        TWAandBSP.add(angleAndSpeed);
    }


    public int getTWS() {
        return TWS;
    }

    public WindAngleAndSpeed getUpWindOptimum() {
        return upWindOptimum;
    }

    public WindAngleAndSpeed getDownWindOptimum() {
        return downWindOptimum;
    }

    public ArrayList<WindAngleAndSpeed> getTWAandBSP() {
        return TWAandBSP;
    }

    /**
     * A setter for the optimum upwind TWA and BSP of the boats, not all readPolars will have this
     * @param upWindOptimum
     */
    public void setUpWindOptimum(WindAngleAndSpeed upWindOptimum) {
        this.upWindOptimum = upWindOptimum;
    }

    /**
     * A setter for the optimum downiwnd TWA and BSP of the boats, not all readPolars will have this
     * @param downWindOptimum
     */
    public void setDownWindOptimum(WindAngleAndSpeed downWindOptimum) {
        this.downWindOptimum = downWindOptimum;
    }

    /**
     * Checks if the supplied boat polar already gave optimum (in this case no interpolaiton needed)
     * @return true if TWA and Bsp supplied
     */
    public Boolean hasUpwindOptimum(){
        if(upWindOptimum.getWindAngle() != 0 && upWindOptimum.getSpeed() != 0){
            return true;
        }
        return false;
    }

    /**
     * Checks if the supplied boat polar already gave optimum (in this case no interpolaiton needed)
     * @return true if TWA and Bsp supplied
     */
    public Boolean hasDownwindOptimum(){
        if(downWindOptimum.getWindAngle() != 0 && downWindOptimum.getSpeed() != 0){
            return true;
        }
        return false;
    }
}
