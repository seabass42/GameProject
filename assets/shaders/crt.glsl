#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_time;
varying vec2 v_texCoords;

void main() {
    vec2 uv = v_texCoords;
    vec2 center = vec2(0.5);
    vec2 dir = uv - center;

    // --- Convex screen curvature ---
    float curvature = 0.12; // increase for stronger convex effect
    float factor = 1.0 + curvature * length(dir*2.0); // invert effect
    uv = center + dir * factor;

    // --- Pulsing chromatic aberration ---
    float baseShift = 0.05;
    float intensity = 0.05;
    float pulse = pow(abs(sin(u_time * 3.0)), 0.4);
    float shift = baseShift + pulse * intensity;
    vec2 aberr = dir * shift;

    float r = texture2D(u_texture, uv + aberr).r;
    float g = texture2D(u_texture, uv).g;
    float b = texture2D(u_texture, uv - aberr).b;
    vec4 color = vec4(r, g, b, 1.0);

    // --- Vignette ---
    float dist = length(uv - center);
    float vignette = smoothstep(0.7, 0.5, dist);
    vignette += 0.1 * sin(u_time * 3.0);
    color.rgb *= vignette;

    // --- Scanlines ---
    float scanline = sin(uv.y * u_resolution.y * 1.5) * 0.08;
    color.rgb -= scanline;

    gl_FragColor = color;
}
