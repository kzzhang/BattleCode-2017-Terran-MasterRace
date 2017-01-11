package Alpha_v_0;

import battlecode.common.*;

/**
 * Created by patri on 2017-01-11.
 */
public class Util {
    public static void dodge(RobotController rc) throws GameActionException{
        BulletInfo[] visibleBullets = rc.senseNearbyBullets();
        int bestCase = 0;
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
            //Check paths 0.05 rad apart (~2.5 deg)
            for (float i = (float) 0.0; i < 2 * Math.PI; i += 0.05){

                int currentCase = 0;
                for (BulletInfo bullet : visibleBullets){

                    MapLocation testLocation = rc.getLocation();
                    testLocation.add(new Direction(i), rc.getType().strideRadius * (int)(bullet.getSpeed() / bullet.getLocation().distanceTo(rc.getLocation())));

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
                    bestCase = currentCase;
                    moveRads = i;
                }
            }
        }

        if (moveRads != -1 && rc.canMove(new Direction(moveRads))){
            rc.move(new Direction(moveRads));
        }
    }
}
