#version 150

#define MAX_STEPS (32)
#define near (0.05)
#define uDensity (0.943)

#define WIDTH (16.0)

struct Ray {
    vec3 origin;
    vec3 direction;
    vec3 invert;
    float length;
};

struct AABB {
    vec3 min;
    vec3 max;
};

uniform sampler2D uDepthSampler;

uniform sampler3D uVoxelSampler;

uniform mat4 uInverseProjectionMatrix;
uniform mat4 uInverseViewMatrix;
uniform vec3 uCameraPosition;

in vec2 texCoord;
out vec4 fragColor;

const vec3 BOX_SIZE = vec3(WIDTH);
const vec3 BOX_BOUND = BOX_SIZE - 1;
const vec3 INV_SIZE = vec3(1.0 / WIDTH);

vec2 intersection(in Ray pRay, in AABB aabb) {
    vec3 t1 = (aabb.min - pRay.origin) * pRay.invert;
    vec3 t2 = (aabb.max - pRay.origin) * pRay.invert;

    vec3 tmin3 = min(t1, t2);
    vec3 tmax3 = max(t1, t2);

    float tmin = max(max(tmin3.x, tmin3.y), max(tmin3.z, 0.0));
    float tmax = min(min(tmax3.x, tmax3.y), tmax3.z);

    vec2 result = vec2(tmin, tmax);
    return tmin > tmax ? vec2(1e10) : result;
}

float traversal(in Ray pRay) {

    AABB aabb;
    aabb.min = vec3(0.0, 56.0, 0.0);
    aabb.max = aabb.min + BOX_SIZE;

    vec2 tEntry = intersection(pRay, aabb);
    if (tEntry.x == 1e10) {
        return 1e10;
    }

    ivec3 step = ivec3(sign(pRay.direction));
    vec3 delta = abs(pRay.invert);

    vec3 entry = pRay.origin + pRay.direction * (tEntry.x + 1e-4) - aabb.min;
    ivec3 coord = ivec3(entry);
    vec3 tmax = (coord - entry + max(step, 0.0)) * pRay.invert;

    int axis = 0;
    float tResult = tEntry.x;
    for (int i = 0; i < MAX_STEPS; i++) {

        float v = texelFetch(uVoxelSampler, coord, 0).r;
        if (bool(v)) {
            float first = float(min(1, i));
            tResult = mix(tResult, tResult + (tmax[axis] - delta[axis]), first);
            break;
        }

        float tMin = min(tmax.x, min(tmax.y, tmax.z));

        axis = (tMin == tmax.x) ? 0 : (tMin == tmax.y ? 1 : 2);

        coord[axis] += step[axis];
        tmax[axis] += delta[axis];

        if (coord[axis] < 0 || coord[axis] >= WIDTH) {
            tResult = 1e10;
            break;
        }
    }

    return tResult;
}

vec4 getWorldSpacePosition(float depth, vec2 uv) {
    vec3 ndc = vec3(uv, depth) * 2.0 - 1.0;
    vec4 clip = vec4(ndc, 1.0);
    vec4 view = uInverseProjectionMatrix * clip;
    view /= view.w;

    return uInverseViewMatrix * view;
}

void main() {
    float depth = texture(uDepthSampler, texCoord).r;

    vec4 worldPosition = getWorldSpacePosition(depth, texCoord);
    vec3 pBegin = uCameraPosition;

    float D = length(worldPosition.xyz - pBegin);

    Ray ray;
    ray.direction = normalize(worldPosition.xyz - pBegin);
    ray.invert = 1.0 / ray.direction;
    ray.origin = pBegin + ray.direction * 0.05;

    float t = traversal(ray);
    vec3 position = pBegin + ray.direction * t;
    vec3 uvw = position * INV_SIZE;

    float hit = 1.0 - step(D, t);
    fragColor.a = hit;
    fragColor.rgb = uvw * hit;
}