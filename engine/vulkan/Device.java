package engine.vulkan;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import engine.VkResultDecoder;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

public class Device
{

    //private class
    public static class SwapChainSupportDetails
    {
        public VkSurfaceCapabilitiesKHR capabilities;
        public VkSurfaceFormatKHR.Buffer formats;
        public IntBuffer presentModes;

        public boolean isAdequate()
        {
            return formats.hasRemaining() && presentModes.hasRemaining();
        }
        
    }

    public static class QueueFamilyIndices
    {
        // We use Integer to use null as the empty value
        public Integer graphicsFamily;
        public Integer presentFamily;

        private boolean isComplete()
        {
            return graphicsFamily != null && presentFamily != null;
        }

        public int[] unique()
        {
            return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
        }

        public int[] array()
        {
            return new int[] {graphicsFamily, presentFamily};
        }
    }

    public static class PhysicalDeviceBlueprint implements java.lang.Cloneable
    {
        static final int NOT_CHECKT = 0, FALSE = 2, TRUE = 1;

        public String deviceName;
        public Set<String> supportedExtentions = new HashSet<String>();
        public Set<String> neededExtentions = new HashSet<String>();
        public VkPhysicalDevice device;
        public QueueFamilyIndices indices;
        public SwapChainSupportDetails swapChainSupportDetails;
        public int compatible = 0;

        public String getDetails()
        {
            String ret = "VkPhysicalDevice_\nName:" + deviceName +"\n";
            for(String s : supportedExtentions)
            {
                ret += s + "\n";
            }
            ret += "QueueFamilyIndicesComplete: " + indices.isComplete();
            ret += "\nSwapChainSupport: " + swapChainSupportDetails.isAdequate();
            ret += "\n\n => COMPATIBLE: ";
            switch (compatible)
            {
                case NOT_CHECKT:
                    ret += "NOT_CHECKT";
                    break;
                case FALSE:
                    ret += "FALSE";
                    break;
                case TRUE:
                    ret += "TRUE";
                    break;
                default:
                    ret += "NOT_CHECKT";
                    break;
            }
            return ret;
        }

        public PhysicalDeviceBlueprint clone()
        {
            PhysicalDeviceBlueprint cloned = new PhysicalDeviceBlueprint();
            cloned.deviceName               = deviceName;
            cloned.supportedExtentions      = new HashSet<String>(supportedExtentions);
            cloned.neededExtentions         = new HashSet<String>(neededExtentions);
            cloned.device                   = device;
            cloned.indices                  = indices;
            cloned.swapChainSupportDetails  = swapChainSupportDetails;
            cloned.compatible               = compatible;
            return cloned;
        }
    }

    //static

    private static PointerBuffer getDeviceExtensionsAsPointerBuffer(Set<String> extentions, MemoryStack stack)
    {
        PointerBuffer layers = stack.mallocPointer(extentions.size());
        for(String s : extentions)
        {
            layers.put(stack.UTF8(s));
        }
        return layers.rewind();
    }

