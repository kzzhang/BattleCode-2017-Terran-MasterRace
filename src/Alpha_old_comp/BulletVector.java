package Alpha_old_comp;

import battlecode.common.BulletInfo;
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
        init (info.getLocation().x, info.getLocation().y, projection.x, projection.y, info.getDamage(), _radius);
    }

    private void init(float _bx0, float _by0, float _bx1, float _by1, float _damage, float _radius){
        bx0 = _bx0;
        by0 = _by0;
        bx1 = _bx1;
        by1 = _by1;

        vectorOrigin = new MapLocation(bx0, by0);
        vectorTail = new MapLocation(bx1, by1);

        dx = bx1 - bx0;
        dy = by1 - by0;
        magnitude = (float) Math.sqrt((float)(Math.pow(dx, 2) + Math.pow(dy, 2)));
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