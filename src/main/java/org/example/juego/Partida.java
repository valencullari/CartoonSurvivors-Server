package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.Enemigo;
import org.example.entidades.TipoEnemigo;
import org.example.entidades.armas.Arma;
import org.example.red.protocolo.Protocolo;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class Partida {
    private static final int MAX_BYTES_ENEMIGOS_POR_BLOQUE = 750;

    private final ProgresoPartida progreso;
    private final PausaPartida pausa = new PausaPartida();
    private final Jugador jugador1;
    private final Jugador jugador2;

    private final GestorEnemigos gestorEnemigos;
    private final DificultadPartida dificultad;
    private final GeneradorEnemigos generadorEnemigos;
    private final SistemaColisiones sistemaColisiones;
    private String resultado;

    public Partida(String personaje1, String personaje2) {

        jugador1 = new Jugador(1, personaje1, 11600, 11600);
        jugador2 = new Jugador(2, personaje2, 6460, 6400);
        progreso = new ProgresoPartida(jugador1, jugador2);
        gestorEnemigos = new GestorEnemigos();
        dificultad = new DificultadPartida();
        generadorEnemigos = new GeneradorEnemigos(gestorEnemigos, dificultad);
        sistemaColisiones = new SistemaColisiones();
    }

    public synchronized void moverJugador(int id, float direccionX, float direccionY) {

        if (resultado != null || pausa.estaActiva() || progreso.estaEnPausa()) {
            return;
        }

        Jugador jugador = obtenerJugador(id);

        if (jugador != null) {
            jugador.establecerMovimiento(direccionX, direccionY);
        }
    }


    public synchronized void actualizar(float delta) {
        if (resultado != null || pausa.estaActiva()) {
            return;
        }
        if (progreso.estaEnPausa()) {
            progreso.actualizarPausa(delta);
            return;
        }
        if (!dificultad.estaEnFaseFinal()) {
            jugador1.actualizarReaparicion(delta);
            jugador2.actualizarReaparicion(delta);
        }

        dificultad.actualizar(delta);
        boolean faseFinal = dificultad.estaEnFaseFinal();

        jugador1.actualizar(delta);
        jugador2.actualizar(delta);
        if (faseFinal) {
            generadorEnemigos.actualizarFaseFinal(jugador1, jugador2);
        } else {
            generadorEnemigos.actualizar(delta, jugador1, jugador2);
        }
        int experienciaAnterior = gestorEnemigos.getExperienciaPorBajas();
        float multiplicadorDanio = dificultad.getMultiplicadorDanio();
        gestorEnemigos.actualizar(delta, jugador1, jugador2, multiplicadorDanio);
        jugador1.actualizarArmas(delta, gestorEnemigos);
        jugador2.actualizarArmas(delta, gestorEnemigos);
        sistemaColisiones.actualizar(delta, jugador1, jugador2, gestorEnemigos, multiplicadorDanio);
        if (!jugador1.estaVivo() && !jugador2.estaVivo()) {
            resultado = faseFinal ? "VICTORIA" : "DERROTA";
        } else if (!faseFinal) {
            progreso.agregarExperiencia(gestorEnemigos.getExperienciaPorBajas() - experienciaAnterior);
        }

        if (progreso.estaEnPausa()) {
            jugador1.establecerMovimiento(0f, 0f);
            jugador2.establecerMovimiento(0f, 0f);
        }
    }


    public synchronized Jugador obtenerJugador(int id) {

        if (id == 1) {
            return jugador1;
        }

        if (id == 2) {
            return jugador2;
        }

        return null;
    }

    public synchronized String crearMensajeEstado(long secuencia) {

        return "ESTADO|" + secuencia + "|" + datosJugador(jugador1) + "|" + datosJugador(jugador2);
    }

    public synchronized String crearMensajeHud(long secuencia) {
        return Protocolo.HUD + "|" + secuencia + "|" + dificultad.getTiempoTranscurrido()
            + "|" + gestorEnemigos.getEnemigosEliminados() + "|" + progreso.datosHud()
            + sufijoResultado() + pausa.datosHud();
    }

    private String sufijoResultado() {
        if (resultado != null) {
            return "|" + resultado;
        }
        return dificultad.estaEnFaseFinal() ? "|FASE_FINAL" : "";
    }

    public synchronized void elegirMejora(int id, String partidaId, int nivel, int revision, int opcion) {
        if (!pausa.estaActiva()) {
            progreso.elegirMejora(id, partidaId, nivel, revision, opcion);
        }
    }

    public synchronized void gestionarPausa(int id, String partidaId, int revision, boolean reanudar) {
        if (resultado != null || obtenerJugador(id) == null || !progreso.correspondeAPartida(partidaId)) {
            return;
        }
        if (reanudar) {
            pausa.confirmar(id, revision);
        } else if (pausa.abrir(id, revision)) {
            jugador1.establecerMovimiento(0f, 0f);
            jugador2.establecerMovimiento(0f, 0f);
        }
    }

    public String crearMensajePersonajes() {
        return Protocolo.PERSONAJES + "|1:" + jugador1.getPersonaje() + "|2:" + jugador2.getPersonaje();
    }

    private String datosJugador(Jugador jugador) {

        return jugador.getId() + ":" + jugador.getPosicionX() + ":" + jugador.getPosicionY()
            + ":" + jugador.getDireccionX() + ":" + jugador.getDireccionY() + ":" + jugador.getVida()
            + ":" + datosArmas(jugador) + ":" + jugador.getVidaMaxima()
            + ":" + jugador.getSegundosParaReaparecer() + ":" + jugador.getVelocidad()
            + ":" + jugador.getRegeneracion() + ":" + jugador.getArmadura() + ":" + jugador.getProbabilidadCritico()
            + ":" + jugador.getLadoMirada();
    }

    private String datosArmas(Jugador jugador) {

        StringBuilder datos = new StringBuilder();
        List<Arma> armas = jugador.getArmas();
        for (int i = 0; i < armas.size(); i++) {
            Arma arma = armas.get(i);
            if (i > 0) {
                datos.append(";");
            }
            datos.append(i).append(",").append(arma.getTipo()).append(",").append(arma.getNumeroAtaque()).append(",").append(arma.getDireccionAtaque())
                .append(",").append(arma.getAlcanceAtaque());
        }

        return datos.toString();
    }


    public synchronized List<String> crearMensajesEnemigos(long secuencia) {

        List<String> entradas = new ArrayList<>();
        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {
            String entrada = enemigo.getId() + ":" + enemigo.getTipo().name() + ":" + enemigo.getPosicionX()
                + ":" + enemigo.getPosicionY() + ":" + (int) Math.ceil(enemigo.getVida());
            if (enemigo.getTipo() == TipoEnemigo.JEFE) {
                entrada += ":" + enemigo.getVidaMaxima();
            }
            entradas.add(entrada);
        }
        gestorEnemigos.getAtaqueFuego().agregarEstado(entradas);
        gestorEnemigos.agregarEstadoBalas(entradas);

        List<String> bloquesDatos = new ArrayList<>();

        StringBuilder bloqueActual = new StringBuilder();

        for (String datos : entradas) {

            String entrada = "|" + datos;

            int bytesActuales = bloqueActual.toString().getBytes(StandardCharsets.UTF_8).length;

            int bytesEntrada = entrada.getBytes(StandardCharsets.UTF_8).length;

            if (bytesActuales + bytesEntrada > MAX_BYTES_ENEMIGOS_POR_BLOQUE && bloqueActual.length() > 0) {

                bloquesDatos.add(bloqueActual.toString());

                bloqueActual = new StringBuilder();
            }

            bloqueActual.append(entrada);
        }

        // Agregar el último bloque.
        // También sirve cuando no existen enemigos.
        bloquesDatos.add(bloqueActual.toString());

        List<String> mensajes = new ArrayList<>();

        int totalBloques = bloquesDatos.size();

        for (int i = 0; i < totalBloques; i++) {

            String mensaje = "ENEMIGOS" + "|" + secuencia + "|" + i + "|" + totalBloques + bloquesDatos.get(i);

            mensajes.add(mensaje);
        }

        return mensajes;
    }


    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }
}
