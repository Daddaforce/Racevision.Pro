package seng302.utilities;
import javafx.util.Pair;
import seng302.models.Polar;
import seng302.models.WindAngleAndSpeed;

import java.io.*;
import java.util.ArrayList;


/**
 * Created by gemma on 12/04/2017.
 */
public class PolarReader {
    private static final String POLAR_FILE_LOCATION = "/defaultFiles/boatPolars.txt";

    public static ArrayList<Polar> getPolarsForAC35Yachts() {
        if (polars == null) {
            readPolars();
        }
        return polars;
    }

    private static ArrayList<Polar> polars = null;

    private static void readPolars() {
        polars = new ArrayList<>();
        String thisLine;
        try {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(PolarReader.class.getResourceAsStream(POLAR_FILE_LOCATION)));
            String titleLine = br.readLine(); // Reads in first line of titles
            String[] titles = titleLine.split(",");

            while ((thisLine = br.readLine()) != null) {
                String[] line = thisLine.split(",");
                int TWS = Integer.parseInt(line[0]);
                Polar tempPolar = new Polar(TWS);
                for (int i = 1; i < line.length; i += 2) {
                    double angle = Double.parseDouble(line[i]);
                    double speed = Double.parseDouble(line[i + 1]);
                    if (titles[i].equals("UpTwa")) {
                        tempPolar.setUpWindOptimum(new WindAngleAndSpeed(angle, speed));
                    } else if (titles[i].equals("DnTwa")) {
                        tempPolar.setDownWindOptimum(new WindAngleAndSpeed(angle, speed));
                    } else {
                        tempPolar.addTWAandBSP(angle, speed);
                    }
                }
                polars.add(tempPolar);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

