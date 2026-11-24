package org.example.juego;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class AtaqueFuego {

    private static final float ALCANCE = 1200f;
    private static final float VELOCIDAD = 300f;
    private static final float SEPARACION = 50f;
    private static final float COOLDOWN = 5f;
    private static final float RADIO = 50f;
    private static final float DURACION = 7f;
    private static final float DURACION_DESVANECIMIENTO = 1f;
    private static final float DANIO_POR_SEGUNDO = 20f;

    private final List<Circulo> circulos = new ArrayList<>();
    private final List<Trayectoria> trayectorias = new ArrayList<>();
    private final Map<Integer, Float> recargas = new HashMap<>();

    void intentarLanzar(Enemigo jefe, Jugador objetivo) {
        if (objetivo == null || recargas.containsKey(jefe.getId())) {
            return;
        }

        float origenX = jefe.getPosicionX() + jefe.getAnchoHitbox() / 2f;
        float origenY = jefe.getPosicionY() + jefe.getAltoHitbox() / 2f;
        float dx = objetivo.getPosicionX() + objetivo.getAnchoHitbox() / 2f - origenX;
        float dy = objetivo.getPosicionY() + objetivo.getAltoHitbox() / 2f - origenY;
        float distancia = (float) Math.hypot(dx, dy);
        if (distancia > ALCANCE || distancia < 0.001f) {
            return;
        }

        trayectorias.add(new Trayectoria(origenX, origenY, dx / distancia, dy / distancia));
        recargas.put(jefe.getId(), COOLDOWN);
        circulos.add(new Circulo(origenX, origenY, DURACION));
    }

    void actualizar(float delta, Jugador jugador1, Jugador jugador2, float multiplicadorDanio) {
        recargas.replaceAll((id, restante) -> restante - delta);
        recargas.values().removeIf(restante -> restante <= 0f);

        trayectorias.removeIf(trayectoria -> {
            trayectoria.avanzar(delta, circulos);
            return trayectoria.termino();
        });

        aplicarDanio(jugador1, delta, multiplicadorDanio);
        aplicarDanio(jugador2, delta, multiplicadorDanio);
        circulos.removeIf(circulo -> {
            circulo.tiempoRestante -= delta;
            return circulo.tiempoRestante <= 0f;
        });
    }

    private void aplicarDanio(Jugador jugador, float delta, float multiplicadorDanio) {
        if (!jugador.estaVivo()) {
            return;
        }

        // La superposicion de circulos no multiplica el danio.
        float exposicion = 0f;
        for (Circulo circulo : circulos) {
            float x = Math.clamp(circulo.x, jugador.getPosicionX(), jugador.getPosicionX() + jugador.getAnchoHitbox());
            float y = Math.clamp(circulo.y, jugador.getPosicionY(), jugador.getPosicionY() + jugador.getAltoHitbox());
            float dx = x - circulo.x;
            float dy = y - circulo.y;
            if (dx * dx + dy * dy <= RADIO * RADIO) {
                float inicio = Math.max(0f, circulo.tiempoRestante - DURACION);
                float fin = Math.min(delta, circulo.tiempoRestante);
                exposicion = Math.max(exposicion, fin - inicio);
            }
        }
        jugador.recibirDanio(DANIO_POR_SEGUNDO * exposicion * multiplicadorDanio);
    }

    void agregarEstado(List<String> entradas) {
        for (Circulo circulo : circulos) {
            float opacidad = Math.min(1f, circulo.tiempoRestante / DURACION_DESVANECIMIENTO);
            entradas.add("FUEGO:" + circulo.x + ":" + circulo.y + ":" + RADIO + ":" + opacidad);
        }
    }

    private static final class Trayectoria {
        private final float x;
        private final float y;
        private final float direccionX;
        private final float direccionY;
        private float distanciaRecorrida;
        private float ultimaDistancia;

        private Trayectoria(float x, float y, float direccionX, float direccionY) {
            this.x = x;
            this.y = y;
            this.direccionX = direccionX;
            this.direccionY = direccionY;
        }

        private void avanzar(float delta, List<Circulo> circulos) {
            float distanciaAnterior = distanciaRecorrida;
            distanciaRecorrida = Math.min(ALCANCE, distanciaRecorrida + VELOCIDAD * delta);
            while (!termino()) {
                float distancia = Math.min(ALCANCE, ultimaDistancia + SEPARACION);
                if (distancia > distanciaRecorrida) {
                    break;
                }
                // El circulo nace en el instante en que la trayectoria alcanza esta distancia.
                float demora = (distancia - distanciaAnterior) / VELOCIDAD;
                circulos.add(new Circulo(x + direccionX * distancia, y + direccionY * distancia, DURACION + demora));
                ultimaDistancia = distancia;
            }
        }

        private boolean termino() {
            return ultimaDistancia >= ALCANCE;
        }
    }

    private static final class Circulo {
        private final float x;
        private final float y;
        private float tiempoRestante;

        private Circulo(float x, float y, float tiempoRestante) {
            this.x = x;
            this.y = y;
            this.tiempoRestante = tiempoRestante;
        }
    }
}
