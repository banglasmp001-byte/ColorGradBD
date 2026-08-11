#version 150

uniform sampler2D DiffuseSampler;

// Color grading uniforms
uniform float Brightness;    // -1.0 to 1.0  (0 = no change)
uniform float Contrast;      // -1.0 to 1.0  (0 = no change)
uniform float Saturation;    // -1.0 to 1.0  (0 = no change)
uniform float HueShift;      // -1.0 to 1.0  (normalized from -180..180 deg)
uniform float Sharpness;     //  0.0 to 1.0
uniform float ColorR;        //  0.0 to 2.0  (1 = no change)
uniform float ColorG;        //  0.0 to 2.0
uniform float ColorB;        //  0.0 to 2.0
uniform float Intensity;     //  0.0 to 1.0  (1 = full effect)
uniform float Gamma;         //  0.5 to 2.0  (1 = no change)
uniform float Temperature;   // -1.0 to 1.0  (0 = neutral)
uniform float Vignette;      //  0.0 to 1.0

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

// --------------------------------------------------
// Utility functions
// --------------------------------------------------

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0/3.0, 2.0/3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0*d+e)), d/(q.x+e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// Luminance (perceptual, Rec.709)
float luminance(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

// Simple 3×3 sharpen kernel
vec3 sharpen(sampler2D tex, vec2 uv, vec2 texel, float strength) {
    vec3 center = texture(tex, uv).rgb;
    vec3 top    = texture(tex, uv + vec2(0.0,  texel.y)).rgb;
    vec3 bottom = texture(tex, uv + vec2(0.0, -texel.y)).rgb;
    vec3 left   = texture(tex, uv + vec2(-texel.x, 0.0)).rgb;
    vec3 right  = texture(tex, uv + vec2( texel.x, 0.0)).rgb;
    // Laplacian sharpening
    vec3 sharpened = center + strength * (4.0*center - top - bottom - left - right);
    return clamp(sharpened, 0.0, 1.0);
}

// --------------------------------------------------
// Main
// --------------------------------------------------

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);
    vec3 color    = original.rgb;

    // --- Sharpness (3×3 Laplacian) ---
    if (Sharpness > 0.001) {
        color = sharpen(DiffuseSampler, texCoord, oneTexel, Sharpness * 2.0);
    }

    // --- Brightness (additive) ---
    color += Brightness;

    // --- Contrast (S-curve around 0.5) ---
    // contrast range -1..1; remap to a scale factor
    float contrastFactor = (Contrast >= 0.0)
        ? 1.0 + Contrast * 3.0
        : 1.0 + Contrast;       // softer reduction
    color = (color - 0.5) * contrastFactor + 0.5;

    // --- Gamma ---
    if (abs(Gamma - 1.0) > 0.001) {
        color = pow(max(color, 0.0), vec3(1.0 / Gamma));
    }

    // --- Saturation ---
    if (abs(Saturation) > 0.001) {
        float lum = luminance(color);
        // positive = more saturated, negative = desaturated
        color = mix(vec3(lum), color, 1.0 + Saturation);
    }

    // --- Hue Shift (±180° mapped to ±1.0) ---
    if (abs(HueShift) > 0.001) {
        vec3 hsv = rgb2hsv(color);
        hsv.x    = fract(hsv.x + HueShift);
        color    = hsv2rgb(hsv);
    }

    // --- Color Temperature ---
    // Warm = more red/less blue, Cool = more blue/less red
    if (abs(Temperature) > 0.001) {
        color.r += Temperature * 0.1;
        color.b -= Temperature * 0.1;
    }

    // --- Color Tint (RGB multiplier) ---
    color *= vec3(ColorR, ColorG, ColorB);

    color = clamp(color, 0.0, 1.0);

    // --- Intensity (blend between original and graded) ---
    color = mix(original.rgb, color, Intensity);

    // --- Vignette ---
    if (Vignette > 0.001) {
        vec2 uv = texCoord * 2.0 - 1.0;
        float dist = length(uv);
        float vig  = smoothstep(0.5, 1.5, dist * (0.5 + Vignette));
        color *= 1.0 - vig * Vignette;
    }

    fragColor = vec4(color, original.a);
}
