package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.armas.Arma;
import org.example.entidades.armas.Pistola;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;

final class ProgresoPartida {
    private static final float DURACION_MEJORAS = 10f;
    private static final int AUMENTO_MINIMO_PORCENTUAL = 5;
    private static final int AUMENTO_MAXIMO_PORCENTUAL = 12;
    private static final int PROBABILIDAD_BALA_EXTRA = 3;

    private record Opcion(String codigo, Runnable aplicar) {
    }

    private final String partidaId = UUID.randomUUID().toString();
    private final List<Jugador> jugadores;
    private final int[] elecciones = new int[2];
    private final int[] revisiones = new int[2];
    private List<List<Opcion>> opciones = List.of();
    private float tiempoRestante;
    private int nivel = 1;
    private int experiencia;

    ProgresoPartida(Jugador jugador1, Jugador jugador2) {
        jugadores = List.of(jugador1, jugador2);
    }

    boolean correspondeAPartida(String id) {
        return partidaId.equals(id);
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
            opciones.get(i).get(Math.max(0, elecciones[i])).aplicar().run();
        }
        iniciarRondaSiCorresponde();
    }

    void elegirMejora(int jugador, String idRecibido, int nivelElegido, int revision, int opcion) {
        if (!estaEnPausa() || !correspondeAPartida(idRecibido) || nivelElegido != nivel
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

    private List<Opcion> sortearOpciones(Jugador jugador) {
        List<Opcion> candidatas = new ArrayList<>();
        for (Arma arma : jugador.getArmas()) {
            candidatas.add(sortearMejoraArma(arma));
        }
        var azar = ThreadLocalRandom.current();
        candidatas.add(opcionPersonal("VELOCIDAD", sortearPorcentaje(azar), jugador::aumentarVelocidad));
        int vida = azar.nextInt(10, 21);
        candidatas.add(new Opcion("VIDA_MAXIMA:" + vida + ":" + (jugador.getNivelVidaMaxima() + 1),
                () -> jugador.aumentarVidaMaxima(vida)));
        candidatas.add(opcionPersonal("REGENERACION", 1, jugador::aumentarRegeneracion));
        int armadura = Math.min(sortearPorcentajeArmadura(azar), jugador.getMargenArmadura());
        if (armadura > 0) {
            candidatas.add(opcionPersonal("ARMADURA", armadura, jugador::aumentarArmadura));
        }
        if (jugador.puedeAumentarCritico()) {
            candidatas.add(opcionPersonal("CRITICO", Jugador.AUMENTO_CRITICO, jugador::aumentarCritico));
        }
        if (jugador.puedeDesbloquearArma()) {
            candidatas.add(new Opcion("ARMA", jugador::desbloquearArma));
        }
        Collections.shuffle(candidatas);
        return candidatas.subList(0, 3);
    }

    private Opcion sortearMejoraArma(Arma arma) {
        var azar = ThreadLocalRandom.current();
        int principal = azar.nextInt(3);
        int danio = sortearAtributo(principal == 0, azar);
        boolean atributoSecundario = principal == 1 || azar.nextInt(100) < 30;
        int intervalo = sortearAtributo(principal == 2, azar);
        if (arma instanceof Pistola pistola) {
            int rebote = atributoSecundario ? 1 : 0;
            int retencion = atributoSecundario ? pistola.mejoraRetencionDisponible() : 0;
            int balaExtra = azar.nextInt(100) < PROBABILIDAD_BALA_EXTRA ? 1 : 0;
            String codigo = "PISTOLA:" + danio + ":" + rebote + ":" + intervalo + ":" + retencion + ":" + balaExtra;
            return new Opcion(codigo, () -> {
                pistola.mejorar(danio, 0, intervalo);
                if (rebote > 0) {
                    pistola.mejorarRebote(retencion);
                }
                if (balaExtra > 0) {
                    pistola.agregarProyectil();
                }
            });
        }
        int alcance = atributoSecundario ? sortearPorcentaje(azar) : 0;
        String codigo = arma.getTipo() + ":" + danio + ":" + alcance + ":" + intervalo;
        return new Opcion(codigo, () -> arma.mejorar(danio, alcance, intervalo));
    }

    private int sortearAtributo(boolean principal, ThreadLocalRandom azar) {
        return principal || azar.nextInt(100) < 30 ? sortearPorcentaje(azar) : 0;
    }

    private int sortearPorcentaje(ThreadLocalRandom azar) {
        return azar.nextInt(AUMENTO_MINIMO_PORCENTUAL, AUMENTO_MAXIMO_PORCENTUAL + 1);
    }

    private int sortearPorcentajeArmadura(ThreadLocalRandom azar) {
        return azar.nextInt(AUMENTO_MINIMO_PORCENTUAL, AUMENTO_MAXIMO_PORCENTUAL + -3);
    }

    private Opcion opcionPersonal(String tipo, int valor, IntConsumer aplicar) {
        return new Opcion(tipo + ":" + valor, () -> aplicar.accept(valor));
    }

    private int experienciaNecesaria() {
        int nivelesAvanzados = Math.max(0, nivel - 5);
        return 5 + Math.min(nivel - 1, 4) * 3 + Math.max(0, nivel - 4) * 2 + nivelesAvanzados * (nivelesAvanzados + 1) / 10;
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

    private String datosOpciones(List<Opcion> lista) {
        return String.join(",", lista.stream().map(Opcion::codigo).toList());
    }
}
