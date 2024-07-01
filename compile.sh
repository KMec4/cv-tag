cd engine/_2d/shaders/
#glslc shader.frag -o frag.spirv
#glslc shader.vert -o vert.spirv

cd ../../_3d/shaders/
glslc shader.frag -o frag.spirv
glslc shader.vert -o vert.spirv


#cd ../../_3d/pickingSystem/shaders/
#glslc shader.frag -o frag.spirv
#glslc shader.vert -o vert.spirv

cd ../../../..