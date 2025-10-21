#version 150

uniform vec4 uColor;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    fragColor.a = 1.0;
    fragColor.rgb = mix(mix(vec3(1), uColor.rgb, texCoord0.x), vec3(0), texCoord0.y);
}
