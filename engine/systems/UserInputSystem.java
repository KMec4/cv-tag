package engine.systems;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import engine._3d.pickingSystem.Pick3d;
import engine.vulkan.GameWindow;
import engine.vulkan.Viewport;

public class UserInputSystem
{

    Viewport player;
    Pick3d select = null;
    GameWindow w;

    public void init(GameWindow window, Viewport v)
    {
        player = v;
        w = window;
        glfwSetKeyCallback(window.getWindow(), new Callback());
    }

    public void linkPick3d(Pick3d instance)
    {
        select = instance;
    }

    private class Callback implements GLFWKeyCallbackI
    {

        @Override
        public void invoke(long window, int key, int scancode, int action, int mods)
        {
            Vector2f rotation = new Vector2f(0f, 0f);
            switch(key)
            {
                case GLFW_KEY_W:
                    if(action == GLFW_RELEASE)
                    {
                        player.setSpeedM_S(0f);
                    }
                    else
                    {
                        player.setSpeedM_S(0.015f);
                    }
                    break;
                case GLFW_KEY_S:
                    if(action == GLFW_RELEASE)
                    {
                        player.setSpeedM_S(0f);
                    }
                    else
                    {
                        player.setSpeedM_S(-0.015f);
                    }
                    break;
                case GLFW_KEY_DOWN:
                    rotation.x += 0.01f;
                    break;
                case GLFW_KEY_UP:
                    rotation.x -= 0.01f;
                    break;
                case GLFW_KEY_LEFT:
                    rotation.y += 0.01f;
                    break;
                case GLFW_KEY_RIGHT:
                    rotation.y -= 0.01f;
                    break;
                case GLFW_KEY_ENTER:
                    if(select != null)
                    {
                        select.action(w);
                    }
                    else
                    {
                        System.out.println("no picker");
                    }
                    break;

            }

            
            if(rotation.dot(rotation) > Float.MIN_NORMAL)
            {
                player.setRotation(rotation);
            }
        }
        
    }
    
}