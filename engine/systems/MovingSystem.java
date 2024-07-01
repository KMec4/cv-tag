package engine.systems;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import org.joml.Vector3f;

import engine.GameObject;

public class MovingSystem
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
                if(obj instanceof MovingSystemI)
                {
                    MovingSystemI moveable = (MovingSystemI) obj;
                    moveable.getDirection().mul(moveable.getSpeedM_S() * dt, toAdd);
                    moveable.getPosition().add( toAdd );
                }
            }
            catch (NullPointerException e)
            {
                
            }
        }
    }
}
