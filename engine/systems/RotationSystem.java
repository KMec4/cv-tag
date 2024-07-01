package engine.systems;

import org.joml.Vector3f;

import engine.GameObject;

public class RotationSystem
{
    public static void init()
    {
        new Thread()
        {
            long start = System.nanoTime();

            @Override
            public void run()
            {
                for(;;)
                {
                    long now = System.nanoTime();
                    float diff = ( (float) now - start ) / 1000000;
                    start = now;
                    doActions(GameObject.getAllGameObjects(), diff);
                }
            }
        }.start();
    }

    public static void doActions(Iterable<GameObject> objects, float dt)
    {
        Vector3f toAdd = new Vector3f();

        for(GameObject obj : objects)
        {
            try
            {
                if(obj instanceof RotationSystemI)
                {
                    RotationSystemI rotateable = (RotationSystemI) obj;
                    rotateable.getRotSpeeds().mul(dt, toAdd);
                    rotateable.getRotation().add(toAdd);
                    
                }
            }
            catch (NullPointerException e)
            {
                
            }
        }
    }
}
