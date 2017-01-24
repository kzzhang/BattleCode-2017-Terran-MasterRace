package Alpha_v_0;

import battlecode.common.*;

/**
 * Created by patri on 2017-01-16.
 */
public class Soldier extends Robot{
    Soldier(RobotController rc, int type){
        super(rc, type);
    }

    @Override
    public void run() throws GameActionException {

        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {


            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();



                if (!Util.dodge()) {
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
                        rc.setIndicatorDot(m,244,144,66);

                    }

                    Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI - (float) Math.PI);
                    if (closestFight != null){
                        //System.out.println("Helping Comrade at :" + closestFight.toString());
                        //rc.setIndicatorDot(closestFight, 66,188,244);
                        dir = new Direction(rc.getLocation(), closestFight);
                    }
                    if (rc.canMove(dir))
                        rc.move(dir);

                    /*if (Util.safeMove(rc, dir) == 0) {
                        RobotPlayer.tryMove(dir);
                    }*/
                }

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
