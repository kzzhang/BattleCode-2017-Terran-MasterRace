package Alpha_v_0;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by patri on 2017-01-11.
 */

public class Util {
    public static final int channel_requests = 0, channel_requests_size = 100;  //0 - 99 for RobotRequests
    public static final int channel_unit_counts = 994; //up to 1000

    public static final int type_gardener = 0;
    public static final int type_soldier = 1;
    public static final int type_archon = 2;
    public static final int type_tank = 3;
    public static final int type_scout = 4;
    public static final int type_lumberjack = 5;
    
    public static final float PI = (float) 3.14;

    private static RobotController rc;
    private static Robot rb;

    public static void init(Robot _rb, RobotController _rc){
        rc = _rc;
        rb = _rb;
    }

    private static final int float_int_factor = 1000000;

    public static int floatToBroadcastInt(float v){
        return (int)(v * float_int_factor);
    }

    public static float broadcastIntToFloat(int v){
        return ((float)(v)) / float_int_factor;
    }

    public static int getUnitCount(int type){
        try {
            return rc.readBroadcast(channel_unit_counts + type);
        }catch (Exception e){
            System.out.println("Error:getUnitCount:: Failed to get type: " + Integer.toString(type));
            return -1;
        }
    }

    public static void incrementUnitCount(int type){
        try{
            int initCount = getUnitCount(type);
            if (initCount != -1) {
                rc.broadcast(channel_unit_counts + type, initCount + 1);
            }

            System.out.println(getUnitCount(type));
        }catch (Exception e){
            System.out.println("Error:incrementUnitCount:: Failed to increment type: " + Integer.toString(type));
        }
    }

    public static int safeMove(RobotController rc, Direction d){
        BulletInfo[] visibleBullets = rc.senseNearbyBullets();
        int currentCase = 0;
        for (BulletInfo bullet : visibleBullets){

            MapLocation testLocation = rc.getLocation();
            testLocation = testLocation.add(d, rc.getType().strideRadius);

            //Create projection of bullet
            float deltaRads = bullet.getDir().radiansBetween(new Direction(bullet.getLocation(), testLocation));
            float deltaDist = bullet.getLocation().distanceTo(testLocation);
            float projectedDist = (float) Math.sin(deltaRads) * deltaDist;

            //Project if the bullet will pass or hit
            boolean projectedHit = (projectedDist <= rc.getType().bodyRadius);
            if (projectedHit) {
                currentCase += bullet.getDamage();     //Projected damage if we stay
            }
        }
        return currentCase;
    }

