package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.armas.Arma;
import org.example.entidades.armas.Pistola;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class ProgresoPartida {
    private static final float DURACION_MEJORAS = 10f;
    private static final int AUMENTO_MINIMO_PORCENTUAL = 5;
    private static final int AUMENTO_MAXIMO_PORCENTUAL = 12;
    private static final int PROBABILIDAD_BALA_EXTRA = 3;

    private record Opcion(String tipo, String codigo, int valor, Arma arma, int alcance,
                          int intervalo, int rebote, int retencion, int balaExtra) {
        Opcion(String tipo, String codigo, int valor) {
            this(tipo, codigo, valor, null, 0, 0, 0, 0, 0);
        }
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
            Opcion opcion = opciones.get(i).get(Math.max(0, elecciones[i]));
            aplicarMejora(jugadores.get(i), opcion);
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
        opciones = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            opciones.add(sortearOpciones(jugador));
        }
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
        candidatas.add(opcionPersonal("VELOCIDAD", sortearPorcentaje(azar)));
        int vida = azar.nextInt(10, 21);
        candidatas.add(new Opcion("VIDA_MAXIMA", "VIDA_MAXIMA:" + vida + ":" + (jugador.getNivelVidaMaxima() + 1), vida));
        candidatas.add(opcionPersonal("REGENERACION", 1));
        int armadura = Math.min(sortearPorcentajeArmadura(azar), jugador.getMargenArmadura());
        if (armadura > 0) {
            candidatas.add(opcionPersonal("ARMADURA", armadura));
        }
        if (jugador.puedeAumentarCritico()) {
            candidatas.add(opcionPersonal("CRITICO", Jugador.AUMENTO_CRITICO));
        }
        if (jugador.puedeDesbloquearArma()) {
            candidatas.add(new Opcion("ARMA", "ARMA", 0));
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
            return new Opcion("PISTOLA", codigo, danio, pistola, 0, intervalo, rebote, retencion, balaExtra);
        }
        int alcance = atributoSecundario ? sortearPorcentaje(azar) : 0;
        String codigo = arma.getTipo() + ":" + danio + ":" + alcance + ":" + intervalo;
        return new Opcion(arma.getTipo(), codigo, danio, arma, alcance, intervalo, 0, 0, 0);
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

    private Opcion opcionPersonal(String tipo, int valor) {
        return new Opcion(tipo, tipo + ":" + valor, valor);
    }

    private void aplicarMejora(Jugador jugador, Opcion opcion) {
        int valor = opcion.valor();
        if (opcion.arma() != null) {
            opcion.arma().mejorar(valor, opcion.alcance(), opcion.intervalo());
            if (opcion.arma() instanceof Pistola pistola) {
                if (opcion.rebote() > 0) {
                    pistola.mejorarRebote(opcion.retencion());
                }
                if (opcion.balaExtra() > 0) {
                    pistola.agregarProyectil();
                }
            }
            return;
        }

        switch (opcion.tipo()) {
            case "VELOCIDAD":
                jugador.aumentarVelocidad(valor);
                break;
            case "VIDA_MAXIMA":
                jugador.aumentarVidaMaxima(valor);
                break;
            case "REGENERACION":
                jugador.aumentarRegeneracion(valor);
                break;
            case "ARMADURA":
                jugador.aumentarArmadura(valor);
                break;
            case "CRITICO":
                jugador.aumentarCritico(valor);
                break;
            case "ARMA":
                jugador.desbloquearArma();
                break;
        }
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
        ArrayList<String> codigos = new ArrayList<>();
        for (Opcion opcion : lista) {
            codigos.add(opcion.codigo());
        }
        return String.join(",", codigos);
    }
}
