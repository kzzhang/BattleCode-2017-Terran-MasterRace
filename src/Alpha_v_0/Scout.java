package Alpha_v_0;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Created by patri on 2017-01-19.
 */
public class Scout extends Robot{

    Scout(RobotController rc, int type){
        super(rc, type);
    }

    @Override
    public void run() throws GameActionException {

        while (true){
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {


            }catch (Exception e){
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }

    }
}
