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
    Direction goal = null;

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

                boolean canPlant = true;
                boolean canWater = true;
                Team self = rc.getTeam();
                TreeInfo[] close = rc.senseNearbyTrees((float)4, self);
                MapLocation home[] = rc.getInitialArchonLocations(self);

                MapLocation center = home[0];
                float scale = rc.getLocation().distanceTo(home[0]);
                for (MapLocation base : home) {
                    float distance = rc.getLocation().distanceTo(base);
                    if (distance < scale) {
                        scale = distance;
                        center = base;
                    }
                }

                if (canPlant) {
                    boolean shouldPlant = true;
                    for (double i = 0.5; i < (3.14159*2); i+= 0.4) {
                        float angle;
                        if (lastPlanted != null) {
                            angle = new Direction(rc.getLocation(), lastPlanted.getLocation()).radians + (float) i;
                        } else {
                            angle = dir.radians + (float) i;
                        }
                        if (angle > (3.14159)) {
                            angle -= (3.14159 * 2);
                        }
                        Direction positive = new Direction(angle);
                        MapLocation newTree = rc.getLocation().add(positive, (float) 1.0);
                        for (TreeInfo tree : close) {
                            if (newTree.distanceTo(tree.getLocation()) <= (float) 3.2) {
                                shouldPlant = false;
                                break;
                            }
                        }

                        if (shouldPlant){
                            if (newTree.distanceTo(center) <= 4.1) {
                                shouldPlant = false;
                                break;
                            }
                        }
                        if (shouldPlant){
                            if (rc.canPlantTree(positive)){
                                canPlant = false;
                                rc.plantTree(positive);
                                lastPlanted = rc.senseTreeAtLocation(newTree);
                                break;
                            }
                        }
                    }
                }

                //watering
                if (canWater) {
                    TreeInfo target = null;
                    for (TreeInfo tree : close) {
                        if (tree.getHealth() < 40 || (tree.getHealth() < 45 && rc.getLocation().distanceTo(tree.getLocation())<1.2)){
                            if (target == null) {
                                target = tree;
                            }
                            if (target.getHealth() > tree.getHealth()) {
                                RobotInfo allies[] = rc.senseNearbyRobots(7, self);
                                boolean isClosest = true;
                                float distance = rc.getLocation().distanceTo(target.getLocation());
                                for (RobotInfo robot : allies){
                                    if (robot.getType() == RobotType.GARDENER){
                                        if (robot.getLocation().distanceTo(target.getLocation()) < distance){
                                            isClosest = false;
                                            break;
                                        }
                                    }
                                }
                                if (isClosest) { target = tree; }
                            }
                        }
                    }
                    rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
                    if (target != null) rc.setIndicatorDot(target.getLocation(), 0, 0, 255);
                    if (target != null) {
                        if (rc.canWater(target.getLocation())) {
                            rc.water(target.getLocation());
                            canWater = false;
                        } else {
                            Direction toTree = new Direction(rc.getLocation(), target.getLocation());
                            if (rc.canMove(toTree)){
                                rc.move(toTree);
                            }else {
                                for (double i = 0.1; i < (3.14159/4); i+= 0.4){
                                    float positive = toTree.radians + (float)i;
                                    if (positive > (3.14159)) { positive -= (3.1415*2); }
                                    Direction posDir = new Direction(positive);
                                    if (rc.canMove(posDir)){
                                        rc.move(posDir);
                                    }
                                    float negative = toTree.radians - (float)i;
                                    if (negative < -3.14159) { negative += (3.1415*2) ;}
                                    Direction negativeDir = new Direction(negative);
                                    if (rc.canMove(negativeDir)){
                                        rc.move(negativeDir);
                                    }
                                }
                            }
                            if (rc.canWater(target.getLocation())) {
                                rc.water(target.getLocation());
                                canWater = false;
                            }
                        }
                    }
                }

                //Todo: if blocked, stop trying to go water
                if (!rc.hasMoved()){
                    if (goal == null) {
                        goal = new Direction(center, rc.getLocation());
                    }
                    if (rc.canMove(goal)){
                        rc.move(goal);
                    }else {
                        for (double i = 0.1; i < 3.14159; i+= 0.4){
                            float positive = goal.radians + (float)i;
                            if (positive > 3.14159) { positive -= (3.1415*2) ;}
                            Direction positiveDir = new Direction(positive);
                            if (rc.canMove(positiveDir)){
                                rc.move(positiveDir);
                                goal = positiveDir;
                            }
                            float negative = goal.radians - (float)i;
                            if (negative < -3.14159) { negative += (3.1415*2) ;}
                            Direction negativeDir = new Direction(negative);
                            if (rc.canMove(negativeDir)){
                                rc.move(negativeDir);
                                goal = negativeDir;
                            }
                        }
                    }
                    if (canPlant){
                        boolean shouldPlant = true;
                        for (double i = 0.5; i < (3.14159*2); i+= 0.4){
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
                                    lastPlanted = rc.senseTreeAtLocation(newTree);
                                    break;
                                }
                            }
                        }
                    }
                }

                /*
                nearby = rc.senseNearbyTrees((float)5.0, NEUTRAL);
                for (int i = 0; i<nearby.length; i++){
                    if (rc.canShake(nearby[i].getID()) && nearby[i].getContainedBullets() >= 1){
                        rc.shake(nearby[i].getID());
                        break;
                    }
                    if (rc.canShake(nearby[i].getID()) && nearby[i].getContainedBullets() >= 1){
                        rc.shake(nearby[i].getID());
                        break;
                    }
                }*/
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
