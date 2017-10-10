#version 120
uniform float time;
#include random.frag
void main() {
    float height = 2*(gl_Color.a-.5);
    float x = gl_Color.r;
    height += .5*cnoise(vec2(x*10, time*.5));
    gl_FragColor = gl_Color;
    gl_FragColor.r = 1;
    gl_FragColor.a = 1-height*height;
}