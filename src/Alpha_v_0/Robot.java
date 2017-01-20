package Alpha_v_0;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Created by patri on 2017-01-17.
 */
public abstract class Robot{
    private int help_callback = -1;
    public void setHelpCallback(int callback){
        help_callback = callback;
    }
    public int getHelpCallback(){
        return help_callback;
    }
    public void onDeathImmenent(){
        System.out.println("Dying...");
        Util.Comms.ClearRequest(help_callback);
    }
    public void run(RobotController rc, int type) throws GameActionException{
        Util.init(this, rc);
        Util.incrementUnitCount(type);
    }
}
