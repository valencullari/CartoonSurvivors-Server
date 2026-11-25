package org.example.juego;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.entidades.TipoEnemigo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorEnemigos {

    private static final int ITERACIONES_SEPARACION = 3;
    private static final float MARGEN_SEPARACION = 0.1f;

    private final List<Enemigo> enemigos;
    private final AtaqueFuego ataqueFuego = new AtaqueFuego();
    private final List<Bala> balas = new ArrayList<>();
    private int enemigosEliminados;
    private int experienciaPorBajas;

    public int getEnemigosEliminados() {
        return enemigosEliminados;
    }

    public int getExperienciaPorBajas() {
        return experienciaPorBajas;
    }
    private int siguienteId;

    public GestorEnemigos() {
        enemigos = new ArrayList<>();
        siguienteId = 1;
    }

    public Enemigo crearEnemigo(TipoEnemigo tipo, float posicionX, float posicionY, int vidaExtra) {

        Enemigo enemigo = new Enemigo(siguienteId, tipo, posicionX, posicionY, vidaExtra);

        enemigos.add(enemigo);
        siguienteId++;
        return enemigo;
    }

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2, float multiplicadorDanio) {

        ataqueFuego.actualizar(delta, jugador1, jugador2, multiplicadorDanio);
        for (int i = 0; i < balas.size();) {
            if (balas.get(i).actualizar(delta, enemigos)) {
                i++;
            } else {
                balas.remove(i);
            }
        }

        // 1. Eliminar muertos y actualizar movimiento.
        for (int i = enemigos.size() - 1; i >= 0; i--) {

            Enemigo enemigo = enemigos.get(i);

            if (!enemigo.estaVivo()) {
                enemigos.remove(i);
                enemigosEliminados++;
                switch (enemigo.getTipo()) {
                    case VIOLETA:
                        experienciaPorBajas += 2;
                        break;
                    case JEFE:
                        experienciaPorBajas += 30;
                        break;
                    default:
                        experienciaPorBajas++;
                        break;
                }
                continue;
            }


            enemigo.actualizar(delta, jugador1, jugador2);
            if (enemigo.getTipo() == TipoEnemigo.JEFE) {
                ataqueFuego.intentarLanzar(enemigo, enemigo.elegirObjetivo(jugador1, jugador2));
            }
            enemigo.actualizarRetroceso(delta);


        }

        // 2. Resolver superposiciones entre enemigos.
        resolverColisionesEntreEnemigos();
    }

    public void dispararBala(float x, float y, Enemigo objetivo, float danio, int rebotes, float retencionDanio) {
        balas.add(new Bala(x, y, objetivo, danio, rebotes, retencionDanio));
    }

    void agregarEstadoBalas(List<String> entradas) {
        for (Bala bala : balas) {
            entradas.add(bala.datos());
        }
    }

    private void resolverColisionesEntreEnemigos() {

        // Varias pasadas ayudan a distribuir grupos
        // densos sin necesidad de física externa.
        for (int iteracion = 0; iteracion < ITERACIONES_SEPARACION; iteracion++) {

            for (int i = 0; i < enemigos.size(); i++) {

                Enemigo enemigo1 = enemigos.get(i);

                for (int j = i + 1; j < enemigos.size(); j++) {

                    Enemigo enemigo2 = enemigos.get(j);

                    separarEnemigos(enemigo1, enemigo2);
                }
            }
        }
    }

    private void separarEnemigos(Enemigo e1, Enemigo e2) {

        if (e1.getTipo() == TipoEnemigo.MUERTE || e2.getTipo() == TipoEnemigo.MUERTE) {
            return;
        }

        // Calcular los centros de las hitboxes.
        float centro1X = e1.getPosicionX() + e1.getAnchoHitbox() / 2f;

        float centro1Y = e1.getPosicionY() + e1.getAltoHitbox() / 2f;

        float centro2X = e2.getPosicionX() + e2.getAnchoHitbox() / 2f;

        float centro2Y = e2.getPosicionY() + e2.getAltoHitbox() / 2f;

        float diferenciaX = centro2X - centro1X;
        float diferenciaY = centro2Y - centro1Y;

        // Superposición de los rectángulos.
        float superposicionX = (e1.getAnchoHitbox() + e2.getAnchoHitbox()) / 2f - Math.abs(diferenciaX);

        float superposicionY = (e1.getAltoHitbox() + e2.getAltoHitbox()) / 2f - Math.abs(diferenciaY);

        // No se están tocando.
        if (superposicionX <= 0f || superposicionY <= 0f) {
            return;
        }

        // Separar por el eje que requiere
        // el menor desplazamiento.
        if (superposicionX < superposicionY) {

            float direccion;
            if (diferenciaX >= 0f) {
                direccion = 1f;
            } else {
                direccion = -1f;
            }

            float desplazamiento = (superposicionX + MARGEN_SEPARACION) / 2f;

            e1.desplazarPorColision(-direccion * desplazamiento, 0f);

            e2.desplazarPorColision(direccion * desplazamiento, 0f);

        } else {

            float direccion;
            if (diferenciaY >= 0f) {
                direccion = 1f;
            } else {
                direccion = -1f;
            }

            float desplazamiento = (superposicionY + MARGEN_SEPARACION) / 2f;

            e1.desplazarPorColision(0f, -direccion * desplazamiento);

            e2.desplazarPorColision(0f, direccion * desplazamiento);
        }
    }

    public Enemigo obtenerEnemigo(int id) {

        for (Enemigo enemigo : enemigos) {
            if (enemigo.getId() == id) {
                return enemigo;
            }
        }

        return null;
    }

    public List<Enemigo> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }

    public long cantidadJefesVivos() {
        long cantidad = 0;
        for (Enemigo enemigo : enemigos) {
            if (enemigo.getTipo() == TipoEnemigo.JEFE && enemigo.estaVivo()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    AtaqueFuego getAtaqueFuego() {
        return ataqueFuego;
    }

    public int cantidadEnemigos() {
        return enemigos.size();
    }
}
