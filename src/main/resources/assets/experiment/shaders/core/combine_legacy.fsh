#version 150

uniform sampler2D uDiffuseSampler;
uniform sampler2D uDepthSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 diffuse = texture(uDiffuseSampler, texCoord);
    fragColor = vec4(diffuse);

    float depth = texture(uDepthSampler, texCoord).r;
    if (diffuse.a > 0.0) {
        gl_FragDepth = depth;
    }
}