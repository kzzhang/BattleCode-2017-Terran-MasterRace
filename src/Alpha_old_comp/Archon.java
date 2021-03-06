package Alpha_old_comp;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static Alpha_v_0.Util.channel_requests;
import static Alpha_v_0.Util.channel_requests_size;

/**
 * Created by Patrick on 2017-01-10.
 */

public class Archon extends Robot {
    Archon(RobotController rc, int type){
        super(rc, type);
    }
    @Override
    public void run() throws GameActionException{
        int GardenerCount = 0;
        while (true){
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                Util.Comms.ClearRequest(getHelpCallback());
                if (rc.getTeamBullets() >= (float)10000.0){
                    rc.donate(rc.getTeamBullets() - rc.getTeamBullets() % (float)10.0);
                }
                for (int i = channel_requests; i < channel_requests + channel_requests_size; i += 4) {
                    int tickerVal = rc.readBroadcast(i + 3);
                    if (tickerVal > 0){
                        rc.broadcast(i + 3, tickerVal - 1);
                        if (tickerVal - 1 == 0){
                            rc.broadcast(i, Util.Comms.help_type_null);
                        }
                    }
                }

                for (float d = (float) -(Math.PI); d < Math.PI; d += 0.1){
                    Direction dir = new Direction(d);
                    if (rc.canHireGardener(dir) && Math.random() < (.01/(2*Math.PI))){
                        rc.hireGardener(dir);
                        GardenerCount++;
                        break;
                    }
                }

                Util.dodge();
                //Find best case movement -> if unavoidable take the lowest damage bullet
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeathImmenent() {
        super.onDeathImmenent();
        //Decrement unit count
    }
}
