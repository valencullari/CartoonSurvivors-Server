package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.armas.Arma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class ProgresoPartida {
    private static final float DURACION_MEJORAS = 10f;

    private enum Mejora {
        DANIO, ALCANCE, CADENCIA, VELOCIDAD;

        void aplicar(Jugador jugador) {
            Arma arma = jugador.getArmas().get(0);
            switch (this) {
                case DANIO -> arma.aumentarDanio();
                case ALCANCE -> arma.aumentarAlcance();
                case CADENCIA -> arma.aumentarCadencia();
                case VELOCIDAD -> jugador.aumentarVelocidad();
            }
        }
    }

    private final String partidaId = UUID.randomUUID().toString();
    private final int[] elecciones = new int[2];
    private final int[] revisiones = new int[2];
    private List<Mejora> opciones = List.of();
    private Mejora ultimaExcluida;
    private float tiempoRestante;
    private int nivel = 1;
    private int experiencia;

    void agregarExperiencia(int bajas) {
        experiencia += bajas;
        iniciarRondaSiCorresponde();
    }

    boolean estaEnPausa() {
        return tiempoRestante > 0f;
    }

    void actualizarPausa(float delta, Jugador jugador1, Jugador jugador2) {
        tiempoRestante = Math.max(0f, tiempoRestante - delta);
        if (estaEnPausa()) {
            return;
        }

        opciones.get(elecciones[0]).aplicar(jugador1);
        opciones.get(elecciones[1]).aplicar(jugador2);
        iniciarRondaSiCorresponde();
    }

    void elegirMejora(int jugador, String idRecibido, int nivelElegido, int revision, int opcion) {
        if (!estaEnPausa() || !partidaId.equals(idRecibido) || nivelElegido != nivel
                || jugador < 1 || jugador > 2 || opcion < 0 || opcion >= opciones.size()) {
            return;
        }

        int indice = jugador - 1;
        if (revision > revisiones[indice]) {
            elecciones[indice] = opcion;
            revisiones[indice] = revision;
        }
    }

    private void iniciarRondaSiCorresponde() {
        if (experiencia < experienciaNecesaria()) {
            return;
        }

        experiencia -= experienciaNecesaria();
        nivel++;
        List<Mejora> candidatas = new ArrayList<>(List.of(Mejora.values()));
        List<Mejora> excluibles = new ArrayList<>(candidatas);
        excluibles.remove(ultimaExcluida);
        ultimaExcluida = excluibles.get(ThreadLocalRandom.current().nextInt(excluibles.size()));
        candidatas.remove(ultimaExcluida);
        Collections.shuffle(candidatas);
        opciones = candidatas;

        // Si no se recibe una elección, queda seleccionada la primera opción.
        elecciones[0] = elecciones[1] = 0;
        revisiones[0] = revisiones[1] = 0;
        tiempoRestante = DURACION_MEJORAS;
    }

    private int experienciaNecesaria() {
        return 5 + (nivel - 1) * 3;
    }

    String datosHud() {
        String datos = partidaId + "|" + nivel + "|" + experiencia + "|" + experienciaNecesaria();
        if (!estaEnPausa()) {
            return datos;
        }

        int milisegundos = (int) Math.ceil(tiempoRestante * 1000);
        return datos + "|" + milisegundos + "|" + opciones.get(0) + "," + opciones.get(1) + "," + opciones.get(2)
            + "|" + elecciones[0] + "," + revisiones[0] + "|" + elecciones[1] + "," + revisiones[1];
    }
}
