package Alpha_v_0;

import battlecode.common.*;

/**
 * Created by patri on 2017-01-11.
 */
public class Util {
    public static final int channel_unit_counts = 994; //up to 1000

    public static final int type_gardener = 0;
    public static final int type_soldier = 1;
    public static final int type_archon = 2;
    public static final int type_tank = 3;
    public static final int type_scout = 4;
    public static final int type_lumberjack = 5;
    
    public static final float PI = (float) 3.14;

    private static RobotController rc;

    public static void init(RobotController _rc){
        rc = _rc;
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

    public static boolean dodge(RobotController rc) throws GameActionException{

        //TODO: Avoid map edges

        BulletInfo[] visibleBullets = rc.senseNearbyBullets();
        float bestCase = 0;
        float moveRads = -1;
        for (BulletInfo bullet : visibleBullets){
            //Create projection of bullet
            float deltaRads = bullet.getDir().radiansBetween(new Direction(bullet.getLocation(), rc.getLocation()));
            float deltaDist = bullet.getLocation().distanceTo(rc.getLocation());
            float projectedDist = (float) Math.sin(deltaRads) * deltaDist;

            //Project if the bullet will pass or hit
            boolean projectedHit = (projectedDist <= rc.getType().bodyRadius);
            if (projectedHit) {
                bestCase += bullet.getDamage();     //Projected damage if we stay
            }
        }


        if (bestCase != 0)
        //If a hit is projected, find alternatives
        {
            float delta_rot = 0;
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

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
                Direction directionToClosetEnemy = rc.getLocation().directionTo(closest_robot.getLocation());
                delta_rot = directionToClosetEnemy.radians;
                delta_rot += Util.PI / 2;
                delta_rot %= Util.PI * 2;
            }

            //Check paths 0.05 rad apart (~2.5 deg)
            for (float x = delta_rot; x < 2 * Util.PI + delta_rot; x += 0.1){
                float i = x % (float) (2 * Util.PI);
                float currentCase = 0;
                for (BulletInfo bullet : visibleBullets){

                    MapLocation testLocation = rc.getLocation();
                    testLocation = testLocation.add(new Direction(i), rc.getType().strideRadius ); //* (int)(bullet.getSpeed() / bullet.getLocation().distanceTo(rc.getLocation()) + 1));

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
                    System.out.println(Float.toString(currentCase) + "," +  Float.toString(bestCase));
                    bestCase = currentCase;
                    moveRads = i;

                }
                if (bestCase == 0 || Clock.getBytecodesLeft() < 500){
                    break;
                }
            }
        }

        if (moveRads != -1 && rc.canMove(new Direction(moveRads))){
            rc.move(new Direction(moveRads));
            return true;
        }else{
            return false;
        }
    }
}
