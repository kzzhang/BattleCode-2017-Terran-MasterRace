package Alpha_v_0;

import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * Created by patri on 2017-01-23.
 */
public class BulletVector{
    private float bx0, bx1, by0, by1;
    private float dx, dy;
    private float magnitude;
    private float damage;
    private float radius;
    MapLocation vectorOrigin, vectorTail;

    public static boolean isTravellingAway(BulletInfo info, MapLocation robotLocation){
        MapLocation vectorOrigin = info.getLocation();
        MapLocation vectorTail = vectorOrigin.add(info.getDir(), info.getSpeed());
        return robotLocation.distanceTo(vectorOrigin) < robotLocation.distanceTo(vectorTail);
    }

    BulletVector(BulletInfo info, float _radius){
        MapLocation projection = info.getLocation().add(info.getDir(), info.getSpeed());
        init (info.getLocation(), info.getDir(), info.getSpeed(), info.getDamage(), _radius);
        System.out.println(info.getSpeed());
    }

    private void init(MapLocation origin, Direction dir, float speed, float _damage, float _radius){
        vectorOrigin = origin;
        vectorTail = vectorOrigin.add(dir, speed);

        bx0 = vectorOrigin.x;
        by0 = vectorOrigin.y;
        bx1 = vectorTail.x;
        by1 = vectorTail.y;

        dx = bx1 - bx0;
        dy = by1 - by0;
        magnitude = speed;
        System.out.println(magnitude);
        damage = _damage;
        radius = _radius;
    }

    public boolean willCollideWithCircle(float x0, float y0){
        MapLocation robotLocation = new MapLocation(x0, y0);
        boolean towardsLocation = robotLocation.distanceTo(vectorOrigin) > robotLocation.distanceTo(vectorTail);

        if (!towardsLocation)
            return false;

        float distance = (dx * (by0 - y0) - (bx0 - x0) * dy) / magnitude;
        if (distance < 0)
            distance *= -1;
        //System.out.println(String.format("D: %f RB(%f,%f), V1(%f, %f), V2(%f, %f)", distance, x0, y0, bx0, by0, bx1, by1));
        return (distance <= radius);
    }

    public boolean willCollideWithCircle(MapLocation location){
        return this.willCollideWithCircle(location.x, location.y);
    }

    public float getDamage(){
        return damage;
    }

    public void setDamage(float _damage){
        damage = _damage;
    }

    public float getX0(){
        return bx0;
    }
    public float getY0(){
        return by0;
    }
    public float getX1(){
        return bx1;
    }
    public float getY1(){
        return by1;
    }

}