package Alpha_old_comp;

import battlecode.common.MapLocation;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by kzhan_000 on 2017-01-11.
 */
public class Distance {
    public static float find(MapLocation a, MapLocation b){
        float x = a.x - b.x;
        float y = a.y - b.y;
        double distance = sqrt((pow(x,2)+pow(y,2)));
        return (float)distance;
    }
}
