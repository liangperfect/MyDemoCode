precision mediump float;  //声明float的精度，一般情况下都是用mediump的
varying vec4 vColor; //接收从顶点glsl传过来的颜色参数
//对片段颜色的具体处理
void main(){
    //直接将顶点传过来的颜色参数赋值给了内置变量gl_fragColor,也就给fragment上色了
    gl_FragColor  = vColor;
}