uniform mat4 uMVPMatrix;  //顶点最终变换矩阵
attribute vec3 aPosition;  //顶点坐标值（x,y,z）
attribute vec2 aTexCoor;
varying vec2 vTextureCoord;

void main(){
gl_Position  = uMVPMatrix * vec4(aPosition,1);
vTextureCoord = aTexCoor;
}