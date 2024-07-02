package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;


import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;

import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.vulkan.VK10.*;

import engine._2d.RenderSystem2d;
import engine._3d.RenderSystem3d;
import engine.vulkan.FrameRenderer;
import engine.vulkan.Device;
import engine.vulkan.GameWindow;
import engine.vulkan.Viewport;
import engine.vulkan.Device.PhysicalDeviceBlueprint;

import static org.lwjgl.glfw.GLFW.*;

public class Vulkan
{

    public static class VulkanProperties
    {
        public boolean ENABLE_VALIDATION_LAYERS;
        public Set<String> VALIDATION_LAYERS;
        public Set<String> device_extentions;

        public int PipelineType = 0;
    }

    //static
    public static VulkanProperties getDefaultProperties()
    {
        String[] dLayers =
        {
            "VK_LAYER_KHRONOS_validation"
        };

        String[] dExtentions =
        {
            VK_KHR_SWAPCHAIN_EXTENSION_NAME,

        };

        return new VulkanProperties()
        {
            {
                ENABLE_VALIDATION_LAYERS = true;
                device_extentions = new HashSet<String>( Arrays.asList(dExtentions) );
                VALIDATION_LAYERS = new HashSet<String>( Arrays.asList(dLayers) );
            }
        };
    }

    public static Vulkan createDefaultInstance()
    {
        return new Vulkan(getDefaultProperties());
    }

    //Objective
    
    VkInstance instance;
    GameWindow w;
    long surface;
    Device logical;
    FrameRenderer cmd;
    boolean renderActive = false;

    VulkanProperties p;

    boolean windowResizedFlag = false;

    public Vulkan(VulkanProperties p)
    {
        this.p = p;

        glfwInit();
        createInstance();
        createCompatibleGLFWWindow(1600, 800);
        createLogicalDevice(null);

        cmd = new FrameRenderer(logical, w);

        RenderSystem3d.init(logical, cmd.getRenderPass());
    }

    //BASIC STUFF
    private void createInstance()
    {
        try(MemoryStack stack = stackPush())
        {
            VkApplicationInfo appInfo = VkApplicationInfo.callocStack(stack);

            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8Safe("AnotherStar"));
            appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.pEngineName(stack.UTF8Safe("No Engine"));
            appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.apiVersion(VK_API_VERSION_1_0);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(appInfo);
            
            //GLFW SUPPORT
            if(!glfwVulkanSupported())
            {
                RuntimeException e = new RuntimeException("Vulkan is not supported on this machien");
                throw e;
            }
            else
            {
                createInfo.ppEnabledExtensionNames(glfwGetRequiredInstanceExtensions());
            }

            //VALIDATION LAYERS
            if(!p.VALIDATION_LAYERS.isEmpty())
            {
                PointerBuffer layers = stack.mallocPointer(p.VALIDATION_LAYERS.size());
                for(String s : p.VALIDATION_LAYERS)
                {
                    layers.put(stack.UTF8(s));
                }

                createInfo.ppEnabledLayerNames(layers.rewind());
            }

            // >>>> INSTANCE CREATION <<<<
            PointerBuffer instancePtr = stack.mallocPointer(1);
            int result = vkCreateInstance(createInfo, null, instancePtr);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Failed to create instance: " + VkResultDecoder.decode(result));
                throw e;
            }
            instance = new VkInstance(instancePtr.get(0), createInfo);

        }
    }

    public void createCompatibleGLFWWindow(int x, int y)
    {

        w = new GameWindow("Chess");
        w.setSize(600, 480);
        w.new WindowEvent()
        {
            int i = 0;

            @Override
            public void onRefresh()
            {
                if(renderActive)
                {
                    if(cmd == null)
                    {
                        return;
                    }
                    cmd.renderFrame();
                }
            }
            @Override
            public void onClose()
            {
                System.out.println("Close");
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {}
                System.exit(0);
            }
            @Override
            public void onResize()
            {
                System.out.println("Resize");
                windowResizedFlag = true;
            }
        };

        surface = w.createSurface(instance);

    }

    public void createLogicalDevice(String dev)
    {
        ArrayList<PhysicalDeviceBlueprint> devs = Device.listSuitableDevices(instance, surface, p.device_extentions);
        //for(PhysicalDeviceBlueprint pd : devs)
        {
           // System.out.println(pd.getDetails());
        }
        //if(dev.equals("") || dev == null)
        {
            logical = new Device(devs.get(0), surface);
            System.out.println("Selected Device: " + logical.getDeviceName());
            //System.out.println(devs.get(0).getDetails());
        }
        //else
        {
            //logical = new Device(dev);
        }
    }

    public Viewport getViewport()
    {
        return cmd.getViewport();
    }

    public GameWindow getWindow()
    {
        return w;
    }

    public Device getDevice()
    {
        return logical;
    }

    public void allowRendering(boolean render)
    {
        renderActive = render;
    }
}