#version 150

#define MAX_STEPS (128)
#define near (0.05)
#define uDensity (0.943)

#define VOX_WIDTH (256.0)
#define TEX_WIDTH (256)

struct Ray {
    vec3 origin;
    vec3 direction;
    vec3 invert;
};

struct AABB {
    vec3 min;
    vec3 max;
};

uniform sampler2D uDepthSampler;

uniform sampler3D uVoxelSampler;
uniform sampler3D uOccupationSampler;

uniform mat4 uInverseProjectionMatrix;
uniform mat4 uInverseViewMatrix;
uniform vec3 uCameraPosition;

in vec2 texCoord;
out vec4 fragColor;

const vec3 MIN_POS = vec3(0.0, 0.0, 0.0);
const vec3 BOX_SIZE = vec3(VOX_WIDTH);
const vec3 BOX_BOUND = BOX_SIZE - 1;
const vec3 INV_SIZE = vec3(1.0 / TEX_WIDTH);

vec2 intersection(
    const in vec3 pOrigin,
    const in vec3 pInvDir,
    const in vec3 pMin,
    const in vec3 pMax
) {
    vec3 t1 = (pMin - pOrigin) * pInvDir;
    vec3 t2 = (pMax - pOrigin) * pInvDir;

    vec3 tmin3 = min(t1, t2);
    vec3 tmax3 = max(t1, t2);

    float tmin = max(max(tmin3.x, tmin3.y), max(tmin3.z, 0.0));
    float tmax = min(min(tmax3.x, tmax3.y), tmax3.z);

    vec2 result = vec2(tmin, tmax);
    return mix(result, vec2(1e10), step(tmax, tmin));
}

vec4 getWorldSpacePosition(float depth, vec2 uv) {
    vec3 ndc = vec3(uv, depth) * 2.0 - 1.0;
    vec4 clip = vec4(ndc, 1.0);
    vec4 view = uInverseProjectionMatrix * clip;
    view /= view.w;

    return uInverseViewMatrix * view;
}

float hash(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.1, 0.2, 0.3));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    // 三线性插值
    float n000 = hash(i + vec3(0,0,0));
    float n100 = hash(i + vec3(1,0,0));
    float n010 = hash(i + vec3(0,1,0));
    float n110 = hash(i + vec3(1,1,0));
    float n001 = hash(i + vec3(0,0,1));
    float n101 = hash(i + vec3(1,0,1));
    float n011 = hash(i + vec3(0,1,1));
    float n111 = hash(i + vec3(1,1,1));
    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(
            mix(mix(n000, n100, u.x), mix(n010, n110, u.x), u.y),
            mix(mix(n001, n101, u.x), mix(n011, n111, u.x), u.y),
            u.z
    );
}

float edgeMask(vec3 uvw, float edgeWidth) {
    vec3 d = smoothstep(edgeWidth, 0.0, min(uvw, 1.0 - uvw));
    return 1.0 - d.x * d.y * d.z;  // 三轴联合影响
}

const float INNER_UP = 4.0;
const float INNER_DOWN = 1.0 / 4.0;

float innerTraversal(
    const in Ray pRay,
    const in ivec3 pOuterCoord,
    const in int pHitAxis
) {

    ivec3 step = ivec3(sign(pRay.direction));
    ivec3 outer = pOuterCoord * 4;
    vec3 delta = abs(pRay.invert) * INNER_DOWN;

    vec3 localOrigin = fract(pRay.origin);
    vec3 entry = localOrigin * INNER_UP;
    ivec3 coord = ivec3(entry);
    vec3 tmax = (vec3(coord) - localOrigin + max(step, 0.0)) * pRay.invert;

    float tResult = 1e10;
    int hitAxis = pHitAxis;

    for (int i = 0; i < MAX_STEPS; ++i) {
        vec3 uvw = (vec3(outer +coord) + 0.5) * INV_SIZE;
        float v = noise(uvw);

        if (v > 0.2) {
            float first = float(min(1, i));
            float tHit = mix(0.0, tmax[hitAxis] - delta[hitAxis], first);
            tResult = tHit;
            break;
        }

        float tMin = min(tmax.x, min(tmax.y, tmax.z));
        int axis = (tMin == tmax.x) ? 0 : (tMin == tmax.y ? 1 : 2);
        coord[axis] += step[axis];
        tmax[axis] += delta[axis];
        hitAxis = axis;
        if (coord[axis] < 0 || coord[axis] >= 4) {
            tResult = 1e10;
            break;
        }
    }

    return tResult;
}

float traversal(const in Ray pRay) {

    vec3 boxMin = MIN_POS;
    vec3 boxMax = boxMin + BOX_SIZE;

    vec2 tEntry = intersection(pRay.origin, pRay.invert, boxMin, boxMax);
    if (tEntry.x == 1e10) {
        return 1e10;
    }

    ivec3 step = ivec3(sign(pRay.direction));
    vec3 delta = abs(pRay.invert);

    vec3 entry = (pRay.origin + pRay.direction * (tEntry.x + 1e-4) - boxMin);
    ivec3 coord = ivec3(entry);
    vec3 tmax = (vec3(coord) - entry + max(step, 0.0)) * pRay.invert;

    int hitAxis = 0;
    float tResult = tEntry.x;

    for (int i = 0; i < MAX_STEPS; ++i) {

        float v = texelFetch(uVoxelSampler, coord, 0).r;
//        float v = texture(uVoxelSampler, (vec3(coord) + 0.5) * INV_SIZE).r;

        float tMin = min(tmax.x, min(tmax.y, tmax.z));
        int axis = (tMin == tmax.x) ? 0 : (tMin == tmax.y ? 1 : 2);

        if (bool(v)) {
            float first = float(min(1, i));
            float tHit = tEntry.x + mix(0.0, tmax[hitAxis] - delta[hitAxis], first);

            tResult = tHit - 0.1;
            break;

            /*Ray subRay;
            subRay.origin = pRay.origin + pRay.direction * (tHit + 1e-4) - boxMin;
            subRay.direction = pRay.direction;
            subRay.invert = pRay.invert;

            float localHit = innerTraversal(
                subRay,
                coord + 1,
                hitAxis
            );

            if (localHit != 1e10) {
                tResult = tHit + localHit;
                break;
            }*/
        }

        coord[axis] += step[axis];
        tmax[axis] += delta[axis];

        if (coord[axis] < 0 || coord[axis] >= VOX_WIDTH) {
            tResult = 1e10;
            break;
        }

        hitAxis = axis;
    }

    return tResult;
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
    vec3 position = pBegin + ray.direction * (t + 1e-2) - MIN_POS;
    vec3 uvw = clamp(position * INV_SIZE, 1e-6, 1.0 - 1e-6);

    /*ray.origin = position;
    float st = subTraversal(ray, ivec3(position));
    t += st;*/

    float dist = t * 0.01;

    float hit = 1.0 - step(D, t);

    fragColor.a = hit;
    fragColor.rgb = vec3(uvw);
}