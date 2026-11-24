package org.example.entidades.armas;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public class Espada extends Arma {

    // El alcance exterior y el ángulo coinciden con la estela del cliente.
    private static final float RADIO_EXTERNO = 130f;
    private static final float ANGULO_BARRIDO = 110f;
    private static final int SEGMENTOS = 20;
    // Centro visual de la estela respecto al origen que se envía al cliente.
    private static final float CENTRO_ESTELA_X = 30f;
    private static final float CENTRO_ESTELA_Y = 20f;

    public Espada() {
        super(25, 1.0f);
    }

    @Override
    protected boolean atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {

        float centroX = jugador.getPosicionX() + CENTRO_ESTELA_X;

        float centroY = jugador.getPosicionY() + CENTRO_ESTELA_Y;

        float anguloBase = getDireccionAtaque() * 45f;

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            if (colisionaConEstela(enemigo, centroX, centroY, anguloBase)) {

                enemigo.recibirDanio(jugador.calcularDanio(getDanio()));
            }
        }
        return true;
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

        float paso = ANGULO_BARRIDO / SEGMENTOS;
        for (int i = 0; i < SEGMENTOS; i++) {
            double a1 = Math.toRadians(inicio + paso * i);
            double a2 = Math.toRadians(inicio + paso * (i + 1));
            float x1 = centroX + (float) Math.cos(a1) * radioMaximo;
            float y1 = centroY + (float) Math.sin(a1) * radioMaximo;
            float x2 = centroX + (float) Math.cos(a2) * radioMaximo;
            float y2 = centroY + (float) Math.sin(a2) * radioMaximo;
            if (colisionaTriangulo(centroX, centroY, x1, y1, x2, y2, ex, ey, ancho, alto)) {
                return true;
            }
        }

        return false;
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
}
