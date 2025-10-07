#version 150

uniform sampler2D uDiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 diffuse = texture(uDiffuseSampler, texCoord);
    fragColor = vec4(diffuse);
}