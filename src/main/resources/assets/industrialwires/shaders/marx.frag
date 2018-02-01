#version 120
uniform float time;
#include random.frag
void main() {
    float x = 2*(gl_Color.r-.5);
    float y = gl_Color.a;
    x -= .5*cnoise(vec2(10*y, time));
    gl_FragColor = vec4(1, 1, 1, 1-abs(x));
}