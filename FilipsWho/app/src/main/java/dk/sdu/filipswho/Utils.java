package dk.sdu.filipswho;

public class Utils {

    /**
     * This method is based on a regression model made with a watt meter and an IKEA E27 1000 lumen light bulb
     * @param intensity Value between 0 and 254
     * @return the watt usage from 0.2 watt to 11.8 watt
     */
    public static double wattUsage(int intensity) {
        if (intensity == 0){
            return 0.2;
        } else {
            return 0.737514880516844 * Math.exp(0.010723316391607 * intensity);
        }
    }

    /**
     * Returns the provided double value to the amount of decimals
     * @param d
     * @param decimals
     * @return
     */
    public static double round(double d, int decimals) {
        return  Math.round(d * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }
}
