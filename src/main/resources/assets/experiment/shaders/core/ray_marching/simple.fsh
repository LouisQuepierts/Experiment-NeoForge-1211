#version 150

#define STEP (256)
#define near (0.05)
#define uDensity (0.943)

uniform sampler2D uScreenSampler;
uniform sampler2D uDepthSampler;

uniform mat4 uInverseProjectionMatrix;
uniform mat4 uInverseViewMatrix;
uniform vec3 uCameraPosition;

in vec2 texCoord;
out vec4 fragColor;

vec4 getWorldSpacePosition(float depth, vec2 uv) {
    vec4 screenPos = vec4(texCoord, depth, 1.0);
    vec4 viewPos = uInverseProjectionMatrix * (screenPos * 2.0 - 1.0);
    viewPos /= viewPos.w;

    vec4 worldPos = uInverseViewMatrix * vec4(viewPos.xyz, 1.0);
    return worldPos;
}

float sdfSphere(vec3 p, vec3 center, float radius) {
    return length(p - center) - radius;
}

float sdRoundBox( vec3 p, vec3 b, float r )
{
    vec3 q = abs(p) - b + r;
    return length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0) - r;
}

float sceneSDF(vec3 p) {
    return sdRoundBox(p, vec3(10.0, 5.0, 10.0), 0.5);
}

vec4 raymarching(vec3 pWorldPosition, vec3 pCameraPosition) {
    vec3 rayDir = normalize(pWorldPosition - pCameraPosition);
    vec3 rayPos = pCameraPosition;

    float t = 0.0;
    float tMax = length(pWorldPosition - pCameraPosition);
    float minDist = 0.001;

    float dist;
    float accumAlpha = 0.0;

    const int MAX_STEPS = 128;

    for(int steps = 0; steps < MAX_STEPS; steps++) {
        vec3 pos = rayPos + t * rayDir;
        dist = sceneSDF(pos);
        float delta = max(dist, 0.025);

        if(dist < minDist) {
            accumAlpha += uDensity * delta;
            if(accumAlpha >= 0.99) {
                accumAlpha = 1.0;
                break;
            }
        }

        t += delta;

        if(t > tMax) break;
    }

    return vec4(accumAlpha);
}

void main() {
    vec4 color = texture(uScreenSampler, texCoord);
    float depth = texture(uDepthSampler, texCoord).r;

    vec4 worldPosition = getWorldSpacePosition(depth, texCoord);
    vec4 cloud = raymarching(worldPosition.xyz, uCameraPosition);
    fragColor.a = 1.0;
    fragColor.rgb = color.rgb * (1.0 - cloud.a) + cloud.rgb;
}