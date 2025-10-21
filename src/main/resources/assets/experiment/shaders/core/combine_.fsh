#version 150

uniform sampler2D uDiffuseSampler;
uniform sampler2D uDepthSampler;
uniform sampler2D uNoiseSampler;

uniform vec2 uDiffuseResolution;
uniform vec2 uTargetResolution;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    ivec2 texelCoord = ivec2(texCoord * uDiffuseResolution);

    vec4 color = texelFetch(uDiffuseSampler, texelCoord, 0);
    float alpha = texture(uDiffuseSampler, texCoord).a;
    float noise = texture(uNoiseSampler, fract(texCoord * uTargetResolution * 0.0625)).r;

    float b = smoothstep(1.0, 0.75, alpha) * noise;
    float a = step(0.5, b);
    fragColor = vec4(color);

//    color.a -= a;
    if (color.a == 1.0) {
        float depth = texelFetch(uDepthSampler, texelCoord, 0).r;
        gl_FragDepth = depth;
    }
}