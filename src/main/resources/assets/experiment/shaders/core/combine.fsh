#version 150

uniform sampler2D uDiffuseSampler;
uniform sampler2D uDepthSampler;
uniform sampler2D uNoiseSampler;

uniform vec2 uDiffuseResolution;
uniform vec2 uTargetResolution;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 coord = texCoord * uDiffuseResolution;
    ivec2 texelCoord = ivec2(coord);
    vec2 delta = coord - vec2(texelCoord);

    vec4 color = texelFetch(uDiffuseSampler, texelCoord, 0);
    vec4 linearColor = texture(uDiffuseSampler, texCoord);

    float difference = length(color - linearColor) * color.a;
    vec2 noise = texture(uNoiseSampler, texCoord * uTargetResolution / 256).rg;

    ivec2 direction = ivec2(sign(delta - 0.25) * step(0.1, difference * noise.x));
    vec4 result = texelFetchOffset(uDiffuseSampler, texelCoord, 0, direction);
    if (color.a == 0.0) {
        discard;
    }

    fragColor = result;

    float depth = texelFetch(uDepthSampler, texelCoord, 0).r;
    gl_FragDepth = depth;

    /*color.a = step(1e-6, color.a - a);
    if (!bool(color.a)) {
        discard;
    }
    fragColor = vec4(color);
    float depth = texelFetch(uDepthSampler, texelCoord, 0).r;
    gl_FragDepth = depth;*/
}