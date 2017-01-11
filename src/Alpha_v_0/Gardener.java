package Alpha_v_0;

import battlecode.common.*;

import static Alpha_v_0.RobotPlayer.randomDirection;
import static Alpha_v_0.RobotPlayer.tryMove;
import static battlecode.common.Team.NEUTRAL;

/**
 * Created by kzhan_000 on 2017-01-11.
 */
public class Gardener {
    private static RobotController rc;

    public static void run(RobotController _rc) throws GameActionException {
        rc = _rc;
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction dir = randomDirection();

                boolean shaken = false;
                TreeInfo[] nearby = rc.senseNearbyTrees((float)7.0, NEUTRAL);
                for (int i = 0; i<nearby.length; i++){
                    if (rc.canShake(nearby[i].getID()) && nearby[i].getContainedBullets() >= 1){
                        rc.shake(nearby[i].getID());
                        shaken = true;
                        i = nearby.length;
                    }
                }

                if (!shaken){
                    MapLocation posClosestNeutral = nearby[0].getLocation();
                    float distanceToTree = Distance.find(rc.getLocation(), posClosestNeutral);

                    for (int i = 1; i<nearby.length; i++){
                        if (Distance.find(rc.getLocation(), nearby[i].getLocation()) < distanceToTree){
                            posClosestNeutral = nearby[i].getLocation();
                        }
                    }

                    Direction direction = new Direction(rc.getLocation(), posClosestNeutral);
                    rc.move(direction);
                }

                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                } /*else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                }*/

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
}
