package engine._3d.pickingSystem;


import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

import engine.GameObject;
import engine.VkResultDecoder;
import engine._3d.RenderSystemI3d;
import engine._3d.Vertex;
import engine.vulkan.Device;
import engine.vulkan.GameWindow;
import engine.vulkan.Pipeline;
import engine.vulkan.Viewport;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.system.MemoryStack.stackCallocLong;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;



public class Pick3d
{

    Pipeline pipe;
    long pRenderpass;
    long pCMD = 0L;
    long pFramebuffer = 0L;

    long imgMem = 0L;
    long imgBuf = 0L;
    long imgView = 0L;
    long depthMem = 0L;
    long depthImg = 0L;
    long depthView = 0L;

    long commandPool = 0L;
    long pFence = 0L;

    VkExtent2D imgExtent2d;

    Viewport view;
    Device dev;


    public Pick3d(Device d, GameWindow w, Viewport view)
    {
        this.view = view;
        dev = d;
        try(MemoryStack stack = stackPush())
        {
            createRenderPass(d);

            pipe = new Pipeline
            (
                d.getLogical(),
                pRenderpass,
                "./engine/_3d/pickingSystem/shaders/frag.spirv",
                "./engine/_3d/pickingSystem/shaders/vert.spirv",
                Vertex.getBindingDescriptions(stack),
                Vertex.getAttributeDescriptions(stack),
                Vertex.PushConstantPicker.getPushConstantRange(stack)
                );

            createFramebuffer(d, w.getSize());
            createCMD(d);

            return;
        }
    }

    public GameObject action(GameWindow w)
    {
        System.out.println("Picker: Action");
        if(!imgExtent2d.equals(w.getSize()))
        {
            System.out.println(" -> recreate Framebuffer");
            recreateFramebuffer(dev, w.getSize());
        }
        execCommandBuffer(view, dev, w.getSize());
        int id = loadImage(dev, w.getWindow());
        if(id < 0)
        {
            return null;
        }
        else
        {
            GameObject o = GameObject.getAllGameObjects().get(id);
            if(o instanceof PickSystemI)
            {
                ((PickSystemI)o).picked();
            }
            return o;
        }
    }



