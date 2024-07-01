#version 450

layout(location = 0) in vec2 position;
layout(location = 1) in vec3 color;

layout(location = 0) out vec3 outC;

layout(push_constant) uniform Push {
  mat2 transform;
  vec2 offset;
  vec3 color;
} push;

vec2 positions[3] = vec2[](
  vec2(0.0, -0.5),
  vec2(0.5, 0.5),
  vec2(-0.5, 0.5)
);

void main() {
    outC = vec3(color.r, color.g, color.b);
    gl_Position = vec4(push.transform * position + push.offset, 0.0, 1.0);
}