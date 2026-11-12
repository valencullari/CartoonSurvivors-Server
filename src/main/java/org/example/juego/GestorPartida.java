
package org.example.juego;

public class GestorPartida {

    private Partida partida;
    private BucleJuego bucleJuego;

    public synchronized void iniciarPartida(
            String personaje1,
            String personaje2) {

        if (partida != null) {
            System.out.println("Ya existe una partida activa.");
            return;
        }

        partida = new Partida(
                personaje1,
                personaje2
        );

        bucleJuego = new BucleJuego(partida);

        bucleJuego.iniciar();

        System.out.println("Partida creada correctamente.");
        System.out.println("Jugador 1: " + personaje1);
        System.out.println("Jugador 2: " + personaje2);
    }

    public synchronized void moverJugador(
            int idJugador,
            float direccionX,
            float direccionY) {

        if (partida == null) {
            return;
        }

        partida.moverJugador(
                idJugador,
                direccionX,
                direccionY
        );
    }

    public synchronized String obtenerEstado(
            long secuencia) {

        if (partida == null) {
            return null;
        }

        return partida.crearMensajeEstado(secuencia);
    }

    public synchronized void detenerPartida() {

        if (bucleJuego != null) {
            bucleJuego.detener();
            bucleJuego = null;
        }

        partida = null;

        System.out.println("Partida eliminada.");
    }

    public synchronized boolean hayPartidaActiva() {
        return partida != null;
    }
}
