package engine;

import org.magicwerk.brownies.collections.BigList;

public class GameObject
{
    //static
    static private int lastObjId = 0;
    static private BigList<GameObject> gameObjects = new BigList<GameObject>();

    // object
    public final int id = lastObjId++;

    public GameObject()
    {
        if(lastObjId > Integer.MAX_VALUE - 4)
        {
            RuntimeException e = new RuntimeException("Run out of GameObject indexes!!!");
            throw e;
        }
        System.out.print(" Object " + id + " has been created, implemented Systems: ");
        if( getClass().getInterfaces().length <= 0)
        {
            System.out.println("none");
        }
        else
        {
            for(Class c : getClass().getInterfaces())
            {
                System.out.println(c.getName() + " ,");
            }
            System.out.println("");
        }
        gameObjects.add(id, this);
    }

    public static BigList<GameObject> getAllGameObjects()
    {
        return gameObjects;
    }

    public static void main(String[] args)
    {
        for(;;new GameObject());
    }
}
