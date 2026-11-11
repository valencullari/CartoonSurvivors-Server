
package org.example.red.gestor;

import org.example.red.modelo.ClienteConectado;

public class GestorLobby {

    private boolean partidaIniciada = false;
    private boolean partidaFinalizada = false;

    public synchronized boolean marcarListo(
            ClienteConectado cliente,
            boolean listo) {

        if (partidaIniciada || partidaFinalizada) {
            return false;
        }

        cliente.setListo(listo);

        return true;
    }

    public synchronized boolean todosListos(
            GestorConexiones gestor) {

        if (partidaIniciada || partidaFinalizada) {
            return false;
        }

        if (!gestor.partidaCompleta()) {
            return false;
        }

        for (ClienteConectado cliente : gestor.getClientes()) {

            if (!cliente.estaListo()) {
                return false;
            }
        }

        return true;
    }

    public synchronized boolean iniciarPartida(
            GestorConexiones gestor) {

        if (!todosListos(gestor)) {
            return false;
        }

        partidaIniciada = true;

        return true;
    }

    public synchronized void reiniciarConfirmaciones(
            GestorConexiones gestor) {

        for (ClienteConectado cliente : gestor.getClientes()) {
            cliente.setListo(false);
        }
    }

    public synchronized void finalizarPartida() {
        partidaFinalizada = true;
    }

    public synchronized boolean estaIniciada() {
        return partidaIniciada;
    }

    public synchronized boolean estaFinalizada() {
        return partidaFinalizada;
    }

    public synchronized boolean estaEnLobby() {
        return !partidaIniciada && !partidaFinalizada;
    }
}
