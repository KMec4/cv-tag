package engine._2d;

import org.joml.Matrix2f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class Vertex
{
    

    public static final int LENGTH = 5;
    public static final int SIZE = LENGTH * 4;

    float[] value = new float[LENGTH];

    public Vertex(Vector2f position, Vector3f color)
    {
        value[0] = position.x ;
        value[1] = position.y ;
        value[2] = color.x ;
        value[3] = color.y ;
        value[4] = color.z ;
    }

    public float[] getData()
    {
        return value;
    }

    public int getSize()
    {
        return value.length * 4;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack stack)
    {
        VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.callocStack(1, stack);
        bindingDescriptions.get(0).binding   (0                     );
        bindingDescriptions.get(0).stride    ( SIZE                     );
        bindingDescriptions.get(0).inputRate (VK_VERTEX_INPUT_RATE_VERTEX );

        return bindingDescriptions;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack)
    {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(2, stack);
            attributeDescriptions.get(0).binding  (0);
            attributeDescriptions.get(0).location (0);
            attributeDescriptions.get(0).format   (VK_FORMAT_R32G32_SFLOAT);
            attributeDescriptions.get(0).offset   (0);//offsetof(Vertex, position));

            attributeDescriptions.get(1).binding  (0);
            attributeDescriptions.get(1).location (1);
            attributeDescriptions.get(1).format   (VK_FORMAT_R32G32B32_SFLOAT);
            attributeDescriptions.get(1).offset   (8);//offsetof(Vertex, color));

            return attributeDescriptions;
    } 

    // PushConstant Class

    public static class PushConstant
    {
        public static final int LENGTH = 11;
        public static final int SIZE = LENGTH * 4;

        float[] value = new float[LENGTH];

        public PushConstant(Vector3f color, Vector2f offset, Matrix2f transform)
        {
            value[0] = transform.m00;
            value[1] = transform.m01;
            value[2] = transform.m10;
            value[3] = transform.m11;
            value[4] = offset.x     ;
            value[5] = offset.y     ;
            value[8] = color.x      ;
            value[9] = color.y      ;
            value[10] = color.z      ;
        }

        public float[] getData()
        {
            return value;
        }

        public int getSize()
        {
            return SIZE;
        }

        public static VkPushConstantRange.Buffer getPushConstantRange(MemoryStack stack)
        {
            VkPushConstantRange.Buffer pushConstantRange = VkPushConstantRange.calloc(1, stack);
            pushConstantRange.get(0).stageFlags ( VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT );
            pushConstantRange.get(0).offset     ( 0                                                   );
            pushConstantRange.get(0).size       ( Vertex.PushConstant.SIZE );
            return pushConstantRange;
        }
    }

}
