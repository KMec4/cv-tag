package engine._2d;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import org.joml.Matrix2f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import engine.GameObject;
import engine.vulkan.Device;
import engine.vulkan.Pipeline;

public class RenderSystem2d
{
    private static Device dev;

    private static Pipeline pipe;

    public static void init(Device d, long renderPass)
    {
        dev = d;
        try(MemoryStack stack = stackPush())
        {
            pipe = new Pipeline
            (
                dev.getLogical(),
                renderPass,
                "engine/_2d/shaders/frag.spirv",
                "engine/_2d/shaders/vert.spirv",
                Vertex.getBindingDescriptions(stack),
                Vertex.getAttributeDescriptions(stack),
                Vertex.PushConstant.getPushConstantRange(stack)
            );
        }
    }

    private static Matrix2f calcMat2(RenderSystemI2d obj)
    {

        Matrix2f rotMatrix = new Matrix2f().rotate(obj.rotation());

        Matrix2f scaleMatrix = new Matrix2f(obj.scale().x, .0f, .0f, obj.scale().y);

        return rotMatrix.mul(scaleMatrix);

    }

    public static void renderGameObjects(VkCommandBuffer cmd)
    {

        vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, pipe.getPipeline());

        for(GameObject obj : GameObject.getAllGameObjects() )
        {
            if( obj instanceof RenderSystemI2d)
            {
                RenderSystemI2d objRS = (RenderSystemI2d) obj;

                //TODO fix push stuff
                vkCmdPushConstants(
                    cmd,
                    pipe.getPipelineLayout(),
                    VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                    0,
                    new Vertex.PushConstant(objRS.color(), objRS.translation(), calcMat2(objRS)).getData()
                );

                objRS.model().submitToCommandBuffer(cmd);
            }
        }
    }

    public static Device getDevice()
    {
        if(dev == null)
        {
            RuntimeException e = new RuntimeException("RenderSystem was not initialized!");
            throw e;
        }
        return dev;
    }

}
