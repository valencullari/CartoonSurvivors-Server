package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.armas.Arma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

final class ProgresoPartida {
    private static final float DURACION_MEJORAS = 10f;

    private enum Mejora {
        DANIO, ALCANCE, CADENCIA, VELOCIDAD, ARMA, VIDA_MAXIMA, REGENERACION;

        void aplicar(Jugador jugador) {
            switch (this) {
                case DANIO -> jugador.getArmas().forEach(Arma::aumentarDanio);
                case ALCANCE -> jugador.getArmas().forEach(Arma::aumentarAlcance);
                case CADENCIA -> jugador.getArmas().forEach(Arma::aumentarCadencia);
                case VELOCIDAD -> jugador.aumentarVelocidad();
                case ARMA -> jugador.desbloquearArma();
                case VIDA_MAXIMA -> jugador.aumentarVidaMaxima();
                case REGENERACION -> jugador.aumentarRegeneracion();
            }
        }
    }

    private final String partidaId = UUID.randomUUID().toString();
    private final List<Jugador> jugadores;
    private final int[] elecciones = new int[2];
    private final int[] revisiones = new int[2];
    private List<List<Mejora>> opciones = List.of();
    private float tiempoRestante;
    private int nivel = 1;
    private int experiencia;

    ProgresoPartida(Jugador jugador1, Jugador jugador2) {
        jugadores = List.of(jugador1, jugador2);
    }

    void agregarExperiencia(int bajas) {
        experiencia += bajas;
        iniciarRondaSiCorresponde();
    }

    boolean estaEnPausa() {
        return tiempoRestante > 0f;
    }

    void actualizarPausa(float delta) {
        tiempoRestante = Math.max(0f, tiempoRestante - delta);
        if (estaEnPausa()) {
            return;
        }

        finalizarRonda();
    }

    private void finalizarRonda() {
        tiempoRestante = 0f;
        for (int i = 0; i < jugadores.size(); i++) {
            opciones.get(i).get(Math.max(0, elecciones[i])).aplicar(jugadores.get(i));
        }
        iniciarRondaSiCorresponde();
    }

    void elegirMejora(int jugador, String idRecibido, int nivelElegido, int revision, int opcion) {
        if (!estaEnPausa() || !partidaId.equals(idRecibido) || nivelElegido != nivel
                || jugador < 1 || jugador > 2 || opcion < 0 || opcion >= opciones.get(jugador - 1).size()) {
            return;
        }

        int indice = jugador - 1;
        if (revision > revisiones[indice]) {
            elecciones[indice] = opcion;
            revisiones[indice] = revision;
            if (revisiones[0] > 0 && revisiones[1] > 0) {
                finalizarRonda();
            }
        }
    }

    private void iniciarRondaSiCorresponde() {
        if (experiencia < experienciaNecesaria()) {
            return;
        }

        experiencia -= experienciaNecesaria();
        nivel++;
        opciones = jugadores.stream().map(this::sortearOpciones).toList();
        elecciones[0] = elecciones[1] = -1;
        revisiones[0] = revisiones[1] = 0;
        tiempoRestante = DURACION_MEJORAS;
    }

    private List<Mejora> sortearOpciones(Jugador jugador) {
        List<Mejora> candidatas = new ArrayList<>(List.of(Mejora.values()));
        if (!jugador.puedeDesbloquearArma()) {
            candidatas.remove(Mejora.ARMA);
        }
        Collections.shuffle(candidatas);
        return candidatas.subList(0, 3);
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
        return datos + "|" + milisegundos + "|" + datosOpciones(opciones.get(0)) + ";" + datosOpciones(opciones.get(1))
            + "|" + elecciones[0] + "," + revisiones[0] + "|" + elecciones[1] + "," + revisiones[1];
    }

    private String datosOpciones(List<Mejora> lista) {
        return String.join(",", lista.stream().map(Mejora::name).toList());
    }
}
