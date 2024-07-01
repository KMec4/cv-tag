package engine._3d;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import engine.GameObject;
import engine.vulkan.Device;
import engine.vulkan.Pipeline;
import engine.vulkan.Viewport;

public class RenderSystem3d
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
                "engine/_3d/shaders/frag.spirv",
                "engine/_3d/shaders/vert.spirv",
                Vertex.getBindingDescriptions(stack),
                Vertex.getAttributeDescriptions(stack),
                Vertex.PushConstant.getPushConstantRange(stack)
            );
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

    private static Matrix4f normalMatrix(RenderSystemI3d obj)
    {
        Matrix4f mat = new Matrix4f
        (
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f,0.0f, 1.0f
        );
        Vector3f invScale = new Vector3f(1.0f).div(obj.scale());
      
        return mat
        .translate(obj.translation())
        .setRotationYXZ(obj.rotation().y, obj.rotation().x, obj.rotation().z)
        .scale(invScale);
        };
 
    public static void renderGameObjects(VkCommandBuffer cmd, Viewport v)
    {

        vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, pipe.getPipeline());

        Matrix4f camMatrix = new Matrix4f();
        v.getProjectionMatrix().mul(v.getViewMatrix(), camMatrix);

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
                    new Vertex.PushConstant(objRS.color(), transform, normalMatrix(objRS)).getData()
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
