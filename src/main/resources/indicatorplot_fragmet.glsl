uniform float alpha;

void main() {
	gl_FragColor = gl_Color;
	gl_FragColor.a *= alpha;
}