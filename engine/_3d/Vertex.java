package engine._3d;

import org.joml.Matrix2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class Vertex
{
    public static final int LENGTH = 11;
    public static final int SIZE = LENGTH * 4;

    float[] value;

    public Vertex(Vector3f position, Vector3f color, Vector3f normal)
    {
        value = createVertex(position, color, normal, new Vector2f(.0f, .0f));
    }

    public Vertex(Vector3f position, Vector3f color, Vector3f normal, Vector2f uv)
    {
        value = createVertex(position, color, normal, uv);
    }

    public float[] getData()
    {
        return value;
    }

    public static int getSize()
    {
        return LENGTH * 4;
    }

    public static float[] createVertex(Vector3f position, Vector3f color, Vector3f normal, Vector2f uv)
    {
        float[] value = new float[LENGTH];
        value[0] = position.x ;
        value[1] = position.y ;
        value[2] = position.z ;
        value[3] = color.x ;
        value[4] = color.y ;
        value[5] = color.z ;
        value[6] = normal.x;
        value[7] = normal.y;
        value[8] = normal.z;
        value[9] = uv.x;
        value[10] = uv.y;
        return value;
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
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(4, stack);
            attributeDescriptions.get(0).binding  (0);
            attributeDescriptions.get(0).location (0);
            attributeDescriptions.get(0).format   (VK_FORMAT_R32G32B32_SFLOAT);
            attributeDescriptions.get(0).offset   (0);//offsetof(Vertex, position));

            attributeDescriptions.get(1).binding  (0);
            attributeDescriptions.get(1).location (1);
            attributeDescriptions.get(1).format   (VK_FORMAT_R32G32B32_SFLOAT);
            attributeDescriptions.get(1).offset   (12);//offsetof(Vertex, color));

            attributeDescriptions.get(2).binding  (0);
            attributeDescriptions.get(2).location (2);
            attributeDescriptions.get(2).format   (VK_FORMAT_R32G32B32_SFLOAT);
            attributeDescriptions.get(2).offset   (24);//offsetof(Vertex, color));

            attributeDescriptions.get(3).binding  (0);
            attributeDescriptions.get(3).location (3);
            attributeDescriptions.get(3).format   (VK_FORMAT_R32G32_SFLOAT);
            attributeDescriptions.get(3).offset   (36);//offsetof(Vertex, color));

            return attributeDescriptions;
    } 

    // PushConstant Class

    public static class PushConstant
    {
        public static final int LENGTH = 32;
        public static final int SIZE = LENGTH * 4;

        float[] value = new float[LENGTH];

        public PushConstant(Vector3f color, Matrix4f transform, Matrix4f normal)
        {
            value[0 ] = transform.m00();
            value[1 ] = transform.m01();
            value[2 ] = transform.m02();
            value[3 ] = transform.m03();
            value[4 ] = transform.m10();
            value[5 ] = transform.m11();
            value[6 ] = transform.m12();
            value[7 ] = transform.m13();
            value[8 ] = transform.m20();
            value[9 ] = transform.m21();
            value[10] = transform.m22();
            value[11] = transform.m23();
            value[12] = transform.m30();
            value[13] = transform.m31();
            value[14] = transform.m32();
            value[15] = transform.m33();

            value[16] = normal.m00();
            value[17] = normal.m01();
            value[18] = normal.m02();
            value[19] = normal.m03();
            value[20] = normal.m10();
            value[21] = normal.m11();
            value[22] = normal.m12();
            value[23] = normal.m13();
            value[24] = normal.m20();
            value[25] = normal.m21();
            value[26] = normal.m22();
            value[27] = normal.m23();
            value[28] = normal.m30();
            value[29] = color.x();
            value[30] = color.y();
            value[31] = color.z();

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

    public static class PushConstantPicker
    {
        public static final int LENGTH = 20;
        public static final int SIZE = LENGTH * 4;

        float[] value = new float[LENGTH];

        public PushConstantPicker(int id, Matrix4f transform)
        {
            value[0 ] = transform.m00();
            value[1 ] = transform.m01();
            value[2 ] = transform.m02();
            value[3 ] = transform.m03();
            value[4 ] = transform.m10();
            value[5 ] = transform.m11();
            value[6 ] = transform.m12();
            value[7 ] = transform.m13();
            value[8 ] = transform.m20();
            value[9 ] = transform.m21();
            value[10] = transform.m22();
            value[11] = transform.m23();
            value[12] = transform.m30();
            value[13] = transform.m31();
            value[14] = transform.m32();
            value[15] = transform.m33();

            value[16] = ((float) ((id >> 0 ) & 0xFF)) / 255.0f; 
            value[17] = ((float) ((id >> 8 ) & 0xFF)) / 255.0f;
            value[18] = ((float) ((id >> 16) & 0xFF)) / 255.0f;
            value[19] = ((float) ((id >> 24) & 0xFF)) / 255.0f;
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
            pushConstantRange.get(0).size       ( Vertex.PushConstantPicker.SIZE );
            return pushConstantRange;
        }
    }

}
