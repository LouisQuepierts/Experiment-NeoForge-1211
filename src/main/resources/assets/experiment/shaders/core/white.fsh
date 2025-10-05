#version 120

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(texCoord, 1.0, 1.0);
}
