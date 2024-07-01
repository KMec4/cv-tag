package engine._3d;

import static org.lwjgl.system.MemoryStack.stackLongs;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import engine.vulkan.Device;


public class Model3d
{
    //Object
    int vertexCount = 0;
    int indicesCount = 0;
    long pVertexBuffer = 0L;
    long pVertexMemory = 0L;
    long pIndexBuffer = 0L;
    long pIndexMemory = 0L;
    Device dev;
    boolean isIndexed = false;

    public Model3d(Device d, Vertex[] data)
    {
        if(data.length < 3)
        {
            RuntimeException e = new RuntimeException("Try to create Model with less than 3 Verticels!");
            throw e;
        }
        dev = d;
        isIndexed = false;
        try(MemoryStack stack = stackPush())
        {

            vertexCount = data.length;
            System.out.println("\nvertexCount=" + vertexCount);
            int bufferSize = data[0].getSize() * (data.length + 2);
            System.out.println("BufferSize=" + bufferSize);
            System.out.println(stack.getSize()); 

            // copy data into Buffer to be able to copy it via MemoryUtils!
            ByteBuffer dataBytes = stack.malloc(bufferSize); //ByteBuffer.allocateDirect(bufferSize);//
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

    public Model3d(File obj, Device d, Vector3f color)
    {
        dev = d;
        isIndexed = true;
        try(MemoryStack stack = stackPush())
        {
            Obj modelDat;
            try
            {
                modelDat = ObjUtils.convertToRenderable(ObjReader.read(new FileInputStream(obj)));
            }
            catch (IOException e)
            {
                RuntimeException e1 = new RuntimeException("can't create 3dModel from " + obj.getAbsolutePath(), e);
                throw e1;
            }

            FloatBuffer verticles = stack.callocFloat(modelDat.getNumVertices() * 3);
            ObjData.getVertices(modelDat, verticles);

            FloatBuffer normals = stack.callocFloat(modelDat.getNumVertices() * 3);
            ObjData.getNormals(modelDat, normals);

            //FloatBuffer uv = stack.callocFloat(modelDat.getNumVertices() * 2);
            //ObjData.getTexCoords(modelDat, uv);
            
            vertexCount = verticles.limit();
            System.out.println("\nvertexCount=" + vertexCount);
            int bufferSize = Vertex.getSize() * vertexCount;
            System.out.println("BufferSize=" + bufferSize);
            
            ByteBuffer dataBytes = stack.malloc(bufferSize);
            FloatBuffer verticlesComputed = dataBytes.asFloatBuffer();

            for(int i = 0; i < vertexCount; i+=3)
            {
                for( float f : Vertex.createVertex(
                    new Vector3f
                    (
                        verticles.get(i),//faceVertexIndices[i]),
                        verticles.get(i+1),//faceVertexIndices[i + 1]),
                        verticles.get(i+2)//faceVertexIndices[i + 2])
                    ),
                    color,
                    new Vector3f
                    (
                        normals.get(i),
                        normals.get(i+1),
                        normals.get(i+2)
                    ),
                    new Vector2f(.0f,.0f)
                    ))
                {
                    verticlesComputed.put(f);
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

            //Indices

            indicesCount = modelDat.getNumFaces() * 3;

            ByteBuffer dataBytesI = stack.malloc(indicesCount * 4);
            IntBuffer indices = dataBytesI.asIntBuffer();
            ObjData.getFaceVertexIndices(modelDat, indices);

            memoryP = stack.callocLong(1);
            bufferP = stack.callocLong(1);

            dev.createBuffer(
                indicesCount * 4,
                VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                bufferP,
                memoryP);

            pIndexMemory = memoryP.get(0);
            pIndexBuffer = bufferP.get(0);

            dataP = stack.mallocPointer(1);
            vkMapMemory(dev.getLogical(), pIndexMemory, 0, indicesCount * 4, 0, dataP);
            MemoryUtil.memCopy( MemoryUtil.memAddress(dataBytesI), dataP.get(0), indicesCount * 4);
            vkUnmapMemory(dev.getLogical(), pIndexMemory);
        }
    }

    public void submitToCommandBuffer(VkCommandBuffer cmd)
    {
        try(MemoryStack stack = stackPush())
        {
            vkCmdBindVertexBuffers(cmd, 0, stack.longs(pVertexBuffer), stackLongs(0));
            if(isIndexed)
            {
                vkCmdBindIndexBuffer(cmd, pIndexBuffer, 0, VK_INDEX_TYPE_UINT32);
                vkCmdDrawIndexed(cmd, indicesCount, 1, 0, 0, 0);
            }
            else
            {
                vkCmdDraw             (cmd, vertexCount, 1, 0, 0);
            }
        }
    }
}
