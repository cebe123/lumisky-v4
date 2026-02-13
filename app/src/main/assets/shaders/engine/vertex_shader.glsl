attribute vec4 a_Position; // Köşe pozisyonu (giriş)
varying vec2 v_TexCoord;   // Doku koordinatları (fragment shader'a gönderilir)
varying vec2 v_Uv;         // UV koordinatları (0.0 - 1.0 aralığında)

void main() {
    gl_Position = a_Position;
    
    // UV koordinatlarını hesapla (-1..1 aralığından 0..1 aralığına dönüştür)
    v_Uv = a_Position.xy * 0.5 + 0.5;
    
    // Doku koordinatlarını ayarla (Y eksenini ters çevir)
    v_TexCoord = vec2(v_Uv.x, 1.0 - v_Uv.y);
}
