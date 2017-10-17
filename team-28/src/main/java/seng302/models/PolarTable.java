package seng302.models;

import seng302.utilities.MathUtils;

import java.util.ArrayList;

/**
 * Created by mjt169 on 15/05/17.
 *
 */
public class PolarTable {

    private double tackVMGofBoat;
    private double tackTWAofBoat;
    private double gybeVMGofBoat;
    private double gybeTWAofBoat;
    private ArrayList<Polar> polars;

    public PolarTable(ArrayList<Polar> polars, Course course) {
        this.polars = polars;
        //TODO rerun these calculations when the course's wind speed is updated
        WindAngleAndSpeed tackingInfo = calculateOptimumTack((int)course.getTrueWindSpeed());
        WindAngleAndSpeed gybingInfo = calculateOptimumGybe((int)course.getTrueWindSpeed());
        gybeVMGofBoat = gybingInfo.getWindAngle();
        gybeTWAofBoat = gybingInfo.getSpeed();
        tackVMGofBoat = tackingInfo.getWindAngle();
        tackTWAofBoat = tackingInfo.getSpeed();
    }

    /**
     * Finds the 3 values closest to the tws.
     * @param TWS Given true wind speed that is used in the calculations
     * @param polars polar table for a specific boat
     * @return arrayList of polars that are closest to TWS
     */
    public ArrayList<Polar> TWSForInterp(int TWS, ArrayList<Polar> polars){
        ArrayList<Polar> interpPolars = new ArrayList<>();
        int index = 0;
        double TWSDiff = Double.POSITIVE_INFINITY;

        for(int i = 0; i < polars.size(); i++){
            if(Math.abs(TWS - polars.get(i).getTWS()) < TWSDiff){
                index = i;
                TWSDiff = Math.abs(TWS - polars.get(i).getTWS());
            }
        }
        if(index == 0){index ++;}
        if(index == polars.size() - 1){index -= 1;}
        Polar polar1 = polars.get(index - 1);
        Polar polar2 = polars.get(index);
        Polar polar3 = polars.get(index + 1);
        interpPolars.add(polar1);
        interpPolars.add(polar2);
        interpPolars.add(polar3);
        return interpPolars;
    }

    /**
     * Finds the true wind angle and returns a list of 3 polars that are closest to this value
     * @param TWA true wind angle
     * @param polar polar table for boat
     * @return arraylist WindAngleAndSpeed
     */
    public ArrayList<WindAngleAndSpeed> TWAForInterp(int TWA, Polar polar){
        ArrayList<WindAngleAndSpeed> interpPolars = new ArrayList<>();
        ArrayList<WindAngleAndSpeed> windAngleAndSpeeds = polar.getTWAandBSP();
        int index = 0;
        double TWADiff = Double.POSITIVE_INFINITY;

        for(int i = 0; i < windAngleAndSpeeds.size(); i++){
            if(Math.abs(TWA - windAngleAndSpeeds.get(i).getWindAngle()) < TWADiff){
                index = i;
                TWADiff = Math.abs(TWA - windAngleAndSpeeds.get(i).getWindAngle());
            }
        }
        if(index == 0){index ++;}
        if(index == windAngleAndSpeeds.size() - 1){index -= 1;}
        WindAngleAndSpeed windAngleAndSpeed1 = windAngleAndSpeeds.get(index - 1);
        WindAngleAndSpeed windAngleAndSpeed2 = windAngleAndSpeeds.get(index);
        WindAngleAndSpeed windAngleAndSpeed3 = windAngleAndSpeeds.get(index + 1);
        interpPolars.add(windAngleAndSpeed1);
        interpPolars.add(windAngleAndSpeed2);
        interpPolars.add(windAngleAndSpeed3);
        return interpPolars;
    }


    /**
     * This function calculates the optimum calculateOptimumGybe angle and speed based on a polar table
     * @param TWS true wind speed
     * @return the TWA and VMG optimum for given boat
     */
    public WindAngleAndSpeed calculateOptimumGybe(int TWS){
        WindAngleAndSpeed boatsGybe;
        ArrayList<Polar> interpPolars = TWSForInterp(TWS, polars);

        Polar polar1 = interpPolars.get(0);
        Polar polar2 = interpPolars.get(1);
        Polar polar3 = interpPolars.get(2);

        double TWS1 = polar1.getTWS();
        double TWS2 = polar2.getTWS();
        double TWS3 = polar3.getTWS();

        double TWA1 = 0;
        double TWA2 = 0;
        double TWA3 = 0;

        double gybeVMG1 = 1000000;
        double gybeVMG2 = 1000000;
        double gybeVMG3 = 1000000;

        WindAngleAndSpeed pair4;
        WindAngleAndSpeed pair5;
        WindAngleAndSpeed pair6;

        double TrueVMG;

        if(polar2.hasDownwindOptimum()){
            WindAngleAndSpeed dnWind1 = polar1.getDownWindOptimum();
            WindAngleAndSpeed dnWind2 = polar2.getDownWindOptimum();
            WindAngleAndSpeed dnWind3 = polar3.getDownWindOptimum();
            gybeVMG1 = MathUtils.VMG(dnWind1.getSpeed(), dnWind1.getWindAngle());
            gybeVMG2 = MathUtils.VMG(dnWind2.getSpeed(), dnWind2.getWindAngle());
            gybeVMG3 = MathUtils.VMG(dnWind3.getSpeed(), dnWind3.getWindAngle());

            TWA1 = dnWind1.getWindAngle();
            TWA2 = dnWind2.getWindAngle();
            TWA3 = dnWind3.getWindAngle();

        } else {
            for(double k = 90; k < 181; k++){
                double BSP1 = MathUtils.lagrangeInterpolation(polar1.getTWAandBSP().get(5), polar1.getTWAandBSP().get(6), polar1.getTWAandBSP().get(7), k);
                if(MathUtils.VMG(BSP1, k) < gybeVMG1){gybeVMG1 = MathUtils.VMG(BSP1, k); TWA1 = k;}
                double BSP2 = MathUtils.lagrangeInterpolation(polar2.getTWAandBSP().get(5), polar2.getTWAandBSP().get(6), polar2.getTWAandBSP().get(7), k);
                if(MathUtils.VMG(BSP2, k) < gybeVMG2){gybeVMG2 = MathUtils.VMG(BSP2, k); TWA2 = k;}
                double BSP3 = MathUtils.lagrangeInterpolation(polar3.getTWAandBSP().get(5), polar3.getTWAandBSP().get(6), polar3.getTWAandBSP().get(7), k);
                if(MathUtils.VMG(BSP3, k) < gybeVMG3){gybeVMG3 = MathUtils.VMG(BSP3, k);TWA3 = k;}

                //interpolate back to get TWA based on found VMG

            } }
        pair4 = new WindAngleAndSpeed(gybeVMG1, TWA1);
        pair5 = new WindAngleAndSpeed(gybeVMG2, TWA2);
        pair6 = new WindAngleAndSpeed(gybeVMG3, TWA3);
        TrueVMG = MathUtils.lagrangeInterpolation(new WindAngleAndSpeed(TWS1, gybeVMG1),new WindAngleAndSpeed(TWS2, gybeVMG2), new WindAngleAndSpeed(TWS3, gybeVMG3), TWS);
        double TWA = MathUtils.lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
        boatsGybe = new WindAngleAndSpeed(TrueVMG, TWA);
        return boatsGybe;
    }