    private static SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, long surface)
    {
        SwapChainSupportDetails details = new SwapChainSupportDetails();

        try(MemoryStack stack = stackPush())
        {
            details.capabilities = VkSurfaceCapabilitiesKHR.malloc(); // mallocStack(stack);
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);

            IntBuffer count = stack.ints(0);

            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

            if(count.get(0) != 0)
            {
                details.formats = VkSurfaceFormatKHR.malloc(count.get(0)); //mallocStack(count.get(0), stack);
                vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats);
            }

            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

            if(count.get(0) != 0)
            {
                IntBuffer presentModes = stack.mallocInt(count.get(0));//IntBuffer.allocate(count.get(0)); 
                vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, presentModes);
                int len = count.get(0);

                int[] modes = new int[len];
                for(int i = 0;presentModes.hasRemaining(); i++)
                {
                    modes[i] = presentModes.get();
                }
                details.presentModes = IntBuffer.wrap(modes);
            }
        }

        return details;
    }

    private static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface)
    {
        QueueFamilyIndices indices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush())
        {
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for(int i = 0;i < queueFamilies.capacity() || !indices.isComplete();i++)
            {
                if((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
                {
                    indices.graphicsFamily = i;
                }

                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                if(presentSupport.get(0) == VK_TRUE)
                {
                    indices.presentFamily = i;
                }
            }

            return indices;
        }
    }

    private static boolean doesDeviceSupportExtensions(PhysicalDeviceBlueprint blueprint) //check with the last calls extensions
    {
        try(MemoryStack stack = stackPush())
        {
            IntBuffer count = stack.ints(0);

            vkEnumerateDeviceExtensionProperties(blueprint.device, (String)null, count, null);
            VkExtensionProperties.Buffer allExtensions = VkExtensionProperties.mallocStack(count.get(0), stack);
            vkEnumerateDeviceExtensionProperties(blueprint.device, (String)null, count, allExtensions);

            Set<String> needed = new HashSet<String>( blueprint.neededExtentions );
            for( VkExtensionProperties p : allExtensions)
            {
                needed.remove(p.extensionNameString());
                blueprint.supportedExtentions.add(p.extensionNameString());
            }
            return needed.isEmpty();
        }
    }

    private static PointerBuffer listDev(VkInstance instance, MemoryStack stack)
    {
        IntBuffer deviceCount = stack.ints(0);

        vkEnumeratePhysicalDevices(instance, deviceCount, null); //check for devices with vulkan
        if(deviceCount.get(0) == 0)
        {
            RuntimeException e = new RuntimeException("No GPUs with Vulkan support installed");
            throw e;
        }
        else
        {
            System.out.println( "-> Vulkan supporting GPUs: [count] " + deviceCount.get(0));
        }

        PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
        vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);    //check for devices supporting special extentions
        return ppPhysicalDevices;
    }

    public static ArrayList<PhysicalDeviceBlueprint> listSuitableDevices(
        VkInstance instance,
        long surface,
        Set<String> extentions
        )
    {
        try(MemoryStack stack = stackPush())
        {
            ArrayList<PhysicalDeviceBlueprint> ret = new ArrayList<PhysicalDeviceBlueprint>();
            PointerBuffer ppPhysicalDevices = listDev(instance, stack);

            for(int i = 0;i < ppPhysicalDevices.capacity();i++) 
            {
                PhysicalDeviceBlueprint blueprint = new PhysicalDeviceBlueprint();
                blueprint.neededExtentions = extentions;

                blueprint.device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);

                VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
                vkGetPhysicalDeviceProperties(blueprint.device, properties);
                blueprint.deviceName = properties.deviceNameString();

                //Check Devices' suitability
                blueprint.compatible = PhysicalDeviceBlueprint.FALSE;

                if(doesDeviceSupportExtensions(blueprint))
                {
                    blueprint.indices = findQueueFamilies(blueprint.device, surface);
                    blueprint.swapChainSupportDetails = querySwapChainSupport(blueprint.device, surface);

                    boolean swapChainAdequate = false;
                    swapChainAdequate = blueprint.swapChainSupportDetails.formats.hasRemaining() && blueprint.swapChainSupportDetails.presentModes.hasRemaining();

                    if(blueprint.indices.isComplete() && swapChainAdequate)
                    {
                        ret.add(blueprint);
                        blueprint.compatible = PhysicalDeviceBlueprint.TRUE;
                    }
                }
            }
            return ret;
        }
    }

    public static ArrayList<PhysicalDeviceBlueprint> listDevices(
        VkInstance instance,
        long surface
        )
    {
        try(MemoryStack stack = stackPush())
        {
            ArrayList<PhysicalDeviceBlueprint> ret = new ArrayList<PhysicalDeviceBlueprint>();
            PointerBuffer ppPhysicalDevices = listDev(instance, stack);

            for(int i = 0;i < ppPhysicalDevices.capacity();i++) 
            {
                PhysicalDeviceBlueprint blueprint = new PhysicalDeviceBlueprint();

                blueprint.device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);

                VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
                vkGetPhysicalDeviceProperties(blueprint.device, properties);
                blueprint.deviceName = properties.deviceNameString();

                doesDeviceSupportExtensions(blueprint); // TO list extentions
                blueprint.indices = findQueueFamilies(blueprint.device, surface);
                blueprint.swapChainSupportDetails = querySwapChainSupport(blueprint.device, surface);
                ret.add(blueprint);
            }
            return ret;
        }
    }    
    


    //Var

    private PhysicalDeviceBlueprint device;
    private VkDevice logical;
    private long surface;
    private long commandPool;

    private VkQueue graphicsQueue;
    private VkQueue presentQueue ;


    //Constructer
    public Device(PhysicalDeviceBlueprint dev, long surface)
    {
        this.surface = surface;
        device = (PhysicalDeviceBlueprint) dev.clone();

        if(surface == VK_FALSE)
        {
            RuntimeException e = new RuntimeException("Surface has not been created");
            throw e;
        } // Maybe check alse dev

        try(MemoryStack stack = stackPush())
        {
            

            //IntBuffer familyCount = stack.ints(0);


            // ===> QUEUE FAMILIES <===

            int[] queueFamilies = IntStream.of( device.indices.graphicsFamily, device.indices.presentFamily ).distinct().toArray();
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(queueFamilies.length, stack);


            for (int i = 0;i < queueFamilies.length;i++)
            {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType           ( VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO );
                queueCreateInfo.queueFamilyIndex( queueFamilies[i]     );
                queueCreateInfo.pQueuePriorities( stack.floats(1.0f) );
            }

            // >>>> LOGICAL DEVICE CREATION <<<<
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack);
            //deviceFeatures.samplerAnisotropy(true);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.callocStack(stack)
            .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
            .pQueueCreateInfos(queueCreateInfos)
            .pEnabledFeatures(deviceFeatures)
            .ppEnabledExtensionNames(getDeviceExtensionsAsPointerBuffer(device.neededExtentions, stack));

            //TODO validationLayers
            /*
            if (enableValidationLayers)
            {
                createInfo.enabledLayerCount = static_cast<uint32_t>(validationLayers.size());
                createInfo.ppEnabledLayerNames = validationLayers.data();
            }
            else
            {
                createInfo.enabledLayerCount = 0;
            }
            */

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);
            int result = vkCreateDevice(device.device, createInfo, null, pDevice );
            if( result != VK_SUCCESS )
            {
                RuntimeException e = new RuntimeException("Failed to create logical device: " + VkResultDecoder.decode(result));
                throw e;
            }
            logical = new VkDevice(pDevice.get(0), device.device, createInfo);

            // ===> Create Command Pool <===

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType            ( VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO  );
            poolInfo.queueFamilyIndex ( dev.indices.graphicsFamily                  );
            poolInfo.flags            ( VK_COMMAND_POOL_CREATE_TRANSIENT_BIT | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

            LongBuffer commandPool = stack.callocLong(1);
            result = vkCreateCommandPool( logical, poolInfo, null, commandPool);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create command pool: " + VkResultDecoder.decode(result));
                throw e;
            }
            this.commandPool = commandPool.get(0);


            PointerBuffer gra = stack.callocPointer(1);
            PointerBuffer pre = stack.callocPointer(1);

            vkGetDeviceQueue(logical, device.indices.graphicsFamily, 0, gra );
            vkGetDeviceQueue(logical, device.indices.presentFamily , 0, pre );

            graphicsQueue = new VkQueue(gra.get(0), logical);
            presentQueue  = new VkQueue(pre.get(0), logical);
        }

    }

    //Methode
    int findMemoryType(int typeFilter, int properties, MemoryStack stack)
    {
        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.callocStack(stack);
        vkGetPhysicalDeviceMemoryProperties(device.device, memProperties);
        for (int i = 0; i < memProperties.memoryTypeCount(); i++)
        {
            if ( (((typeFilter >> i) & 1) == 1 ) && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties)
            {
                return i;
            }
        }
        RuntimeException e = new RuntimeException("failed to find suitable memory type!");
        throw e;
    }

    int findSupportedFormat( int[] candidates, int tiling, int features)
    {
        try(MemoryStack stack = stackPush())
        {
            for (int format : candidates)
            {
                VkFormatProperties props = VkFormatProperties.callocStack(stack);
                vkGetPhysicalDeviceFormatProperties(device.device, format, props);

                if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features)
                {
                    return format;
                }
                else if ( tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features)
                {
                    return format;
                }
            }
        }
        RuntimeException e = new RuntimeException("failed to find supported format!");
        throw e;
}

    public void createImageWithInfo( VkImageCreateInfo imageInfo, int memProperties, LongBuffer image, LongBuffer imageMemory)
    {
        try(MemoryStack stack = stackPush())
        {
            int result = vkCreateImage(logical, imageInfo, null, image);
            if (result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create image: " + VkResultDecoder.decode(result));
                throw e;
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.callocStack(stack);
            vkGetImageMemoryRequirements(logical , image.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType           ( VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO );
            allocInfo.allocationSize  ( memRequirements.size()                 );
            allocInfo.memoryTypeIndex ( findMemoryType(memRequirements.memoryTypeBits(), memProperties, stack));

            result = vkAllocateMemory( logical, allocInfo, null, imageMemory);
            if ( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to allocate image memory: " + VkResultDecoder.decode(result));
                throw(e);
            }

            result = vkBindImageMemory(logical, image.get(0), imageMemory.get(0), 0);
            if (result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to bind image memory: " + VkResultDecoder.decode(result));
                throw e;
            }
        }
    }

    public void createBuffer( long size, int usage, int properties, LongBuffer buffer, LongBuffer bufferMemory)
    {
        try(MemoryStack stack = stackPush())
        {
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
            bufferInfo.sType      (VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size       (size                                );
            bufferInfo.usage      (usage                               );
            bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE           );

            int result = vkCreateBuffer(getLogical(), bufferInfo, null, buffer);
            if (result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create vertex buffer: " + VkResultDecoder.decode(result));
                throw e;
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
            vkGetBufferMemoryRequirements(getLogical(), buffer.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
            allocInfo.sType          ( VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO );
            allocInfo.allocationSize ( memRequirements.size()                 );
            allocInfo.memoryTypeIndex( findMemoryType(memRequirements.memoryTypeBits() , properties, stack ));
            
            result = vkAllocateMemory(getLogical(), allocInfo, null, bufferMemory);
            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to allocate vertex buffer memory: " + VkResultDecoder.decode(result) );
                throw e;
            }

            vkBindBufferMemory(getLogical(), buffer.get(0), bufferMemory.get(0), 0);
        }
    }

    public void copyBuffer(long srcBuffer, long dstBuffer, int size)
    {
        try(MemoryStack stack = stackPush())
        {
            VkCommandBuffer commandBuffer = beginSingleTimeCommands(stack);

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
            copyRegion.srcOffset ( 0 );  // Optional
            copyRegion.dstOffset ( 0 );  // Optional
            copyRegion.size      ( size );
            vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);

            endSingleTimeCommands(commandBuffer, stack);
        }
    }

    public void copyImageToBuffer(long srcImg, long dstBuf, int width, int height, int layerCount, int format)
    {
        try(MemoryStack stack = stackPush())
        {
            VkCommandBuffer cmd = beginSingleTimeCommands(stack);

            VkBufferImageCopy.Buffer copyRegion = VkBufferImageCopy.calloc(1, stack);
            copyRegion.bufferOffset      ( 0 );
            copyRegion.bufferRowLength   ( 0 );
            copyRegion.bufferImageHeight ( 0 );

            copyRegion.imageSubresource().aspectMask     ( VK_IMAGE_ASPECT_COLOR_BIT );
            copyRegion.imageSubresource().mipLevel       ( 0 );
            copyRegion.imageSubresource().baseArrayLayer ( 0 );
            copyRegion.imageSubresource().layerCount     ( layerCount );

            copyRegion.imageOffset().set( 0, 0, 0 );
            copyRegion.imageExtent().set( width, height, 1);

            vkCmdCopyImageToBuffer(cmd, srcImg, format, dstBuf, copyRegion);

            endSingleTimeCommands(cmd, stack);
        }
    }

    VkCommandBuffer beginSingleTimeCommands(MemoryStack stack)
    {
        VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
        allocInfo.sType             ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO );
        allocInfo.level             ( VK_COMMAND_BUFFER_LEVEL_PRIMARY );
        allocInfo.commandPool       ( commandPool );
        allocInfo.commandBufferCount(  1 );
      
        VkCommandBuffer commandBuffer;
        PointerBuffer cmd = stack.callocPointer(1);
        vkAllocateCommandBuffers(logical, allocInfo, cmd);
        commandBuffer = new VkCommandBuffer(cmd.get(0), logical);
      
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
        beginInfo.sType( VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO );
        beginInfo.flags( VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT );
      
        vkBeginCommandBuffer(commandBuffer, beginInfo);
        return commandBuffer;
    }

    void endSingleTimeCommands(VkCommandBuffer commandBuffer, MemoryStack stack)
    {
        vkEndCommandBuffer(commandBuffer);

        VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
        submitInfo.sType              ( VK_STRUCTURE_TYPE_SUBMIT_INFO );
        submitInfo.pCommandBuffers    ( stack.pointers(commandBuffer.address()) );

        vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE);
        vkQueueWaitIdle(graphicsQueue);

        vkFreeCommandBuffers(logical, commandPool, commandBuffer);
    }
    // ==== GETTERS ====

    public String getDeviceName()
    {
        return device.deviceName;
    }

    public Set<String> getSupportedExtentions()
    {
        return device.supportedExtentions;
    }

    public VkPhysicalDevice getPhysicalDevice()
    {
        return device.device;
    }

    public QueueFamilyIndices getGraphicsFamilyIndices()
    {
        return device.indices;
    }

    public SwapChainSupportDetails getSwapChainSupportDetails()
    {
        device.swapChainSupportDetails = querySwapChainSupport(device.device, surface);
        return device.swapChainSupportDetails;
    }

    public long getSurface()
    {
        return surface;
    }

    public VkDevice getLogical()
    {
        return logical;
    }

    public long getCommandPool()
    {
        return commandPool;
    }

    public VkQueue getPresentQueue()
    {
        return presentQueue;
    }
    
    public VkQueue getGraphicsQueue()
    {
        return graphicsQueue;
    }


}
