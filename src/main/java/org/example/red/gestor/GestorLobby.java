
package org.example.red.gestor;

import org.example.red.modelo.ClienteConectado;

public class GestorLobby {

    private final GestorConexiones gestorConexiones;

    public GestorLobby(GestorConexiones gestorConexiones) {
        this.gestorConexiones = gestorConexiones;
    }

    public synchronized boolean marcarListo(int id) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(id);

        if (cliente == null) {
            return false;
        }

        cliente.setListo(true);

        System.out.println(
                "Jugador " + id + " está listo."
        );

        return gestorConexiones.todosListos();
    }

    public synchronized void desmarcarListo(int id) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(id);

        if (cliente != null) {
            cliente.setListo(false);
        }
    }

    public synchronized void reiniciar() {

        for (ClienteConectado cliente :
                gestorConexiones.getClientes()) {

            cliente.setListo(false);
        }
    }
}
