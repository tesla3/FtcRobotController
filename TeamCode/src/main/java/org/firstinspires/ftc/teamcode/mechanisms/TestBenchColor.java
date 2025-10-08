package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;



import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


import com.qualcomm.robotcore.util.RobotLog;



public class TestBenchColor {

    //NormalizedColorSensor colorSensor;
    ColorSensor colorSensor;
    double sat_threshold = 0.1;

    public enum DetectedColor {
        PURPLE {
            @Override
            public String toString(){
                return "P";
            }
        },
        GREEN  {
            @Override
            public String toString(){
                return "G";
            }
        },
        UNKNOWN   {
            @Override
            public String toString(){
                return "U";
            }
        };
    }

    public void init(HardwareMap hwMap) {
        colorSensor = hwMap.get(ColorSensor.class, "sensor_color_distance");

    }

    public DetectedColor getDetectedColor(Telemetry telemetry) {
        //NormalizedRGBA colors = colorSensor.getNormalizedColors();


        int r = colorSensor.red() ;
        int g = colorSensor.green() ;
        int b = colorSensor.blue();


        //telemetry.addData("red", r);
        //telemetry.addData("green", g);
        //telemetry.addData("blue", b);

        DetectedColor detectedColor;
        if (g > r && g > b){
            detectedColor = DetectedColor.GREEN;
        }else if (b > g && b > r) {
            detectedColor = DetectedColor.PURPLE;
        }else{
            detectedColor = DetectedColor.UNKNOWN;
        }
        //telemetry.addData("detected", detectedColor);

        float r_n = r/2  + 30;
        float g_n = g/2  + 30;
        float b_n = b + 30;

        float sum_n = r_n + g_n + b_n;
        float sat = 1 - 3 * Math.min(b_n, Math.min(r_n, g_n))/sum_n;

        //telemetry.addData("saturation", sat);

        // method 1
        // Purple = high red + high blue, low green
        // Green = high green, lower red and blue
        boolean isPurple = (r + b > g * 1.6)
                && (r + b > 60);
        boolean isGreen = (g > r * 1.4)
                && (g > b * 1.1)
                && (g > 40);

        DetectedColor detected1 = DetectedColor.UNKNOWN;
       if (sat > sat_threshold){
           if (isPurple && !isGreen) {
               detected1 = DetectedColor.PURPLE;
           } else if (isGreen){
               detected1 = DetectedColor.GREEN;
           }
       }

        //telemetry.addData("detected1", detected1);
        /*
        // method 2: Normalized indices (if threshold drift is a problem)
        int sum_rg = r + g + 1;
        int sum_bg = b + g + 1;

        float pgi = (float)(r - g) / sum_rg;  // -1 to +1
        float bgi = (float)(b - g) / sum_bg;  // -1 to +1

        boolean isPurple2 = (bgi > -0.25) && (r + b > 70);
        boolean isGreen2 = (pgi < -0.10)  && (g > 40);

        DetectedColor detected2 = DetectedColor.UNKNOWN;
        if (sat > sat_threshold) {
            if (isPurple2 && !isGreen2) {
                detected2 = DetectedColor.PURPLE;
            } else if (isGreen2) {
                detected2 = DetectedColor.GREEN;
            }
        }

        telemetry.addData("pgi", pgi);
        telemetry.addData("bgi", bgi);
        telemetry.addData("detected2", detected2);
        */

        //method 3: use logreg model
        /*
        double logit = - 60.45 * pgi - 29.34 * bgi - 5.8;
        double greenScore = 1.0 / (1.0 + Math.exp(-logit));

        DetectedColor detected3 = DetectedColor.UNKNOWN;
        if (sat > sat_threshold) {
            if ((greenScore < 0.5) && (r + b > 70)) {
                detected3 = DetectedColor.PURPLE;
            } else if ((greenScore > 0.5) && (g > 40)) {
                detected3 = DetectedColor.GREEN;
            }
        }
        telemetry.addData("greenScore", greenScore);
        telemetry.addData("detected3", detected3);
        */

        return detected1;

        /*
        DistanceSensor distanceSensor = (DistanceSensor) colorSensor;
        double distance_cm = distanceSensor.getDistance(DistanceUnit.CM); // Get distance in centimeters
        telemetry.addData("alpha", colors.alpha);
        telemetry.addData("red", colors.red);
        telemetry.addData("green", colors.green);
        telemetry.addData("blue", colors.blue);
        telemetry.addData("distance", distance_cm);
        RobotLog.ii("color_sensor.csv", "%.6f,%.6f,%.6f,%.6f,%.6f", colors.alpha, colors.red, colors.green, colors.blue, distance_cm);
         */



    }
}
