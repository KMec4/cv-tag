package engine.vulkan;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.GameObject;
import engine.systems.MovingSystemI;

public class Viewport extends GameObject implements MovingSystemI
{
    Matrix4f projectionMatrix = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f,
                                             0.0f, 1.0f, 0.0f, 0.0f,
                                             0.0f, 0.0f, 1.0f, 0.0f,
                                             0.0f, 0.0f,0.0f, 1.0f );

    Matrix4f viewMatrix       = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f,
                                             0.0f, 1.0f, 0.0f, 0.0f,
                                             0.0f, 0.0f, 1.0f, 0.0f,
                                             0.0f, 0.0f,0.0f, 1.0f );

    Vector3f pos = new Vector3f(0.0f, .0f, .0f);
    Vector3f dir = new Vector3f(0.0f, .0f, 1.0f);
    Vector3f up  = new Vector3f(.0f,  -1.f, .0f);

    Vector2f rotate = new Vector2f(.0f, .0f);

    float speed = .00f;


    public void setOrthographicProjection( float left, float right, float top, float bottom, float near, float far)
    {
        projectionMatrix = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );
        projectionMatrix.m00( 2.f / (right - left)              );
        projectionMatrix.m11( 2.f / (bottom - top)              );
        projectionMatrix.m22( 1.f / (far - near)                );
        projectionMatrix.m30( -(right + left) / (right - left)  );
        projectionMatrix.m31( -(bottom + top) / (bottom - top)  );
        projectionMatrix.m32( -near / (far - near)              );
    }

    public void setPerspectiveProjection(float fovy, float aspect, float near, float far)
    {
        //assert(glm::abs(aspect - std::numeric_limits<float>::epsilon()) > 0.0f);

        float tanHalfFovy = (float) StrictMath.tan(fovy / 2.f);
        projectionMatrix = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );
        projectionMatrix.m00( 1.f / (aspect * tanHalfFovy)  );
        projectionMatrix.m11( 1.f / (tanHalfFovy)           );
        projectionMatrix.m22( far / (far - near)            );
        projectionMatrix.m23( 1.f                       );
        projectionMatrix.m32( -(far * near) / (far - near)  );
    }

    public void setViewDirection()
    {
        viewMatrix.identity()
                .rotateX(rotate.x)
                .rotateY(rotate.y)
                .translate(-pos.x, -pos.y, -pos.z);
        /*Vector3f w = new Vector3f();
        dir.normalize(w);

        Vector3f u = new Vector3f();
        w.cross(up, u);
        u.normalize();

        Vector3f v = new Vector3f();
        w.cross(u, v);


        viewMatrix = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );

        viewMatrix.m00( u.x );
        viewMatrix.m10( u.y );
        viewMatrix.m20( u.z );
        viewMatrix.m01( v.x );
        viewMatrix.m11( v.y );
        viewMatrix.m21( v.z );
        viewMatrix.m02( w.x );
        viewMatrix.m12( w.y );
        viewMatrix.m22( w.z );
        viewMatrix.m30( u.dot(pos) );
        viewMatrix.m31( v.dot(pos) );
        viewMatrix.m32( w.dot(pos) );*/
      }
      
    public void setViewTarget(Vector3f target) //TODO target - pos != 0
    {
        Vector3f w = new Vector3f();
        target.sub(pos, w);
        w.normalize();

        Vector3f u = new Vector3f();
        w.cross(up, u);
        u.normalize();

        Vector3f v = new Vector3f();
        w.cross(u, v);


        viewMatrix = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );

        viewMatrix.m00( u.x );
        viewMatrix.m10( u.y );
        viewMatrix.m20( u.z );
        viewMatrix.m01( v.x );
        viewMatrix.m11( v.y );
        viewMatrix.m21( v.z );
        viewMatrix.m02( w.x );
        viewMatrix.m12( w.y );
        viewMatrix.m22( w.z );
        viewMatrix.m30( u.dot(pos) );
        viewMatrix.m31( v.dot(pos) );
        viewMatrix.m32( w.dot(pos) );
    }
      
      /*void LveCamera::setViewYXZ(glm::vec3 position, glm::vec3 rotation) {
        const float c3 = glm::cos(rotation.z);
        const float s3 = glm::sin(rotation.z);
        const float c2 = glm::cos(rotation.x);
        const float s2 = glm::sin(rotation.x);
        const float c1 = glm::cos(rotation.y);
        const float s1 = glm::sin(rotation.y);
        const glm::vec3 u{(c1 * c3 + s1 * s2 * s3), (c2 * s3), (c1 * s2 * s3 - c3 * s1)};
        const glm::vec3 v{(c3 * s1 * s2 - c1 * s3), (c2 * c3), (c1 * c3 * s2 + s1 * s3)};
        const glm::vec3 w{(c2 * s1), (-s2), (c1 * c2)};
        viewMatrix = glm::mat4{1.f};
        viewMatrix[0][0] = u.x;
        viewMatrix[1][0] = u.y;
        viewMatrix[2][0] = u.z;
        viewMatrix[0][1] = v.x;
        viewMatrix[1][1] = v.y;
        viewMatrix[2][1] = v.z;
        viewMatrix[0][2] = w.x;
        viewMatrix[1][2] = w.y;
        viewMatrix[2][2] = w.z;
        viewMatrix[3][0] = -glm::dot(u, position);
        viewMatrix[3][1] = -glm::dot(v, position);
        viewMatrix[3][2] = -glm::dot(w, position);
      }*/
      
    //GETTERS

    public Matrix4f getProjectionMatrix()
    {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix()
    {
        return viewMatrix;
    }

    @Override
    public Vector3f getDirection() //here the direction can be altered
    {
        return viewMatrix.positiveZ(dir).negate();
    }

    @Override
    public Vector3f getPosition() //and the position too
    {
        return pos;
    }

    @Override
    public float getSpeedM_S()
    {
        return -speed; //speed;
    }

    //SETTERS

    public void setSpeedM_S(float speed)
    {
        this.speed = speed;
    }

    public void setRotation(Vector2f rot)
    {
        rotate.add(rot);
    }
    
}
