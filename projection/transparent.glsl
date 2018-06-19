#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  vec3 col = texture2D(texture, vertTexCoord.st).rgb;
  float alpha = 1;
  if (col.r == 0 && col.g == 0 && col.b == 0) {
    alpha = 0;
  }
  gl_FragColor = vec4(col, alpha);
}