    public static boolean dodge() throws GameActionException{
        //TODO: Avoid map edges

        rc.setIndicatorDot(rc.getLocation(), 255,255,255);
        BulletInfo[] visibleBullets = rc.senseNearbyBullets();
        float bestCase = 0;
        MapLocation moveLocation = rc.getLocation();
        boolean standStill = true;
        for (BulletInfo bullet : visibleBullets){
            //Create projection of bullet
            float deltaRads = bullet.getDir().radiansBetween(new Direction(bullet.getLocation(), rc.getLocation()));
            float deltaDist = bullet.getLocation().distanceTo(rc.getLocation());
            float projectedDist = (float) Math.sin(deltaRads) * deltaDist;

            //Project if the bullet will pass or hit
            boolean projectedHit = (projectedDist <= rc.getType().bodyRadius);
            if (projectedHit) {
                bestCase += bullet.getDamage();     //Projected damage if we stay
                standStill = false;
            }
        }


        if (!standStill)
        //If a hit is projected, find alternatives
        {
            float delta_rot = 0;
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

            RobotInfo closest_robot = null;
            float closest_dist = Float.MAX_VALUE;
            for (RobotInfo r  : nearbyRobots){
                float testDist = r.getLocation().distanceTo(rc.getLocation());
                if (closest_robot == null || testDist < closest_dist){
                    closest_robot = r;
                    closest_dist = testDist;
                }
            }
            if (closest_robot != null) {
                Comms.RequestHelp(Comms.help_type_fight, closest_robot.getLocation());
                //System.out.println("Requesting Help : " + closest_robot.getLocation().toString();
                Direction directionToClosetEnemy = rc.getLocation().directionTo(closest_robot.getLocation());
                delta_rot = directionToClosetEnemy.radians;
                delta_rot += Util.PI / 2;
                delta_rot %= Util.PI * 2;
            }

            //Check paths 0.05 rad apart (~2.5 deg)
            for (float x = delta_rot; x < 2 * Util.PI + delta_rot; x += 0.1){
                float i = (x % (2 * Util.PI)) - Util.PI;
                float currentCase = 0;

                MapLocation testLocation = rc.getLocation();
                testLocation = testLocation.add(new Direction(i), rc.getType().strideRadius ); //* (int)(bullet.getSpeed() / bullet.getLocation().distanceTo(rc.getLocation()) + 1));

                for (BulletInfo bullet : visibleBullets){

                    //Create projection of bullet
                    float deltaRads = bullet.getDir().radiansBetween(new Direction(bullet.getLocation(), testLocation));
                    float deltaDist = bullet.getLocation().distanceTo(testLocation);
                    float projectedDist = (float) Math.sin(deltaRads) * deltaDist;

                    //Project if the bullet will pass or hit
                    boolean projectedHit = (projectedDist <= rc.getType().bodyRadius);
                    if (projectedHit) {
                        currentCase += bullet.getDamage();     //Projected damage if we stay
                    }
                }
                if (currentCase < bestCase){
                    System.out.println(Float.toString(currentCase) + "," +  Float.toString(bestCase) + "," + rc.getHealth());
                    bestCase = currentCase;
                    moveLocation = testLocation;

                }
                if (bestCase == 0 || Clock.getBytecodesLeft() < 500){
                    rc.setIndicatorDot(testLocation, 0, 155, 0);
                    break;
                }else{
                    rc.setIndicatorDot(testLocation, 155, 0, 0);
                }
            }
        }

        if (bestCase >= (int) rc.getHealth()){
            rb.onDeathImmenent();
        }

        if (rc.canMove(moveLocation) || standStill){
            rc.move(moveLocation);
            return true;
        }else{
            System.out.println("Not Dodging : " + Integer.toString(Clock.getBytecodesLeft()));
            return false;
        }
    }
    public static class Comms {
        public static final int help_type_null = 0;
        public static final int help_type_tree_removal = 1;
        public static final int help_type_tree_plant = 2;
        public static final int help_type_tree_shake = 3;
        public static final int help_type_archon_def = 4;
        public static final int help_type_fight = 5;

        public static int RequestHelp(int help_type, MapLocation location){
            //Requests work this way, numbers are used
            //Type, X_coord, Y_coord, ticker

            ClearRequest(rb.getHelpCallback());

            for (int i = channel_requests; i < channel_requests + channel_requests_size; i += 4){
                try{
                    int r_type = rc.readBroadcast(i);

                    if (r_type == help_type_null)
                    //if unused, overwrite
                    {
                        rc.broadcast(i, help_type);
                        rc.broadcast(i + 1, floatToBroadcastInt(location.x));
                        rc.broadcast(i + 2, floatToBroadcastInt(location.y));
                        rc.broadcast(i + 3, 10);

                        rb.setHelpCallback(i);
                        return i;
                    }
                }catch (Exception e){
                    System.out.println("Error:RequestHelp:: Failed to get channel: " + Integer.toString(i));
                }
            }
            rb.setHelpCallback(-1);
            return -1;
        }
        public static void ClearRequest(int callback){
            try{
                if (callback != -1)
                    rc.broadcast(callback, help_type_null);
                rb.setHelpCallback(-1);
            }catch (Exception e){
                System.out.println("Error:ClearRequest:: Failed to set channel: " + Integer.toString(callback));
            }
        }

        public static MapLocation[] getHelpRequestLocations(int help_type){
            ArrayList<MapLocation> locations = new ArrayList<>();
            for (int i = channel_requests; i < channel_requests + channel_requests_size; i += 3){
                try{
                    int r_type = rc.readBroadcast(i);

                    if (r_type == help_type)
                    {
                        float x = broadcastIntToFloat(rc.readBroadcast(i + 1));
                        float y = broadcastIntToFloat(rc.readBroadcast(i + 2));
                        locations.add(new MapLocation(x, y));
                    }

                }catch (Exception e){
                    System.out.println("Error:RequestHelp:: Failed to get channel: " + Integer.toString(i));
                }
            }
            return locations.toArray(new MapLocation[locations.size()]);
        }
    }


}
