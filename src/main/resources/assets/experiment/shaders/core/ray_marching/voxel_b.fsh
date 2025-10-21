#version 150

#define MAX_STEPS (32)
#define near (0.05)
#define uDensity (0.943)

#define VOX_WIDTH (16.0)
#define TEX_WIDTH (18)

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

const vec3 MIN_POS = vec3(0.0, 56.0, 0.0);
const vec3 BOX_SIZE = vec3(VOX_WIDTH);
const vec3 BOX_BOUND = BOX_SIZE - 1;
const vec3 INV_SIZE = vec3(1.0 / TEX_WIDTH);

vec2 intersection(
    in Ray pRay,
    const in vec3 pMin,
    const in vec3 pMax
) {
    vec3 t1 = (pMin - pRay.origin) * pRay.invert;
    vec3 t2 = (pMax - pRay.origin) * pRay.invert;

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

float edgeMask(vec3 uvw, float edgeWidth)
{
    vec3 d = smoothstep(edgeWidth, 0.0, min(uvw, 1.0 - uvw));
    return 1.0 - d.x * d.y * d.z;  // 三轴联合影响
}

float raymarching(
    const in vec3 pBegin,
    const in vec3 pDir,
    const in float tBegin,
    const in float tMax
) {
    float step = 0.125;
    float density = 0.0;
    float t = tBegin;

    vec3 pStep = pDir * step;
    vec3 p = pBegin + vec3(1.0);

    for (int i = 0; i < 8; ++i) {
        ivec3 uvw = ivec3(p);

        vec3 localUVW = clamp(p - vec3(uvw), 0.0, 1.0);
        float mask = edgeMask(localUVW, 0.2);
        float d = mix(1.0, noise(p), mask);

        if (d > 0.5) {
            density = 1.0;
            break;
        }

        p += pStep;
        t += step;
    }

    return clamp(density, 0.0, 1.0);
}

float sdRoundBox( vec3 p, vec3 b, float r )
{
    vec3 q = abs(p) - b + r;
    return length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0) - r;
}

float traversal(in Ray pRay, out float density) {

    vec3 boxMin = MIN_POS;
    vec3 boxMax = boxMin + BOX_SIZE;

    vec2 tEntry = intersection(pRay, boxMin, boxMax);
    if (tEntry.x == 1e10) {
        density = 0.0;
        return 1e10;
    }

    ivec3 step = ivec3(sign(pRay.direction));
    vec3 delta = abs(pRay.invert);

    vec3 entry = pRay.origin + pRay.direction * (tEntry.x + 1e-4) - boxMin;
    ivec3 coord = ivec3(entry);
    vec3 tmax = (coord - entry + max(step, 0.0)) * pRay.invert;

    int hitAxis = 0;
    float tResult = tEntry.x;

    float densityAccumulator = 0.0;
    for (int i = 0; i < MAX_STEPS; ++i) {

        float v = texelFetch(uVoxelSampler, coord + 1, 0).r;

        float tOld = tmax[hitAxis];
        float tMin = min(tmax.x, min(tmax.y, tmax.z));
        int axis = (tMin == tmax.x) ? 0 : (tMin == tmax.y ? 1 : 2);

        if (bool(v)) {
            float first = float(min(1, i));
            float tHit = mix(tResult, tResult + (tOld - delta[hitAxis]), first);

            /*vec3 rmBegin = pRay.origin + pRay.direction * (tHit + 1e-4) - boxMin;

            float density = raymarching(
                rmBegin,
                pRay.direction,
                tHit,
                tHit + delta[hitAxis]
            );

            if (density > 0.8) {
                tResult = tHit;
                densityAccumulator = 1.0;
                break;
            }*/
            tResult = tHit;
            densityAccumulator = 1.0;
            break;
        }

        coord[axis] += step[axis];
        tmax[axis] += delta[axis];

        if (coord[axis] < 0 || coord[axis] >= VOX_WIDTH) {
            tResult = 1e10;
            break;
        }

        hitAxis = axis;
    }

    density = clamp(densityAccumulator, 0.0, 1.0);
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
    ray.origin = pBegin;
    ray.length = D;

    float density;
    float t = traversal(ray, density);
    vec3 position = pBegin + ray.direction * t - MIN_POS;
    vec3 uvw = (position + 1.0) * INV_SIZE;

    density = smoothstep(0.8, 1.0, density);

    float hit = (1.0 - step(D, t)) * step(0.0, density);
    fragColor.a = hit;
    fragColor.rgb = vec3(uvw * hit);
}