    /**
     * This function calculates the optimum tacking angle and speed based on a polar table
     * @param TWS true wind speed
     * @return the TWA and VMG optimum for given boat
     */
    public WindAngleAndSpeed calculateOptimumTack(int TWS){
        WindAngleAndSpeed boatsTack;
        ArrayList<Polar> interpPolars = TWSForInterp(TWS, polars);

        Polar polar1 = interpPolars.get(0);
        Polar polar2 = interpPolars.get(1);
        Polar polar3 = interpPolars.get(2);

        double TWS1 = polar1.getTWS();
        double TWS2 = polar2.getTWS();
        double TWS3 = polar3.getTWS();

        double TWA1 = 0;
        double TWA2 = 0;
        double TWA3 = 0;

        double VMG1 = 0;
        double VMG2 = 0;
        double VMG3 = 0;

        WindAngleAndSpeed pair4;
        WindAngleAndSpeed pair5;
        WindAngleAndSpeed pair6;

        double TrueVMG;


        if(polar2.hasUpwindOptimum()){
            WindAngleAndSpeed upWind1 = polar1.getUpWindOptimum();
            WindAngleAndSpeed upWind2 = polar2.getUpWindOptimum();
            WindAngleAndSpeed upWind3 = polar3.getUpWindOptimum();
            VMG1 = MathUtils.VMG(upWind1.getSpeed(), upWind1.getWindAngle());
            VMG2 = MathUtils.VMG(upWind2.getSpeed(), upWind2.getWindAngle());
            VMG3 = MathUtils.VMG(upWind3.getSpeed(), upWind3.getWindAngle());
            TWA1 = upWind1.getWindAngle();
            TWA2 = upWind2.getWindAngle();
            TWA3 = upWind3.getWindAngle();

        } else {
            for(double k = 0; k < 91; k++){
                double BSP1 = MathUtils.lagrangeInterpolation(polar1.getTWAandBSP().get(1), polar1.getTWAandBSP().get(2), polar1.getTWAandBSP().get(3), k);
                if(MathUtils.VMG(BSP1, k) > VMG1){VMG1 = MathUtils.VMG(BSP1, k); TWA1 = k;}
                double BSP2 = MathUtils.lagrangeInterpolation(polar2.getTWAandBSP().get(1), polar2.getTWAandBSP().get(2), polar2.getTWAandBSP().get(3), k);
                if(MathUtils.VMG(BSP2, k) > VMG2){VMG2 = MathUtils.VMG(BSP2, k); TWA2 = k;}
                double BSP3 = MathUtils.lagrangeInterpolation(polar3.getTWAandBSP().get(1), polar3.getTWAandBSP().get(2), polar3.getTWAandBSP().get(3), k);
                if(MathUtils.VMG(BSP3, k) > VMG3){VMG3 = MathUtils.VMG(BSP3, k);TWA3 = k;}
            }

            //interpolate back to get TWA based on found VMG
        }
        TrueVMG = MathUtils.lagrangeInterpolation(new WindAngleAndSpeed(TWS1, VMG1),new WindAngleAndSpeed(TWS2, VMG2), new WindAngleAndSpeed(TWS3, VMG3), TWS);
        pair4 = new WindAngleAndSpeed(VMG1, TWA1);
        pair5 = new WindAngleAndSpeed(VMG2, TWA2);
        pair6 = new WindAngleAndSpeed(VMG3, TWA3);
        //Interpolate between closet TWS to get highest VMG of each
        //if calculateOptimumGybe 5,6,7 if calculateOptimumTack 1,2,3

        double TWA = MathUtils.lagrangeInterpolation(pair4,pair5,pair6,TrueVMG);
        boatsTack = new WindAngleAndSpeed(TrueVMG, TWA);
        return boatsTack;
    }

    public double getOptimumVMG(boolean onTack) {
        return onTack ? tackVMGofBoat : gybeVMGofBoat;
    }

    public double getOptimumTWA(boolean onTack) {
        return onTack ? tackTWAofBoat : gybeTWAofBoat;
    }




}
