package engine._3d;

import org.joml.Vector3f;


public interface RenderSystemI3d
{
    Vector3f translation();
    Vector3f scale();
    Vector3f rotation();

    Vector3f color();
    Model3d  model();
}
