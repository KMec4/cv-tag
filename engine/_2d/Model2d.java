package engine._2d;

import static org.lwjgl.system.MemoryStack.stackLongs;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;

import engine.vulkan.Device;


public class Model2d
{
    //Object
    int vertexCount = 0;
    long pVertexBuffer = 0L;
    long pVertexMemory = 0L;
    Device dev;

    public Model2d(Device d, Vertex[] data)
    {
        if(data.length < 3)
        {
            RuntimeException e = new RuntimeException("Try to create Model with less than 3 Verticels!");
            throw e;
        }
        dev = d;
        try(MemoryStack stack = stackPush())
        {

            vertexCount = data.length;
            int bufferSize = data[0].getSize() * (data.length + 2);

            // copy data into Buffer to be able to copy it via MemoryUtils!
            ByteBuffer dataBytes = stack.calloc(bufferSize);
            FloatBuffer verticles = dataBytes.asFloatBuffer();

            for( Vertex v : data)
            {
                for( float f : v.getData() )
                {
                    verticles.put(f);
                }
            }
            
            LongBuffer memoryP = stack.callocLong(1);
            LongBuffer bufferP = stack.callocLong(1);

            dev.createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                bufferP,
                memoryP
                );
            
            pVertexMemory = memoryP.get(0);
            pVertexBuffer = bufferP.get(0);

            PointerBuffer dataP = stack.mallocPointer(1);
            vkMapMemory(dev.getLogical(), pVertexMemory, 0, bufferSize, 0, dataP);
            MemoryUtil.memCopy( MemoryUtil.memAddress(dataBytes), dataP.get(0), bufferSize);
            vkUnmapMemory(dev.getLogical(), pVertexMemory);
        }
    }

    public void submitToCommandBuffer(VkCommandBuffer cmd)
    {
        try(MemoryStack stack = stackPush())
        {
            vkCmdBindVertexBuffers(cmd, 0, stack.longs(pVertexBuffer), stackLongs(0));
            vkCmdDraw             (cmd, vertexCount, 1, 0, 0);
        }
    }
}
