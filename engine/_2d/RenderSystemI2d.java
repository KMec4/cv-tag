package engine._2d;

import org.joml.Vector2f;
import org.joml.Vector3f;

public interface RenderSystemI2d
{
    Vector2f translation();
    Vector2f scale();
    float    rotation();

    Vector3f color();
    Model2d  model();
}
