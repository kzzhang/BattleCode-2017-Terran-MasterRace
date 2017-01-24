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
            BulletVector vector = new BulletVector(bullet, rc.getType().bodyRadius);
            if (vector.willCollideWithCircle(rc.getLocation())){
                currentCase += bullet.getDamage();
            }

            /*float deltaRads = bullet.getDir().radiansBetween(new Direction(bullet.getLocation(), testLocation));
            float deltaDist = bullet.getLocation().distanceTo(testLocation);
            float projectedDist = (float) Math.sin(deltaRads) * deltaDist;
            */
            //Project if the bullet will pass or hit
            /*boolean projectedHit = (projectedDist <= rc.getType().bodyRadius);
            if (projectedHit) {
                currentCase += bullet.getDamage();     //Projected damage if we stay
            }*/
        }
        return currentCase;
    }

    public static boolean dodge() throws GameActionException{
        //TODO: Avoid map edges

        rc.setIndicatorDot(rc.getLocation(), 255,255,255);
        BulletInfo[] visibleBullets = rc.senseNearbyBullets();
        Vector<BulletVector> bulletVectors = new Vector<>(visibleBullets.length);

        boolean isMoving = false;

        float bestCase = 0;
        MapLocation moveLocation = rc.getLocation();
        for (int i = 0, c = 0; i < visibleBullets.length; ++i){
            if (moveLocation.distanceTo(visibleBullets[i].getLocation()) > rc.getType().strideRadius &&
                    !BulletVector.isTravellingAway(visibleBullets[i], moveLocation)) {
                bulletVectors.add(new BulletVector(visibleBullets[i], rc.getType().bodyRadius));
                if (bulletVectors.get(c).willCollideWithCircle(rc.getLocation())) {
                    bestCase += visibleBullets[i].getDamage();
                }
                rc.setIndicatorLine(new MapLocation(bulletVectors.get(c).getX0(), bulletVectors.get(c).getY0()),
                        new MapLocation(bulletVectors.get(c).getX1(), bulletVectors.get(c).getY1()), 0, 0, 0);
                rc.setIndicatorDot(new MapLocation(bulletVectors.get(c).getX1(), bulletVectors.get(c).getY1()), 100, 0, 100);
                c++;
            }
        }


        if (bestCase != 0) {
            Direction closestEnemyDirection = new Direction(0);

            //Find Closest Enemy Robot -- Then ask for support
            RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            RobotInfo closestEnemy = null;
            float closest_dist = Float.MAX_VALUE;
            for (RobotInfo enemy : nearbyEnemies){
                float testDist = enemy.getLocation().distanceTo(rc.getLocation());
                if (closestEnemy == null || testDist < closest_dist){
                    closestEnemy = enemy;
                    closest_dist = testDist;
                }
            }

            if (closestEnemy != null) {
                Comms.RequestHelp(Comms.help_type_fight, closestEnemy.getLocation());
                closestEnemyDirection = rc.getLocation().directionTo(closestEnemy.getLocation());
            }

            Direction testDirection = closestEnemyDirection;
            testDirection.rotateLeftDegrees(90);

            final int CHECK_TICKS = 8;
            final float CHECK_INCREMENTS_DEG = (float)360.0 / (float)CHECK_TICKS;


            for (int i = 0; i < CHECK_TICKS; ++i){
                float currentCase = 0;
                testDirection = testDirection.rotateLeftDegrees(CHECK_INCREMENTS_DEG);

                MapLocation testLocation = rc.getLocation().add(testDirection, rc.getType().strideRadius);
                if (rc.canMove(testLocation)) {
                    rc.setIndicatorDot(testLocation, 155, 0, 0);

                    //System.out.println("Checkpt A: " + Float.toString(Clock.getBytecodesLeft()));
                    for (BulletVector bulletVector : bulletVectors) {
                        if (bulletVector.willCollideWithCircle(testLocation)) {
                            currentCase += bulletVector.getDamage();
                        }
                    }
                    //System.out.println("Checkpt B: " + Float.toString(Clock.getBytecodesLeft()));


                    if (currentCase < bestCase) {
                        bestCase = currentCase;
                        moveLocation = testLocation;
                        isMoving = true;
                    }

                /*if (bestCase == 0 || Clock.getBytecodesLeft() < 500){
                    break;
                }*/
                }else{
                    rc.setIndicatorDot(testLocation, 100, 100, 100);
                }
            }
        }
        if (bestCase == 0) {
            rc.setIndicatorDot(moveLocation, 0, 155, 0);
        }else{
            rc.setIndicatorDot(moveLocation, 155, 155, 0);

        }
        if (bestCase >= (int) rc.getHealth()){
            rb.onDeathImmenent();
        }

        if (isMoving && rc.canMove(moveLocation)){
            rc.move(moveLocation);
            return true;
        }else{
            return false;
        }

        /*boolean standStill = true;
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
        }*/


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