    private static Matrix4f calcMat4(RenderSystemI3d obj)
    {
        Matrix4f mat = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );
        return mat
        .translate(obj.translation())
        .setRotationYXZ(obj.rotation().y, obj.rotation().x, obj.rotation().z)
        .scale(obj.scale());
    }
 
    private void pickingSystemRender(VkCommandBuffer cmd)
    {
        vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, pipe.getPipeline());

        Matrix4f camMatrix = new Matrix4f();
        view.getProjectionMatrix().mul(view.getViewMatrix(), camMatrix);

        for(GameObject obj : GameObject.getAllGameObjects() )
        {
            if( obj instanceof RenderSystemI3d)
            {
                RenderSystemI3d objRS = (RenderSystemI3d) obj;

                Matrix4f transform = new Matrix4f();
                camMatrix.mul(calcMat4(objRS), transform);

                //TODO fix push stuff
                vkCmdPushConstants(
                    cmd,
                    pipe.getPipelineLayout(),
                    VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                    0,
                    new Vertex.PushConstantPicker(obj.id, transform).getData()
                );

                objRS.model().submitToCommandBuffer(cmd);
            }
        }
    }

    void createRenderPass(Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
            VkAttachmentDescription depthAttachment = VkAttachmentDescription.callocStack(stack);
            depthAttachment.format         (VK_FORMAT_D16_UNORM                             );
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
            colorAttachment.format         ( VK_FORMAT_R8G8B8A8_UNORM         );
            colorAttachment.samples        ( VK_SAMPLE_COUNT_1_BIT            );
            colorAttachment.loadOp         ( VK_ATTACHMENT_LOAD_OP_CLEAR      );
            colorAttachment.storeOp        ( VK_ATTACHMENT_STORE_OP_STORE     );  
            colorAttachment.stencilStoreOp ( VK_ATTACHMENT_STORE_OP_DONT_CARE );
            colorAttachment.stencilLoadOp  ( VK_ATTACHMENT_LOAD_OP_DONT_CARE  );
            colorAttachment.initialLayout  ( VK_IMAGE_LAYOUT_UNDEFINED        );
            colorAttachment.finalLayout    ( VK_IMAGE_LAYOUT_GENERAL  );
        
            VkAttachmentReference.Buffer colorAttachmentRefB = VkAttachmentReference.callocStack(1, stack);
            VkAttachmentReference colorAttachmentRef = colorAttachmentRefB.get(0);
            colorAttachmentRef.attachment ( 0                                  );
            colorAttachmentRef.layout     ( VK_IMAGE_LAYOUT_GENERAL );
        
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

    void createFramebuffer(Device dev, VkExtent2D dim)
    {
        try(MemoryStack stack = stackPush())
        {
            imgExtent2d = VkExtent2D.create().set(dim.width(), dim.height());
            // Image
            VkImageCreateInfo cInf = VkImageCreateInfo.calloc(stack);
            cInf.sType$Default();
            cInf.format          (VK_FORMAT_R8G8B8A8_UNORM);
            cInf.usage           (VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
            cInf.imageType       (VK_IMAGE_TYPE_2D                           );
            cInf.extent().width  (dim.width()                    );
            cInf.extent().height (dim.height()                   );
            cInf.extent().depth  (1                        );
            cInf.mipLevels       (1                                    );
            cInf.arrayLayers     (1                                    );
            cInf.tiling          (VK_IMAGE_TILING_OPTIMAL                    );
            cInf.initialLayout   (VK_IMAGE_LAYOUT_UNDEFINED                  );
            cInf.samples         (VK_SAMPLE_COUNT_1_BIT                      );
            cInf.sharingMode     (VK_SHARING_MODE_EXCLUSIVE                  );
            cInf.flags           (0                                    );


            LongBuffer pImg = stackCallocLong(1);
            LongBuffer pImgMem = stackCallocLong(1);
            dev.createImageWithInfo(cInf, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pImg, pImgMem);
            imgMem = pImgMem.get(0);
            imgBuf = pImg.get(0);

            // Depth Image
            cInf.usage           (VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
            cInf.format          (VK_FORMAT_D16_UNORM);
            cInf.flags           (VK_IMAGE_CREATE_MUTABLE_FORMAT_BIT);

            LongBuffer pImgDepth = stackCallocLong(1);
            LongBuffer pImgMemDepth = stackCallocLong(1);
            dev.createImageWithInfo(cInf, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pImgDepth, pImgMemDepth);
            depthMem = pImgMemDepth.get(0);
            depthImg = pImgDepth.get(0);

            // Image Views

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
            viewInfo.sType                             (VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO );
            viewInfo.image                             (pImg.get(0)                   );
            viewInfo.viewType                          (VK_IMAGE_VIEW_TYPE_2D                    );
            viewInfo.format                            (VK_FORMAT_R8G8B8A8_UNORM                 );
            viewInfo.subresourceRange().aspectMask     (VK_IMAGE_ASPECT_COLOR_BIT                );
            viewInfo.subresourceRange().baseMipLevel   (0                                  );
            viewInfo.subresourceRange().levelCount     (1                                  );
            viewInfo.subresourceRange().baseArrayLayer (0                                  );
            viewInfo.subresourceRange().layerCount     (1                                  );

            VkImageViewCreateInfo viewInfoD = VkImageViewCreateInfo.callocStack(stack);
            viewInfoD.sType                             (VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO );
            viewInfoD.image                             (pImgDepth.get(0)                   );
            viewInfoD.viewType                          (VK_IMAGE_VIEW_TYPE_2D                    );
            viewInfoD.format                            (VK_FORMAT_D16_UNORM                      );
            viewInfoD.subresourceRange().aspectMask     (VK_IMAGE_ASPECT_DEPTH_BIT                );
            viewInfoD.subresourceRange().baseMipLevel   (0                                  );
            viewInfoD.subresourceRange().levelCount     (1                                  );
            viewInfoD.subresourceRange().baseArrayLayer (0                                  );
            viewInfoD.subresourceRange().layerCount     (1                                  );

            // Creation
            LongBuffer attachments = stack.callocLong(2);

            LongBuffer imageView = stack.callocLong(1);
            int result = vkCreateImageView(dev.getLogical(), viewInfo, null, imageView);
            attachments.put(0, imageView.get(0));
            imgView = imageView.get(0);
            
            result = vkCreateImageView(dev.getLogical(), viewInfoD, null, imageView);
            attachments.put(1, imageView.get(0));
            depthView = imageView.get(0);


            //Framebuffer
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType           ( VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO );
            framebufferInfo.renderPass      ( pRenderpass                               );
            framebufferInfo.attachmentCount ( 1                                   );
            framebufferInfo.pAttachments    ( attachments                               );
            framebufferInfo.width           ( dim.width()                               );
            framebufferInfo.height          ( dim.height()                              );
            framebufferInfo.layers          ( 1                                   );
            
            LongBuffer ret = stack.callocLong(1);
            result = vkCreateFramebuffer( dev.getLogical(), framebufferInfo, null, ret );
            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create framebuffer: " + VkResultDecoder.decode(result));
                throw e;
            }
            pFramebuffer = ret.get(0);
            
        }
    }

    void createCMD(Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType            ( VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO  );
            poolInfo.queueFamilyIndex ( dev.getGraphicsFamilyIndices().graphicsFamily );
            poolInfo.flags            ( VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

            LongBuffer commandPool = stack.callocLong(1);
            int result = vkCreateCommandPool( dev.getLogical(), poolInfo, null, commandPool);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create command pool: " + VkResultDecoder.decode(result));
                throw e;
            }
            this.commandPool = commandPool.get(0);

            PointerBuffer commandBuffer = stack.callocPointer(1);

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocInfo.sType              ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO );
            allocInfo.level              ( VK_COMMAND_BUFFER_LEVEL_PRIMARY                );
            allocInfo.commandPool        ( commandPool.get(0)                    );
            allocInfo.commandBufferCount ( 1 );

            result = vkAllocateCommandBuffers(dev.getLogical(), allocInfo, commandBuffer);
            if(result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to allocate command buffers: " + VkResultDecoder.decode(result));
                throw e;
            }

            pCMD = commandBuffer.get(0);

            // create Fence

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType( VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags( VK_FENCE_CREATE_SIGNALED_BIT       );

            LongBuffer ret3 = stack.callocLong(1);
            int res3 = vkCreateFence    (dev.getLogical(), fenceInfo    , null, ret3);
            if(res3 != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to create synchronization objects for a frame:" +
                "\n -Fence                     : " + VkResultDecoder.decode(res3));
                throw e;
            }

            pFence = ret3.get(0);
        }
    }

    void recreateFramebuffer(Device dev, VkExtent2D dim)
    {
        //Cleanup
        vkDestroyImage(dev.getLogical(), imgBuf, null);
        vkDestroyImage(dev.getLogical(), depthImg, null);

        vkDestroyImageView(dev.getLogical(), imgView, null);
        vkDestroyImageView(dev.getLogical(), depthView, null);

        vkFreeMemory(dev.getLogical(), imgMem, null);
        vkFreeMemory(dev.getLogical(), depthMem, null);

        vkDestroyFramebuffer(dev.getLogical(), pFramebuffer, null);

        // Recreation
        createFramebuffer(dev, dim);
    }


    void execCommandBuffer(Viewport view, Device dev, VkExtent2D d)
    {
        try(MemoryStack stack = stackPush())
        {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO );

            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCMD, dev.getLogical());

            int result = vkBeginCommandBuffer(commandBuffer, beginInfo);
            if (result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to begin recording command buffer!");
                throw e;
            }

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType       ( VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO );
            renderPassInfo.renderPass  ( pRenderpass                              );
            renderPassInfo.framebuffer ( pFramebuffer         );

            VkOffset2D offset = VkOffset2D.callocStack(stack).set(0, 0);
            renderPassInfo.renderArea(VkRect2D.callocStack(stack).offset(offset).extent(d));

            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color       ( VkClearColorValue.callocStack(stack).float32(stack.floats(0.01f, 0.01f, 0.01f, 1.0f)));
            clearValues.get(1).depthStencil( VkClearDepthStencilValue.callocStack(stack).set(1.0f, 0) );

            renderPassInfo.pClearValues   ( clearValues );

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);


            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.get(0).x        ( 0.0f                          );
            viewport.get(0).y        ( 0.0f                          );
            viewport.get(0).width    ( d.width()  );
            viewport.get(0).height   ( d.height() );
            viewport.get(0).minDepth ( 0.0f                          );
            viewport.get(0).maxDepth ( 1.0f                          );

            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.get(0).set(offset, d);

            vkCmdSetViewport(commandBuffer, 0, viewport);
            vkCmdSetScissor (commandBuffer, 0,  scissor );

            try
            {
                pickingSystemRender(commandBuffer); // TODO cut in here
            }
            catch (NullPointerException e)
            {
                System.out.println("The render system was not initialized");
            }
            

            vkCmdEndRenderPass(commandBuffer);

            result = vkEndCommandBuffer(commandBuffer);
            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to record command buffer: " + VkResultDecoder.decode(result));
                throw e;
            }

            submitCommandBuffers(commandBuffer, dev);
        }
    }

    void submitCommandBuffers(VkCommandBuffer buffer, Device dev)
    {
        try(MemoryStack stack = stackPush())
        {
        
            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType              ( VK_STRUCTURE_TYPE_SUBMIT_INFO                             );
            submitInfo.waitSemaphoreCount ( 0                                                   );
            submitInfo.pCommandBuffers    ( stack.pointers(buffer.address())                          );
        
            vkResetFences( dev.getLogical(), pFence);

            int result = vkQueueSubmit(dev.getGraphicsQueue(), submitInfo, pFence);

            if( result != VK_SUCCESS)
            {
                RuntimeException e = new RuntimeException("failed to submit draw command buffer: " + VkResultDecoder.decode(result));
                throw e;
            }

            vkWaitForFences(dev.getLogical(), pFence, true, Long.MAX_VALUE);
        }
    }

    int loadImage(Device dev, long window)
    {
        try(MemoryStack stack = stackPush())
        {
            // create staging buffer
            PointerBuffer dataP = stack.mallocPointer(1);
            int bufferSize = imgExtent2d.width() * imgExtent2d.height() * 4;

            LongBuffer stagingBuffer = stack.callocLong(1);
            LongBuffer stagingBufferMemory = stack.callocLong(1);
            dev.createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                stagingBuffer,
                stagingBufferMemory);
            dev.copyImageToBuffer(imgBuf, stagingBuffer.get(0), imgExtent2d.width(), imgExtent2d.height(), 1, VK_IMAGE_LAYOUT_GENERAL);

            // calc cursor offset
            DoubleBuffer x = stack.callocDouble(1);
            DoubleBuffer y = stack.callocDouble(1);
            glfwGetCursorPos(window, x, y);
            if(x.get(0) > imgExtent2d.width() || y.get(0) > imgExtent2d.height())
            {
                return -1;
            }
            int offset = (int) (x.get(0) + (imgExtent2d.width() * y.get(0))) * 4;

            // copy cursor value and get id
            IntBuffer cursorValue = stack.callocInt(1);

            vkMapMemory(dev.getLogical(), stagingBufferMemory.get(0), offset, 4, 0, dataP);
            MemoryUtil.memCopy( dataP.get(0), MemoryUtil.memAddress(cursorValue), 4);
            vkUnmapMemory(dev.getLogical(), stagingBufferMemory.get(0));

            vkDestroyBuffer(dev.getLogical(), stagingBuffer.get(0), null);
            vkFreeMemory   (dev.getLogical(), stagingBufferMemory.get(0), null);

            int id = cursorValue.get(0);

            //output
            System.out.println("Cursor pos: [ " + x.get(0) + " , " + y.get(0) + " ]");
            if(id < 0)
            {
                System.out.println("Picked: none");
                return -1;
            }
            else
            {
                System.out.println("Picked: " + id );
                return id;
            }
        }
    }

    private void createPGM(IntBuffer img)
    {
        File f = new File("outPicker.pgm");
        FileOutputStream fos;

        try
        {
            f.createNewFile();
            fos = new FileOutputStream(f);
            fos.write( ("P2\n"+ imgExtent2d.width() + " " + imgExtent2d.height() + "\n255\n").getBytes() );
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        for(int i = 0; i < img.limit(); i++)
        {
            try
            {
                if(img.get(i) < 0)
                {
                    fos.write((" 255").getBytes());
                }
                else
                {
                    fos.write((" " + (img.get(i))).getBytes());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                try
                {
                    fos.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                return;
            }
        }
        try
        {
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}