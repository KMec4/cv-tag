package engine.systems;

import org.joml.Vector3f;

public interface MovingSystemI
{
    public Vector3f getDirection();
    public Vector3f getPosition();
    public float getSpeedM_S();
}
