package engine.vulkan;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryStack.stackCallocLong;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import engine.VkResultDecoder;

public class Swapchain
{
    static int MAX_FRAMES_IN_FLIGHT = 5;

    long pSwapchain;
    long pRenderpass;
    VkExtent2D windowExtent;
    Device dev;

    ArrayList<Long> depthImageMemorys   = new ArrayList<Long>();
    ArrayList<Long> depthImageViews     = new ArrayList<Long>();
    ArrayList<Long> swapChainImageViews = new ArrayList<Long>();
    ArrayList<Long> swapChainFrameBuffers = new ArrayList<Long>();

    int swapChainImageFormat;
    VkExtent2D swapChainExtent;

    //ArrayList<Long> imageAvailableSemaphores = new ArrayList<Long>();
    //ArrayList<Long> renderFinishedSemaphores = new ArrayList<Long>();
    //ArrayList<Long> inFlightFences           = new ArrayList<Long>();
    //ArrayList<Long> imagesInFlight           = new ArrayList<Long>();;

    HashMap<Integer, Long> imageAvailableSemaphores = new HashMap<Integer, Long>();
    HashMap<Integer, Long> renderFinishedSemaphores = new HashMap<Integer, Long>();
    HashMap<Integer, Long> inFlightFences           = new HashMap<Integer, Long>();
    HashMap<Integer, Long> imagesInFlight           = new HashMap<Integer, Long>();;

    //VkImage.Buffer depthImage;
    //VkDeviceMemory.Buffer depthImageMemorys;
    //VkImageView.Buffer depthImageViews;
    //VkImage.Buffer swapChainImages;
    //VkImageView.Buffer swapChainImageViews;

    public Swapchain (Device dev, VkExtent2D windowExtent)
    {
        this(dev, windowExtent, null);
    }

    public Swapchain (Device dev, VkExtent2D windowExtent, Swapchain previous)
    {
        this.dev = dev;
        this.windowExtent = windowExtent;
        recreateSwapchain(dev, previous);

        if(previous != null) previous.clean();

        createImageViews(dev);
        createRenderPass(dev);
        createDepthResources(dev);
        createFramebuffers(dev);
        createSyncObjects(dev);
    }

