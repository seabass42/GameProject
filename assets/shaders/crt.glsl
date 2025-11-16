#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;  // Fullscreen texture (FBO)
uniform vec2 u_resolution;    // Screen resolution
uniform float u_time;         // Optional, for dynamic effects
varying vec2 v_texCoords;

void main() {
    // Centered coordinates (0 at center, -1..1)
    vec2 uv = v_texCoords;

    // Chromatic aberration offset
    float shift = 0.1; // adjust for strength
    vec2 dir = (uv - 0.5); // direction from center
    vec2 offset = dir * shift;

    // Sample each channel separately with slight offsets
    float r = texture2D(u_texture, uv + offset).r;
    float g = texture2D(u_texture, uv).g;
    float b = texture2D(u_texture, uv - offset).b;

    gl_FragColor = vec4(r, g, b, 1.0);
}
