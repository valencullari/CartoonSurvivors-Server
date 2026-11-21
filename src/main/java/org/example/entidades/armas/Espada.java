package org.example.entidades.armas;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public class Espada extends Arma {

    // Las mismas dimensiones que la estela del cliente.
    private static final float RADIO_INTERNO = 37f;
    private static final float RADIO_EXTERNO = 92f;
    private static final float ANGULO_BARRIDO = 110f;
    private static final int SEGMENTOS = 20;
    // Centro visual de la estela respecto al origen que se envía al cliente.
    private static final float CENTRO_ESTELA_X = 30f;
    private static final float CENTRO_ESTELA_Y = 20f;

    // Franja brillante exterior dibujada por el cliente.
    private static final float RADIO_BRILLO_INTERNO = RADIO_EXTERNO - 13f;

    private static final float RADIO_BRILLO_EXTERNO = RADIO_EXTERNO - 3f;

    // Ocho direcciones, separadas por 45 grados.
    private int direccion = 0;
    private int direccionAtaque;

    public Espada() {
        super(25, 1.0f);
    }

    @Override
    public void actualizar(float delta, Jugador jugador, GestorEnemigos gestorEnemigos) {

        float dx = jugador.getDireccionX();
        float dy = jugador.getDireccionY();

        if (dx * dx + dy * dy > 0.0001f) {

            double angulo = Math.atan2(dy, dx);

            int sector = (int) Math.round(angulo / (Math.PI / 4.0));

            direccion = Math.floorMod(sector, 8);
        }

        super.actualizar(delta, jugador, gestorEnemigos);
    }

    @Override
    protected void atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {

        float centroX = jugador.getPosicionX() + CENTRO_ESTELA_X;

        float centroY = jugador.getPosicionY() + CENTRO_ESTELA_Y;

        direccionAtaque = direccion;
        float anguloBase = direccionAtaque * 45f;

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            if (colisionaConEstela(enemigo, centroX, centroY, anguloBase)) {

                enemigo.recibirDanio(danio);
            }
        }
    }

    private boolean colisionaConEstela(Enemigo enemigo, float centroX, float centroY, float anguloBase) {

        float ex = enemigo.getPosicionX();
        float ey = enemigo.getPosicionY();

        float ancho = enemigo.getAnchoHitbox();
        float alto = enemigo.getAltoHitbox();

        // Rechazo rápido si está fuera del alcance máximo.
        float masCercanoX = Math.max(ex, Math.min(centroX, ex + ancho));

        float masCercanoY = Math.max(ey, Math.min(centroY, ey + alto));

        float dx = masCercanoX - centroX;
        float dy = masCercanoY - centroY;

        float radioMaximo = RADIO_EXTERNO * getAlcanceAtaque();
        if (dx * dx + dy * dy > radioMaximo * radioMaximo) {
            return false;
        }

        float inicio = anguloBase - ANGULO_BARRIDO / 2f;

        // Reconstruye los triángulos de la estela
        // que dibuja el cliente.
        for (int i = 0; i < SEGMENTOS; i++) {

            float t1 = (float) i / SEGMENTOS;
            float t2 = (float) (i + 1) / SEGMENTOS;

            float a1 = inicio + ANGULO_BARRIDO * t1;
            float a2 = inicio + ANGULO_BARRIDO * t2;

            // Cuerpo principal de la estela.
            if (colisionaSegmento(centroX, centroY, a1, a2, t1, t2, RADIO_INTERNO, RADIO_EXTERNO, ex, ey, ancho, alto)) {
                return true;
            }

            // Franja brillante, que en algunos ángulos
            // sobresale de la zona celeste.
            if (colisionaSegmento(centroX, centroY, a1, a2, t1, t2, RADIO_BRILLO_INTERNO, RADIO_BRILLO_EXTERNO, ex, ey, ancho, alto)) {
                return true;
            }
        }

        return false;
    }

    private boolean colisionaSegmento(float cx, float cy, float a1, float a2, float t1, float t2, float radioInterno, float radioExterno, float ex, float ey, float ancho, float alto) {

        // Mismo afinamiento que dibujarEstela()
        // del cliente.
        radioInterno *= getAlcanceAtaque();
        radioExterno *= getAlcanceAtaque();

        float grosor1 = (float) Math.sin(Math.PI * t1);
        float grosor2 = (float) Math.sin(Math.PI * t2);

        float r1 = radioExterno + (radioInterno - radioExterno) * grosor1;

        float r2 = radioExterno + (radioInterno - radioExterno) * grosor2;

        double rad1 = Math.toRadians(a1);
        double rad2 = Math.toRadians(a2);

        float cos1 = (float) Math.cos(rad1);
        float sin1 = (float) Math.sin(rad1);

        float cos2 = (float) Math.cos(rad2);
        float sin2 = (float) Math.sin(rad2);

        // Cuatro vértices del segmento.
        float x1 = cx + cos1 * radioInterno;
        float y1 = cy + sin1 * radioInterno;

        float x2 = cx + cos2 * radioInterno;
        float y2 = cy + sin2 * radioInterno;

        float x3 = cx + cos1 * r1;
        float y3 = cy + sin1 * r1;

        float x4 = cx + cos2 * r2;
        float y4 = cy + sin2 * r2;

        // Los mismos dos triángulos del renderizado.
        return colisionaTriangulo(x1, y1, x3, y3, x2, y2, ex, ey, ancho, alto) || colisionaTriangulo(x2, y2, x3, y3, x4, y4, ex, ey, ancho, alto);
    }

    private boolean colisionaTriangulo(float x1, float y1, float x2, float y2, float x3, float y3, float ex, float ey, float ancho, float alto) {

        // Separating Axis Theorem (SAT).
        // Comprueba los ejes X e Y del rectángulo
        // y las normales de los lados del triángulo.

        if (!solapanEnEje(1f, 0f, x1, y1, x2, y2, x3, y3, ex, ey, ancho, alto)) {
            return false;
        }

        if (!solapanEnEje(0f, 1f, x1, y1, x2, y2, x3, y3, ex, ey, ancho, alto)) {
            return false;
        }

        if (!solapanEnEje(-(y2 - y1), x2 - x1, x1, y1, x2, y2, x3, y3, ex, ey, ancho, alto)) {
            return false;
        }

        if (!solapanEnEje(-(y3 - y2), x3 - x2, x1, y1, x2, y2, x3, y3, ex, ey, ancho, alto)) {
            return false;
        }

        return solapanEnEje(-(y1 - y3), x1 - x3, x1, y1, x2, y2, x3, y3, ex, ey, ancho, alto);
    }

    private boolean solapanEnEje(float ejeX, float ejeY, float x1, float y1, float x2, float y2, float x3, float y3, float ex, float ey, float ancho, float alto) {

        // Ignorar ejes de lados degenerados.
        if (ejeX * ejeX + ejeY * ejeY < 0.000001f) {
            return true;
        }

        float p1 = x1 * ejeX + y1 * ejeY;
        float p2 = x2 * ejeX + y2 * ejeY;
        float p3 = x3 * ejeX + y3 * ejeY;

        float minimoTriangulo = Math.min(p1, Math.min(p2, p3));

        float maximoTriangulo = Math.max(p1, Math.max(p2, p3));

        // Proyectar el AABB del enemigo sobre el eje.
        float centroEnemigoX = ex + ancho / 2f;
        float centroEnemigoY = ey + alto / 2f;

        float centroProyectado = centroEnemigoX * ejeX + centroEnemigoY * ejeY;

        float radioProyectado = Math.abs(ejeX) * ancho / 2f + Math.abs(ejeY) * alto / 2f;

        float minimoEnemigo = centroProyectado - radioProyectado;

        float maximoEnemigo = centroProyectado + radioProyectado;

        return maximoTriangulo >= minimoEnemigo && maximoEnemigo >= minimoTriangulo;
    }

    @Override
    public String getTipo() {
        return "ESPADA";
    }

    @Override
    public int getDireccionAtaque() {
        return direccionAtaque;
    }
}