    // ===> SwapChainCreation <===
    private void recreateSwapchain(Device dev, Swapchain previous)
    {
        Device.SwapChainSupportDetails swapChainSupport = dev.getSwapChainSupportDetails();
        Device.QueueFamilyIndices indices               = dev.getGraphicsFamilyIndices();
          
        VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
        int presentMode                  = chooseSwapPresentMode  (swapChainSupport.presentModes);
        VkExtent2D extent                = chooseSwapExtent       (swapChainSupport.capabilities);
        
        int imageCount = swapChainSupport.capabilities.minImageCount() + 1;

        if (swapChainSupport.capabilities.maxImageCount() > 0 && imageCount > swapChainSupport.capabilities.maxImageCount())
        {
            imageCount = swapChainSupport.capabilities.maxImageCount();
        }
        
        try(MemoryStack stack = stackPush())
        {
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
            createInfo.sType   ( VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR );
            createInfo.surface ( dev.getSurface()                            );
            
            createInfo.minImageCount    ( imageCount                         );
            createInfo.imageFormat      ( surfaceFormat.format()             );
            createInfo.imageColorSpace  ( surfaceFormat.colorSpace()         );
            createInfo.imageExtent      ( extent                             );
            createInfo.imageArrayLayers ( 1                            );
            createInfo.imageUsage       ( VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
            
            IntBuffer queueFamilyIndices = stack.ints(indices.graphicsFamily, indices.presentFamily);
            
            if (!indices.graphicsFamily.equals(indices.presentFamily))
            {
              createInfo.imageSharingMode      (VK_SHARING_MODE_CONCURRENT);
              createInfo.queueFamilyIndexCount (2                   );
              createInfo.pQueueFamilyIndices   (queueFamilyIndices        );
            }
            else
            {
              createInfo.imageSharingMode      (VK_SHARING_MODE_EXCLUSIVE);
              createInfo.queueFamilyIndexCount (0                  );  // Optional
              createInfo.pQueueFamilyIndices   (null               );  // Optional
            }

            createInfo.preTransform   ( swapChainSupport.capabilities.currentTransform() );
            createInfo.compositeAlpha ( VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR                );
            createInfo.presentMode    ( presentMode                                      );
            createInfo.clipped        ( true                                       );

            if(previous == null)
            {
                createInfo.oldSwapchain( VK_NULL_HANDLE );
            }
            else
            {
                createInfo.oldSwapchain( previous.getSwapChain() );
            }

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

            int result = vkCreateSwapchainKHR(dev.getLogical(), createInfo, null, pSwapChain);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Failed to create swap chain: " + VkResultDecoder.decode(result));
                throw e;
            }
            pSwapchain = pSwapChain.get(0);

            swapChainImageFormat = surfaceFormat.format();
            swapChainExtent = VkExtent2D.calloc();
            swapChainExtent.set(extent);
        }
    }

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats)
    {
        for (VkSurfaceFormatKHR availableFormat : availableFormats)
        {
            if (availableFormat.format() == VK_FORMAT_B8G8R8A8_SRGB && availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
            {
            return availableFormat;
            }
        }
        return availableFormats.get(0);
    }

    private int chooseSwapPresentMode( IntBuffer availablePresentModes )
    {
        boolean supportImmediate = false;
        boolean supportMailbox   = false;
        boolean supportFifo      = false;

        for (int i = 0; i < availablePresentModes.limit(); i++)
        {
            switch (availablePresentModes.get(i))
            {
                case VK_PRESENT_MODE_MAILBOX_KHR:
                    supportMailbox = true;
                    break;
                case VK_PRESENT_MODE_IMMEDIATE_KHR:
                    //supportImmediate = true;
                    break;
                case VK_PRESENT_MODE_FIFO_KHR:
                    supportFifo = true;
                    break;
                default:
                    break;
            }
        }

        if(supportMailbox)
        {
            System.out.println("SwapChainPresentMode: Mailbox");
            return VK_PRESENT_MODE_MAILBOX_KHR;
        }
        else if(supportImmediate)
        {
            System.out.println("SwapChainPresentMode: Immediate");
            return VK_PRESENT_MODE_IMMEDIATE_KHR;
        }
        else if(supportFifo)
        {
            System.out.println("SwapChainPresentMode: V-sync");
            return VK_PRESENT_MODE_FIFO_KHR;
        }
        else
        {
            RuntimeException e = new RuntimeException("Failed to find supported Present Mode");
            throw e;
        }
    }

    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities)
    {
        if (capabilities.currentExtent().width() != Integer.MAX_VALUE)
        {
            return capabilities.currentExtent();
        }
        else
        {
            VkExtent2D actualExtent = windowExtent;
            actualExtent.width(  Integer.max( capabilities.minImageExtent().width(),
                                              Integer.min(capabilities.maxImageExtent().width(), actualExtent.width())
                                            ));
            actualExtent.height( Integer.max( capabilities.minImageExtent().height(),
                                              Integer.min(capabilities.maxImageExtent().height(), actualExtent.height())
                                            ));
            return actualExtent;
        }
    }

    // ===> ImageViews <===

    void createImageViews(Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
            // we only specified a minimum number of images in the swap chain, so the implementation is
            // allowed to create a swap chain with more. That's why we'll first query the final number of
            // images with vkGetSwapchainImagesKHR, then resize the container and finally call it again to
            // retrieve the handles.

            IntBuffer imageCountQuery = stack.callocInt(1);
            vkGetSwapchainImagesKHR(dev.getLogical(), pSwapchain, imageCountQuery, null);

            LongBuffer images = stackCallocLong(imageCountQuery.get(0));
            vkGetSwapchainImagesKHR(dev.getLogical(), pSwapchain, imageCountQuery, images);
            
            for (;images.hasRemaining();)
            {
                VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
                viewInfo.sType    (VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO );
                viewInfo.image    (images.get()                             );
                viewInfo.viewType (VK_IMAGE_VIEW_TYPE_2D                    );
                viewInfo.format   (swapChainImageFormat                     );
                viewInfo.subresourceRange().aspectMask     (VK_IMAGE_ASPECT_COLOR_BIT);
                viewInfo.subresourceRange().baseMipLevel   (0         );
                viewInfo.subresourceRange().levelCount     (1         );
                viewInfo.subresourceRange().baseArrayLayer (0         );
                viewInfo.subresourceRange().layerCount     (1         );
      
                LongBuffer imageView = stackCallocLong(1);
                int result = vkCreateImageView(dev.getLogical(), viewInfo, null, imageView);
                if (result != VK_SUCCESS)
                {
                    RuntimeException e = new RuntimeException("failed to create texture image view: " + VkResultDecoder.decode(result));
                    throw e;
                }
                swapChainImageViews.add(imageView.get(0));
            }
        }
    }

    // ===> RenderPass <===

    void createRenderPass(Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
            VkAttachmentDescription depthAttachment = VkAttachmentDescription.callocStack(stack);
            depthAttachment.format         (findDepthFormat(dev)                            );
            depthAttachment.samples        (VK_SAMPLE_COUNT_1_BIT                           );
            depthAttachment.loadOp         (VK_ATTACHMENT_LOAD_OP_CLEAR                     );
            depthAttachment.storeOp        (VK_ATTACHMENT_STORE_OP_DONT_CARE                );
            depthAttachment.stencilLoadOp  (VK_ATTACHMENT_LOAD_OP_DONT_CARE                 );
            depthAttachment.stencilStoreOp (VK_ATTACHMENT_STORE_OP_DONT_CARE                );
            depthAttachment.initialLayout  (VK_IMAGE_LAYOUT_UNDEFINED                       );
            depthAttachment.finalLayout    (VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        
            VkAttachmentReference depthAttachmentRef = VkAttachmentReference.callocStack(stack);
            depthAttachmentRef.attachment ( 1                                          );
            depthAttachmentRef.layout     ( VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL );
        
            VkAttachmentDescription.Buffer colorAttachmentB = VkAttachmentDescription.callocStack(1, stack);
            VkAttachmentDescription colorAttachment = colorAttachmentB.get(0);
            colorAttachment.format         ( swapChainImageFormat             );
            colorAttachment.samples        ( VK_SAMPLE_COUNT_1_BIT            );
            colorAttachment.loadOp         ( VK_ATTACHMENT_LOAD_OP_CLEAR      );
            colorAttachment.storeOp        ( VK_ATTACHMENT_STORE_OP_STORE     );  
            colorAttachment.stencilStoreOp ( VK_ATTACHMENT_STORE_OP_DONT_CARE );
            colorAttachment.stencilLoadOp  ( VK_ATTACHMENT_LOAD_OP_DONT_CARE  );
            colorAttachment.initialLayout  ( VK_IMAGE_LAYOUT_UNDEFINED        );
            colorAttachment.finalLayout    ( VK_IMAGE_LAYOUT_PRESENT_SRC_KHR  );
        
            VkAttachmentReference.Buffer colorAttachmentRefB = VkAttachmentReference.callocStack(1, stack);
            VkAttachmentReference colorAttachmentRef = colorAttachmentRefB.get(0);
            colorAttachmentRef.attachment ( 0                                  );
            colorAttachmentRef.layout     ( VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL );
        
            VkSubpassDescription.Buffer subpassB = VkSubpassDescription.callocStack(1, stack);
            VkSubpassDescription subpass = subpassB.get(0);
            subpass.pipelineBindPoint       ( VK_PIPELINE_BIND_POINT_GRAPHICS );
            subpass.colorAttachmentCount    ( 1                         );
            subpass.pColorAttachments       ( colorAttachmentRefB              );
            subpass.pDepthStencilAttachment ( depthAttachmentRef              );
        
            VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack);
            dependency.dstSubpass    ( 0                                        );
            dependency.dstAccessMask ( VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT );
            dependency.dstStageMask  ( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT );
            dependency.srcSubpass    ( VK_SUBPASS_EXTERNAL                            );
            dependency.srcAccessMask ( 0                                        );
            dependency.srcStageMask  ( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT  );
        
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(2, stack);
            attachments.put(0, colorAttachment);
            attachments.put(1 , depthAttachment);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
            renderPassInfo.sType           ( VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO );
            renderPassInfo.pAttachments    ( attachments                               );
            renderPassInfo.pSubpasses      ( subpassB                                  );
            renderPassInfo.pDependencies   ( dependency                                );
        
            LongBuffer renderPass = stack.callocLong(1);
            int result = vkCreateRenderPass(dev.getLogical(), renderPassInfo, null, renderPass);
            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create render pass: " + VkResultDecoder.decode(result));
                throw e;
            }
            pRenderpass = renderPass.get(0);
        }
    }

    // ===> DepthResources <===

    int findDepthFormat(Device dev)
    {
        int [] formats = {VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT};
        return dev.findSupportedFormat ( formats, VK_IMAGE_TILING_OPTIMAL, VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

    void createDepthResources(Device dev)
    {
        int depthFormat = findDepthFormat(dev);

        try(MemoryStack stack = stackPush())
        {
        
            for (int i = 0; i < swapChainImageViews.size(); i++)
            {
            
                VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack);
                imageInfo.sType           (VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO        );
                imageInfo.imageType       (VK_IMAGE_TYPE_2D                           );
                imageInfo.extent().width  (swapChainExtent.width()                    );
                imageInfo.extent().height (swapChainExtent.height()                   );
                imageInfo.extent().depth  (1                                    );
                imageInfo.mipLevels       (1                                    );
                imageInfo.arrayLayers     (1                                    );
                imageInfo.format          (depthFormat                                );
                imageInfo.tiling          (VK_IMAGE_TILING_OPTIMAL                    );
                imageInfo.initialLayout   (VK_IMAGE_LAYOUT_UNDEFINED                  );
                imageInfo.usage           (VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
                imageInfo.samples         (VK_SAMPLE_COUNT_1_BIT                      );
                imageInfo.sharingMode     (VK_SHARING_MODE_EXCLUSIVE                  );
                imageInfo.flags           (0                                    );
                
                LongBuffer imageMem = stack.callocLong(1);
                LongBuffer depthImage = stack.callocLong(1);
                // TODO MEMORY MANAGEMENT DEPTH IMAGES
                dev.createImageWithInfo( imageInfo, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, depthImage, imageMem);
                depthImageMemorys.add(imageMem.get(0));

                VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
                viewInfo.sType                             (VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO );
                viewInfo.image                             (depthImage.get(0)                  );
                viewInfo.viewType                          (VK_IMAGE_VIEW_TYPE_2D                    );
                viewInfo.format                            (depthFormat                              );
                viewInfo.subresourceRange().aspectMask     (VK_IMAGE_ASPECT_DEPTH_BIT                );
                viewInfo.subresourceRange().baseMipLevel   (0                                  );
                viewInfo.subresourceRange().levelCount     (1                                  );
                viewInfo.subresourceRange().baseArrayLayer (0                                  );
                viewInfo.subresourceRange().layerCount     (1                                  );

                LongBuffer imageView = stack.callocLong(1);
                int result = vkCreateImageView(dev.getLogical(), viewInfo, null, imageView);
                if (result != VK_SUCCESS)
                {
                    RuntimeException e = new RuntimeException("failed to create texture image view: " + VkResultDecoder.decode(result));
                    throw e;
                }
                depthImageViews.add(imageView.get(0));
                
            }
        }
    }

    // ===> FrameBuffers <===

    void createFramebuffers(Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
            for (int i = 0; i < swapChainImageViews.size(); i++)
            {
                LongBuffer attachments = stack.callocLong(2);
                attachments.put(0, swapChainImageViews.get(i));
                attachments.put(1, depthImageViews.get(i));
                
                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
                framebufferInfo.sType           ( VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO );
                framebufferInfo.renderPass      ( pRenderpass                               );
                framebufferInfo.attachmentCount ( 1                                   );
                framebufferInfo.pAttachments    ( attachments                               );
                framebufferInfo.width           ( swapChainExtent.width()                   );
                framebufferInfo.height          ( swapChainExtent.height()                  );
                framebufferInfo.layers          ( 1                                   );
            
                LongBuffer ret = stack.callocLong(1);
                int result = vkCreateFramebuffer( dev.getLogical(), framebufferInfo, null, ret );
                if( result != VK_SUCCESS)
                {
                    RuntimeException e = new RuntimeException("failed to create framebuffer: " + VkResultDecoder.decode(result));
                    throw e;
                }
                swapChainFrameBuffers.add(ret.get(0));
            }
        }
    }

    // ===> Sync Objects <===

    void createSyncObjects(Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType (VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType( VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags( VK_FENCE_CREATE_SIGNALED_BIT       );

            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
            {
                LongBuffer ret1 = stack.callocLong(1);
                LongBuffer ret2 = stack.callocLong(1);
                LongBuffer ret3 = stack.callocLong(1);
                int res1 = vkCreateSemaphore(dev.getLogical(), semaphoreInfo, null, ret1);
                int res2 = vkCreateSemaphore(dev.getLogical(), semaphoreInfo, null, ret2);
                int res3 = vkCreateFence    (dev.getLogical(), fenceInfo    , null, ret3);

                if(res1 != VK_SUCCESS || res2 != VK_SUCCESS || res3 != VK_SUCCESS)
                {
                    RuntimeException e = new RuntimeException("failed to create synchronization objects for a frame:" +
                    "\n -Semaphore (imageAvailable): " + VkResultDecoder.decode(res1) +
                    "\n -Semaphore (renderFinished): " + VkResultDecoder.decode(res2) + 
                    "\n -Fence                     : " + VkResultDecoder.decode(res3));
                    throw e;
                }

                imageAvailableSemaphores.put(i, ret1.get(0));
                renderFinishedSemaphores.put(i, ret2.get(0));
                inFlightFences          .put(i, ret3.get(0));

            }
        }
    }

    // ===> Cleanup <===

    private boolean isCleaned = false;

    public void clean()
    {
        if(!isCleaned)
        {
            for (long imageView : swapChainImageViews)
            {
                vkDestroyImageView(dev.getLogical(), imageView, null);
            }
            swapChainImageViews.clear();
        
            if (pSwapchain != 0L)
            {
                vkDestroySwapchainKHR(dev.getLogical(), pSwapchain, null);
                pSwapchain = 0L;
            }
        
            for (int i = 0; i < getImageCount(); i++)
            {
                vkDestroyImageView  (dev.getLogical(), depthImageViews  .get(i), null);
                //vkDestroyImage      (dev.getLogical(), depthImages      .get(i), null);
                vkFreeMemory        (dev.getLogical(), depthImageMemorys.get(i), null);
            }
        
            for (long framebuffer : swapChainFrameBuffers)
            {
                vkDestroyFramebuffer(dev.getLogical(), framebuffer, null);
            }
        
            vkDestroyRenderPass(dev.getLogical(), pRenderpass, null);
        
            // cleanup synchronization objects
            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
            {
                vkDestroySemaphore(dev.getLogical(), renderFinishedSemaphores.get(i), null);
                vkDestroySemaphore(dev.getLogical(), imageAvailableSemaphores.get(i), null);
                vkDestroyFence    (dev.getLogical(), inFlightFences          .get(i), null);
            }
            isCleaned = true;
        }
    }

    // ===> CMD BUFFER IMAGE STUFF <===
    public int currentFrame = MAX_FRAMES_IN_FLIGHT - 1;

    public int acquireNextImage()
    {
        try(MemoryStack stack = stackPush())
        {
            currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

            IntBuffer imageIndex = stack.callocInt(1);
            LongBuffer fence = stack.longs(inFlightFences.get(currentFrame));

            vkWaitForFences( dev.getLogical(), fence, true, Long.MAX_VALUE );
            int result = vkAcquireNextImageKHR( dev.getLogical(), pSwapchain, Integer.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK_NULL_HANDLE, imageIndex);
            if(result == VK_ERROR_OUT_OF_DATE_KHR)
            {
                return -1;
            }
            else if(result == VK_SUBOPTIMAL_KHR)
            {
                return imageIndex.get(0); //TODO Suboptimal swapchain
            }
            else if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("Cannot acquire next Image: " + VkResultDecoder.decode(result));
                throw e;
            }
            return imageIndex.get(0);
        }
    }

    int submitCommandBuffers(VkCommandBuffer buffers, int imageIndex)
    {
        try(MemoryStack stack = stackPush())
        {
            if (imagesInFlight.get(imageIndex) != null)
            {
                if(imagesInFlight.get(imageIndex) != VK_NULL_HANDLE)
                {
                    vkWaitForFences(dev.getLogical(), stack.longs(imagesInFlight.get(imageIndex)), true, Long.MAX_VALUE);
                }
            }
            imagesInFlight.put(imageIndex, inFlightFences.get(currentFrame));
        
            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType              ( VK_STRUCTURE_TYPE_SUBMIT_INFO                             );
            submitInfo.waitSemaphoreCount ( 1                                                   );
            submitInfo.pWaitSemaphores    ( stack.longs(imageAvailableSemaphores.get(currentFrame))   );
            submitInfo.pWaitDstStageMask  ( stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT) );
            submitInfo.pCommandBuffers    ( stack.pointers(buffers.address())                         );
            submitInfo.pSignalSemaphores  ( stack.longs(renderFinishedSemaphores.get(currentFrame))   );
        
            vkResetFences( dev.getLogical(), stack.longs(inFlightFences.get(currentFrame)));

            int result = vkQueueSubmit(dev.getGraphicsQueue(), submitInfo, inFlightFences.get(currentFrame));

            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to submit draw command buffer: " + VkResultDecoder.decode(result));
                throw e;
            }
        
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType              ( VK_STRUCTURE_TYPE_PRESENT_INFO_KHR                      );
            presentInfo.pWaitSemaphores    ( stack.longs(renderFinishedSemaphores.get(currentFrame)) );
            presentInfo.swapchainCount     (1                                                  );
            presentInfo.pSwapchains        ( stack.longs(pSwapchain)                                 );
            presentInfo.pImageIndices      ( stack.ints(imageIndex)                                  );
        
            result = vkQueuePresentKHR(dev.getPresentQueue(), presentInfo);
            return result;
        }
    }

    // ==== GETTERS ====
    public long getSwapChain()
    {
        return pSwapchain;
    }

    public long getRenderPass()
    {
        return pRenderpass;
    }

    public long getFrameBuffer(int i)
    {
        return swapChainFrameBuffers.get(i);
    }

    public VkExtent2D getSwapchainExtent()
    {
        return swapChainExtent;
    }

    public int getImageCount()
    {
        return swapChainImageViews.size();
    }

    float getExtentAspectRatio()
    {
        return ((float) swapChainExtent.width()) / ((float) swapChainExtent.height());
      }
}

