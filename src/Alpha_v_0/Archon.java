package Alpha_v_0;

import battlecode.common.*;

/**
 * Created by Patrick on 2017-01-10.
 */

public class Archon{
    private static RobotController rc;
    public static void run(RobotController _rc) throws GameActionException{
        rc = _rc;
        while (true){
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                for (float d = (float)(0.0); d < Math.PI; d += 0.1){
                    Direction dir = new Direction(d);
                    if (rc.canHireGardener(dir)){
                        rc.hireGardener(dir);
                        break;
                    }
                }

                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}
