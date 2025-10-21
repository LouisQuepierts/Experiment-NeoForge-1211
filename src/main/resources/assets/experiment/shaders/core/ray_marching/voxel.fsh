#version 150

#define MAX_STEPS (64)
#define near (0.05)
#define uDensity (0.943)

#define VOX_WIDTH (16.0)
#define TEX_WIDTH (512)
#define CHUNK_SIZE_I (16)
#define CHUNK_SIZE (16.0)
#define CHUNK_VOXELS (16.0)

#define INF (1e10)

struct Ray {
    vec3 origin;
    vec3 direction;
    vec3 invert;
};

struct TraversalContext {
    ivec3 chunkCoord;

    ivec3 atlasMin;
    ivec3 atlasMax;

    ivec3 cStep;
    vec3 cStepF;
    vec3 tDelta;

    float tEntry;
    int tHitAxis;
};

//uniform sampler2D uDepthSampler;

uniform usampler3D uVoxelSampler;
uniform usampler3D uOccupationSampler;

uniform mat4 uProjectionMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uInverseProjectionMatrix;
uniform mat4 uInverseViewMatrix;
uniform vec3 uCameraPosition;

uniform vec3 uOffset;
#define MIN_POS uOffset

uniform ivec3 uChunkAmount;

in vec2 texCoord;
out vec4 fragColor;

const vec3 BOX_SIZE = vec3(VOX_WIDTH);
const vec3 BOX_BOUND = BOX_SIZE - 1;
const vec3 INV_SIZE = vec3(1.0 / 256.0);

Ray tRay;
TraversalContext tContext;

float intersection(
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

    return mix(tmin, INF, step(tmax, tmin));
}

vec4 getWorldSpacePosition(vec2 uv) {
    vec3 ndc = vec3(uv, 1.0) * 2.0 - 1.0;
    vec4 clip = vec4(ndc, 1.0);
    vec4 view = uInverseProjectionMatrix * clip;
    view /= view.w;

    return uInverseViewMatrix * view;
}

/*float voxelTraversal() {
    const float VOXEL_UP = 1.0 / 2.0;
    const float VOXEL_DOWN = 2.0;

    vec3 rOrigin = tRay.origin;
    vec3 rDirection = tRay.direction;
    vec3 rInvert = tRay.invert;

    ivec3 cstep = tContext.cStep;
    vec3 delta = tContext.tDelta;

    float pEntry = tContext.tEntry;

    ivec3 pAtlasMin = tContext.atlasMin;
    ivec3 pAtlasMax = tContext.atlasMax;

    vec3 entry = (rOrigin + rDirection * (pEntry + 1e-4)) - MIN_POS * CHUNK_VOXELS;
    vec3 tmax = (vec3(floor(entry)) - entry + max(tContext.cStepF, 0.0)) * rInvert;
    ivec3 coord = ivec3(entry) - tContext.chunkCoord + pAtlasMin;

    int hitAxis = tContext.tHitAxis;
    float tResult = pEntry;

    for (uint vi = 0u; vi < 48u; ++vi) {

        uint v = texelFetch(uVoxelSampler, coord % 256, 0).r;

        if (bool(v)) {
            float first = float(min(1u, vi));
            float tHit = pEntry + mix(0.0, tmax[hitAxis] - delta[hitAxis], first);

            tResult = tHit;
            break;
        }

        float tMin = min(tmax.x, min(tmax.y, tmax.z));
        int axis = int(mix(
                mix(2.0, 1.0, step(tmax.y, tMin)),
                0.0,
                step(tmax.x, tMin)
        ));

        coord[axis] += cstep[axis];
        tmax[axis] += delta[axis];

        if (coord[axis] < pAtlasMin[axis] || coord[axis] >= pAtlasMax[axis]) {
            tResult = INF;
            break;
        }

        hitAxis = axis;
    }

    return tResult;
}*/

