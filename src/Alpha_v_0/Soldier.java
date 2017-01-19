package Alpha_v_0;

import battlecode.common.*;

/**
 * Created by patri on 2017-01-16.
 */
public class Soldier extends Robot{
    @Override
    public void run(RobotController rc) throws GameActionException {
        Util.init(this, rc);
        Util.incrementUnitCount(Util.type_soldier);

        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {


            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }

                if (!Util.dodge(rc)) {
                    Util.Comms.ClearRequest(getHelpCallback());


                    MapLocation[] fightLocations = Util.Comms.getHelpRequestLocations(Util.Comms.help_type_fight);

                    MapLocation closestFight = null;
                    float closestDist = Float.MAX_VALUE;
                    for (MapLocation m : fightLocations){
                        float testDist = m.distanceSquaredTo(rc.getLocation());
                        if (testDist < closestDist){
                            closestDist = testDist;
                            closestFight = m;
                        }
                    }

                    Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
                    if (closestFight != null){
                        System.out.println("Helping Comrade at :" + closestFight.toString());
                        dir = new Direction(rc.getLocation(), closestFight);
                    }
                    if (rc.canMove(dir))
                        rc.move(dir);

                    /*if (Util.safeMove(rc, dir) == 0) {
                        RobotPlayer.tryMove(dir);
                    }*/
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeathImmenent(){
        super.onDeathImmenent();
    }
}
