#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;       // FBO texture
uniform vec2 u_resolution;         // Screen resolution
uniform float u_time;              // Time for pulsing effects
uniform float u_tiredIntensity;    // 0 = no effect, 1 = full effect
varying vec2 v_texCoords;

void main() {
    vec2 uv = v_texCoords;
    vec2 center = vec2(0.5);
    vec2 dir = uv - center;

    // --- Convex screen curvature scaled by tiredness ---
    float curvature = 0.12;
    float factor = 1.0 + curvature * length(dir*2.0) * u_tiredIntensity;
    uv = center + dir * factor;

    // --- Pulsing chromatic aberration ---
    float baseShift = 0.05;
    float intensity = 0.05;
    float pulse = pow(abs(sin(u_time * 5.25)), 0.4);
    float shift = (baseShift + pulse * intensity) * u_tiredIntensity;
    vec2 aberr = dir * shift;

    float r = texture2D(u_texture, uv + aberr).r;
    float g = texture2D(u_texture, uv).g;
    float b = texture2D(u_texture, uv - aberr).b;
    vec4 color = vec4(r, g, b, 1.0);

    // --- Vignette scaled by tiredness ---
    float dist = length(uv - center);
    float vignette = smoothstep(0.8, 0.4, dist); // stronger falloff
    vignette += 0.1 * sin(u_time * 3.0);
    color.rgb = mix(color.rgb, color.rgb * vignette, u_tiredIntensity);

    // --- Scanlines scaled by tiredness ---
    float scanline = sin(uv.y * u_resolution.y * 1.5) * 0.08 * u_tiredIntensity;
    color.rgb -= scanline;

    gl_FragColor = color;
}
