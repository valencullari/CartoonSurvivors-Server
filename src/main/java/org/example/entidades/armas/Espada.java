package org.example.entidades.armas;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public class Espada extends Arma {

    private static final float ALCANCE = 100f;
    private static final float ALTO_ATAQUE = 30f;
    private static final float SEPARACION = 5f;

    // 0: derecha, 1: arriba-derecha,
    // 2: arriba, 3: arriba-izquierda,
    // 4: izquierda, 5: abajo-izquierda,
    // 6: abajo, 7: abajo-derecha.
    private int direccion = 0;

    public Espada() {
        super(25, 1.0f);
    }

    @Override
    public void actualizar(float delta, Jugador jugador, GestorEnemigos gestorEnemigos) {

        float dx = jugador.getDireccionX();
        float dy = jugador.getDireccionY();

        // Solo actualizar la direccion si se mueve.
        // Si esta quieto, conservar la ultima.
        if (dx * dx + dy * dy > 0.0001f) {

            double angulo = Math.atan2(dy, dx);

            int sector = (int) Math.round(angulo / (Math.PI / 4.0));

            direccion = Math.floorMod(sector, 8);
        }

        super.actualizar(delta, jugador, gestorEnemigos);
    }

    @Override
    protected void atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {

        double angulo = direccion * Math.PI / 4.0;

        float dx = (float) Math.cos(angulo);
        float dy = (float) Math.sin(angulo);

        // Centro de la hitbox del jugador.
        float jugadorX = jugador.getPosicionX() + jugador.getAnchoHitbox() / 2f;

        float jugadorY = jugador.getPosicionY() + jugador.getAltoHitbox() / 2f;

        // Distancia desde el centro del jugador
        // hasta el borde en la direccion del ataque.
        float bordeJugador = Math.abs(dx) * jugador.getAnchoHitbox() / 2f + Math.abs(dy) * jugador.getAltoHitbox() / 2f;

        // Centro de la zona de ataque.
        float distancia = bordeJugador + SEPARACION + ALCANCE / 2f;

        float ataqueX = jugadorX + dx * distancia;
        float ataqueY = jugadorY + dy * distancia;

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            if (colisiona(enemigo, ataqueX, ataqueY, dx, dy)) {

                enemigo.recibirDanio(danio);
            }
        }
    }

    private boolean colisiona(Enemigo enemigo, float ataqueX, float ataqueY, float dx, float dy) {

        float enemigoX = enemigo.getPosicionX() + enemigo.getAnchoHitbox() / 2f;

        float enemigoY = enemigo.getPosicionY() + enemigo.getAltoHitbox() / 2f;

        float diferenciaX = enemigoX - ataqueX;
        float diferenciaY = enemigoY - ataqueY;

        float mitadAncho = enemigo.getAnchoHitbox() / 2f;
        float mitadAlto = enemigo.getAltoHitbox() / 2f;

        float mitadAlcance = ALCANCE / 2f;
        float mitadGrosor = ALTO_ATAQUE / 2f;

        // Eje longitudinal de la espada.
        float proyeccionLargo = Math.abs(diferenciaX * dx + diferenciaY * dy);

        float radioLargo = Math.abs(dx) * mitadAncho + Math.abs(dy) * mitadAlto;

        if (proyeccionLargo > mitadAlcance + radioLargo) {
            return false;
        }

        // Eje perpendicular a la espada.
        float proyeccionGrosor = Math.abs(-diferenciaX * dy + diferenciaY * dx);

        float radioGrosor = Math.abs(dy) * mitadAncho + Math.abs(dx) * mitadAlto;

        if (proyeccionGrosor > mitadGrosor + radioGrosor) {
            return false;
        }

        // Comprobaciones en los ejes X e Y.
        // Evitan falsos positivos en las diagonales.
        float radioAtaqueX = Math.abs(dx) * mitadAlcance + Math.abs(dy) * mitadGrosor;

        float radioAtaqueY = Math.abs(dy) * mitadAlcance + Math.abs(dx) * mitadGrosor;

        return Math.abs(diferenciaX) <= radioAtaqueX + mitadAncho && Math.abs(diferenciaY) <= radioAtaqueY + mitadAlto;
    }

    @Override
    public String getTipo() {
        return "ESPADA";
    }

    @Override
    public int getDireccionAtaque() {
        return direccion;
    }
}

