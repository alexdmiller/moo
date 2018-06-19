#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform sampler2D mask;

uniform vec2 texOffset;
varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  gl_FragColor = mix(vec4(1, 1, 1, 0), vec4(0, 0, 0, 0), 1.0 - maskColor.r);
}