float chunkTraversal() {
    const float CHUNK_VOXEL_MUL = 1.0 / CHUNK_SIZE;
    const int CHUNK_AMOUNT = 16;
    ivec3 SIZE = ivec3(CHUNK_AMOUNT * 16);

    vec3 boxMin = MIN_POS * CHUNK_SIZE;
    vec3 boxMax = boxMin + SIZE;

    vec3 rOrigin = tRay.origin;
    vec3 rDirection = tRay.direction;
    vec3 rInvert = tRay.invert;

    float pEntry = intersection(rOrigin, rInvert, boxMin, boxMax);

    if (pEntry >= INF) {
        return INF;
    }

    ivec3 cStep = ivec3(sign(rDirection));
    vec3 cStepF = vec3(cStep);
    vec3 tDelta = abs(rInvert);

    /*tContext.cStep = cStep;
    tContext.cStepF = vec3(cStep);
    tContext.tDelta = tDelta;*/

    vec3 origin = floor(rOrigin * CHUNK_VOXEL_MUL);
    vec3 entry = (rOrigin + rDirection * (pEntry + 1e-4) - boxMin) * CHUNK_VOXEL_MUL;
    ivec3 coord = ivec3(entry);
    vec3 tmax = (vec3(coord) - entry + max(cStepF, 0.0)) * rInvert;

    int hitAxis = 0;
    float tResult = pEntry;

    for (uint i = 0u; i < 48u; ++i) {

        uvec4 chunk = texelFetch(uOccupationSampler, coord % 16, 0);

        float tMin = min(tmax.x, min(tmax.y, tmax.z));
        int axis = int(mix(
                mix(2.0, 1.0, step(tmax.y, tMin)),
                0.0,
                step(tmax.x, tMin)
        ));

        if (bool(chunk.w)) {
            vec3 fcoord = vec3(coord);
            vec3 delta = fcoord - origin;
            float d = length(delta);
            int lod = int(d * 0.125);
            vec3 innerBoxMin = boxMin + fcoord * CHUNK_SIZE;
            vec3 innerBoxMax = innerBoxMin + CHUNK_SIZE;

            float innerEntry = intersection(rOrigin, rInvert, innerBoxMin, innerBoxMax);
            /*tResult = innerEntry;
            break;*/

            if (innerEntry < INF) {

                /*if (d2 > 128) {
                    tResult = innerEntry;
                    break;
                }*/

                ivec3 atlasCoord = ivec3(chunk.xyz) * 16;
                ivec3 vChunkCoord = coord * 16;

                /*tContext.chunkCoord = coord * 16;
                tContext.atlasMin = atlasCoord;
                tContext.atlasMax = atlasCoord + 16;
                tContext.tEntry = innerEntry;
                tContext.tHitAxis = axis;*/

//                float voxel = voxelTraversal();
                // inline begin
                ivec3 vAtlasMin = atlasCoord;
                ivec3 vAtlasMax = atlasCoord + 16;
                float pEntry = innerEntry;
                int vHitAxis = axis;

                vec3 vEntry = (rOrigin + rDirection * (pEntry + 1e-4)) - MIN_POS * CHUNK_VOXELS;
                vec3 vMax = (vec3(floor(vEntry)) - vEntry + max(cStepF, 0.0)) * rInvert;
                ivec3 vCoord = ivec3(vEntry) - vChunkCoord + vAtlasMin;

                float voxel = pEntry;

                for (uint vi = 0u; vi < 48u; ++vi) {

                    uint v = texelFetch(uVoxelSampler, vCoord % 256, 0).r;

                    if (bool(v)) {
                        float vFirst = float(min(1u, vi));
                        float vHit = pEntry + mix(0.0, vMax[vHitAxis] - tDelta[vHitAxis], vFirst);

                        voxel = vHit;
                        break;
                    }

                    float tMin = min(vMax.x, min(vMax.y, vMax.z));
                    int vAxis = int(mix(
                            mix(2.0, 1.0, step(vMax.y, tMin)),
                            0.0,
                            step(vMax.x, tMin)
                    ));

                    vCoord[vAxis] += cStep[vAxis];
                    vMax[vAxis] += tDelta[vAxis];

                    if (vCoord[vAxis] < vAtlasMin[vAxis] || vCoord[vAxis] >= vAtlasMax[vAxis]) {
                        voxel = INF;
                        break;
                    }

                    vHitAxis = vAxis;
                }

                // inline end

                if (voxel != INF) {
                    tResult = voxel;
                    break;
                }
            }
        }

        coord[axis] += cStep[axis];
        tmax[axis] += tDelta[axis];

        if (coord[axis] < 0 || coord[axis] >= 16) {
            tResult = INF;
            break;
        }

        hitAxis = axis;
    }

    return tResult;
}

void main() {
//    float depth = texture(uDepthSampler, texCoord).r;

    vec4 worldPosition = getWorldSpacePosition(texCoord);
    vec3 pBegin = uCameraPosition;

    vec3 delta = worldPosition.xyz - pBegin;

    vec3 rDirection = normalize(delta);
    tRay.direction = rDirection;
    tRay.invert = 1.0 / rDirection;
    tRay.origin = pBegin + rDirection * 0.05;

    float t = chunkTraversal();
    vec3 position = pBegin + rDirection * (t + 1e-2);
    vec3 uvw = clamp((position - MIN_POS) * INV_SIZE, 1e-6, 1.0 - 1e-6);

    /*ray.origin = position;
    float st = subTraversal(ray, ivec3(position));
    t += st;*/

    float dist = t * 0.01;

    float hit = 1.0 - step(INF, t);

    fragColor.a = hit;
    fragColor.rgb = vec3(uvw * hit);

    vec4 clip = uProjectionMatrix * uViewMatrix * vec4(position, 1.0);
    float ndcZ = clip.z / clip.w;
    gl_FragDepth = ndcZ * 0.5 + 0.5;
}