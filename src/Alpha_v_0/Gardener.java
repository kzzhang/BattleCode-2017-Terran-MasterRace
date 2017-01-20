package Alpha_v_0;

import battlecode.common.*;

import static Alpha_v_0.RobotPlayer.randomDirection;
import static Alpha_v_0.RobotPlayer.tryMove;
import static battlecode.common.Team.NEUTRAL;

/**
 * Created by kzhan_000 on 2017-01-11.
 */
public class Gardener extends Robot{
    TreeInfo lastPlanted = null;
    Gardener(RobotController rc, int type){
        super(rc, type);
    }

    @Override
    public void run() throws GameActionException {
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Generate a random direction
                Direction dir = randomDirection();

                // Listen for home archon's location
                //int xPos = rc.readBroadcast(0);
                //int yPos = rc.readBroadcast(1);
                //MapLocation archonLoc = new MapLocation(xPos,yPos);


                boolean shaken = false;
                boolean planting = true;
                Team self = rc.getTeam();
                TreeInfo[] close = rc.senseNearbyTrees((float)3.2, self);
                //RobotInfo[] friendlyClose = rc.senseNearbyRobots((float)7, self);

                if (planting) {
                    boolean shouldPlant = true;
                    for (double i = 0.1; i < (3.14159*2); i+= 0.1){
                        float angle;
                        if (lastPlanted != null){
                            angle = new Direction(rc.getLocation(), lastPlanted.getLocation()).radians + (float)i;
                        }
                        else{
                            angle = dir.radians + (float)i;
                        }
                        if (angle > (3.14159)) {angle -= (3.14159*2);}
                        Direction positive = new Direction(angle);
                        MapLocation newTree = rc.getLocation().add(positive, (float)1.0);
                        for (TreeInfo tree : close){
                            if (newTree.distanceTo(tree.getLocation())<= (float)3) {
                                shouldPlant = false;
                                break;
                            }
                        }
                        if (shouldPlant){
                            if (rc.canPlantTree(positive)){
                                rc.plantTree(positive);
                            }
                        }
                        lastPlanted = rc.senseTreeAtLocation(newTree);
                    }
                }

                TreeInfo[] nearby = rc.senseNearbyTrees((float)5.0, self);
                //watering
                TreeInfo target = null;
                for (TreeInfo tree : nearby){
                    if (tree.getHealth() < 45 && rc.canWater(tree.getLocation())){
                        if (target == null) {target = tree;}
                        else if (target.getHealth()>tree.getHealth()){
                            target = tree;
                        }
                    }
                }
                rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
                rc.setIndicatorDot(target.getLocation(), 0, 0, 255);
                if (target != null){
                    if (rc.canWater(target.getLocation())){
                        rc.water(target.getLocation());
                    }
                    else {
                        rc.move(target.getLocation());
                        if (rc.canWater(target.getLocation())) {
                            rc.water(target.getLocation());
                        }
                    }
                }

                for (int i = 0; i<nearby.length; i++){
                    if (rc.canShake(nearby[i].getID()) && nearby[i].getContainedBullets() >= 1){
                        rc.shake(nearby[i].getID());
                        shaken = true;
                        i = nearby.length;
                    }
                }
                /*
                //if no shake available
                if (!shaken){
                    MapLocation posClosestNeutral = null;
                    float distanceToTree = -1;
                    int start = 0;

                    //find first tree nearby with bullets available
                    for (int i = 0; i<nearby.length; i++){
                        if (nearby[i].getContainedBullets() > 2.0){
                            posClosestNeutral = nearby[i].getLocation();
                            distanceToTree = Distance.find(rc.getLocation(), posClosestNeutral);
                            start = i;
                            break;
                        }
                    }

                    //find closest tree nearby with bullets available and no friendly gardeners nearby
                    boolean found = false;
                    if (posClosestNeutral != null) {
                        for (int i = start; i < nearby.length; i++) {
                            System.out.println(Distance.find(rc.getLocation(), nearby[i].getLocation()));
                            System.out.println(distanceToTree);
                            System.out.println(nearby[i].getContainedBullets());
                            if (Distance.find(rc.getLocation(), nearby[i].getLocation()) < distanceToTree && nearby[i].getContainedBullets() > 0) {
                                System.out.println("found close!");
                                for (RobotInfo data : friendlyClose) {
                                    if (data.getType() == RobotType.GARDENER) {
                                        System.out.println("Nearby!");
                                        if (Distance.find(rc.getLocation(), nearby[i].getLocation()) > Distance.find(data.getLocation(), nearby[i].getLocation())) {
                                            found = true;
                                            System.out.println("Found!");
                                            break;
                                        }
                                    }
                                }
                                if (!found) {
                                    posClosestNeutral = nearby[i].getLocation();
                                    distanceToTree = Distance.find(rc.getLocation(), posClosestNeutral);
                                }
                            }
                        }
                    }


                    //take shortest path to closest tree

                    Direction direction;
                    if (posClosestNeutral != null) direction = new Direction(rc.getLocation(), posClosestNeutral);
                    else{
                        float archondir = 0;
                        MapLocation[] archons = rc.getInitialArchonLocations(self);
                        float distance = 99999;
                        for (MapLocation archon : archons){
                            if (Distance.find(rc.getLocation(), archon) < distance){
                                distance = Distance.find(rc.getLocation(), archon);
                                direction = new Direction(rc.getLocation(), archon);
                                archondir = direction.radians;
                            }
                        }
                        archondir += 3.14159;
                        if (archondir > (3.14159*2)) archondir -= (float)(3.14159*2);
                        direction = new Direction(archondir);
                    }

                    if (rc.canMove(direction)){
                        rc.move(direction);
                    }else {
                        for (double i = 0.1; i < 3.14159; i+= 0.1){
                            Direction positive = new Direction(direction.radians + (float)i);
                            if (rc.canMove(positive)){
                                rc.move(positive);
                            }
                            float negative = direction.radians - (float)i;
                            if (negative < 0.0) { negative += (3.14159*2) ;}
                            Direction negativeDir = new Direction(negative);
                            if (rc.canMove(negativeDir)){
                                rc.move(negativeDir);
                            }
                        }
                    }
                }
                */
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                } /*else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                }*/

                // Move randomly
                //tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                System.out.println("Bytes left: " + Integer.toString(Clock.getBytecodesLeft()));
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
}
