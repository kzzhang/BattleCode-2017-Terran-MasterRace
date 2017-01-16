package Alpha_v_0;

import battlecode.common.*;

/**
 * Created by Patrick on 2017-01-10.
 */

public class Archon{
    public static void run(RobotController rc) throws GameActionException{
        int GardenerCount = 0;
        while (true){
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                for (float d = (float)(0.0); d < Math.PI; d += 0.1){
                    Direction dir = new Direction(d);
                    if (rc.canHireGardener(dir) && Math.random() < (.01/(2*Math.PI))){
                        rc.hireGardener(dir);
                        GardenerCount++;
                        break;
                    }
                }

                Util.dodge(rc);
                //Find best case movement -> if unavoidable take the lowest damage bullet
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}
