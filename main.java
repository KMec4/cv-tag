import org.lwjgl.system.Configuration;

import engine.Vulkan;
import engine._3d.pickingSystem.Pick3d;
import engine.systems.MovingSystem;
import engine.systems.RotationSystem;
import engine.systems.UserInputSystem;
import res.schach.Schachbrett;

public class main
{
    static
    {
        Configuration.STACK_SIZE.set(2048);
    }

    public static void main(String[] args)
    {
        Vulkan engine = Vulkan.createDefaultInstance();
        UserInputSystem ui = new UserInputSystem();
        ui.init(engine.getWindow(), engine.getViewport());
        ui.linkPick3d(new Pick3d(engine.getDevice(), engine.getWindow(), engine.getViewport()));

        new Schachbrett();

        engine.allowRendering(true);
        MovingSystem.init();
        RotationSystem.init();

        System.err.println("main ready");

        while (true)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }            
        }
    }

}
