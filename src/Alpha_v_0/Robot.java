package Alpha_v_0;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;

/**
 * Created by patri on 2017-01-17.
 */
public abstract class Robot{
    RobotController rc;
    final int robotType;
    Team EnemyTeam, OurTeam;

    private int help_callback = -1;

    Robot(RobotController _rc, int type){
        rc = _rc;
        robotType = type;
        Util.init(this, _rc);
        Util.incrementUnitCount(type);
        EnemyTeam = rc.getTeam().opponent();
        OurTeam = rc.getTeam();
    }


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
    public abstract void run() throws GameActionException;
}
