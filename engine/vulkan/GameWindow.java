package engine.vulkan;

import java.nio.LongBuffer;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkInstance;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;


public class GameWindow
{
    long window;
    long surface;

    int height = 1;
    int width = 1;
    String title;

    boolean isFullscreen = false;
    int maxRefreshRate = 30;

    private LinkedList<WindowEvent> windowEvents = new LinkedList<WindowEvent>();

    public GameWindow(String title)
    {
        glfwInit();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CURSOR, GLFW_CROSSHAIR_CURSOR);
        glfwWindowHint(GLFW_REFRESH_RATE, 1);

        window = glfwCreateWindow(width, height, title, 0L, 0L);

        glfwSetFramebufferSizeCallback(window, new WindowResizeEvent());
        new WindowEventProcess().start();
    }

    public void setSize(int x, int y)
    {
        height = y;
        width = x;
        glfwSetWindowSize(window, height, width);
    }

    public void destroy()
    {
        glfwDestroyWindow(window);
    }

    public void addWindowEvent(WindowEvent e)
    {
        windowEvents.add(e);
    }

    public void removeWindowEvent(WindowEvent e)
    {
        windowEvents.remove(e);
    }

    public long createSurface(VkInstance instance)
    {
        try(MemoryStack stack = stackPush())
        {
            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

            int result = glfwCreateWindowSurface(instance, window, null, pSurface);

            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Failed to create window surface: " + engine.VkResultDecoder.decode(result));
                throw e;
            }
            else
            {
                surface = pSurface.get(0);
            }
            return pSurface.get(0);
        }
    }

    public long getSurface()
    {
        return surface;
    }

    public VkExtent2D getSize()
    {
        return VkExtent2D.malloc()
        .height(height)
        .width(width);
    }

    public long getWindow()
    {
        return window;
    }


    private class WindowResizeEvent extends GLFWFramebufferSizeCallback
    {

        @Override
        public void invoke(long w, int x, int y)
        {
            if(window == w)
            {
                height = y;
                width = x;
                for( WindowEvent e : windowEvents)
                {
                    e.onResize();
                }
            }
        }

    }

    private class WindowEventProcess extends Thread
    {
        private void onClose()
        {
            for(WindowEvent e : windowEvents)
            {
                e.onClose();
            }
        }
        @Override
        public void run()
        {
            while (!glfwWindowShouldClose(window))
            {
                glfwSwapBuffers(window);
                glfwPollEvents();
                try
                {
                    for(WindowEvent e : windowEvents)
                    {
                        e.onRefresh();
                    }
                }
                catch(ConcurrentModificationException e)
                {
                }
            }
            onClose();
        }
    }


    public class WindowEvent
    {

        public WindowEvent()
        {
            addWindowEvent(this);
        }

        public void onRefresh()
        {
            return;
        }

        public void onClose()
        {
            return;
        }

        public void onResize()
        {
            return;
        }

    }
}