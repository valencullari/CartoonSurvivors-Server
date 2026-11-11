
package org.example.red;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GestorConexiones {

    private static final int MAX_JUGADORES = 2;

    private final List<ClienteConectado> clientes;

    public GestorConexiones() {
        clientes = new ArrayList<>();
    }

    public synchronized ClienteConectado registrarCliente(
            InetAddress ip,
            int puerto) {

        // Comprobar si el cliente ya está registrado
        for (ClienteConectado cliente : clientes) {

            if (cliente.isConectado()
                    && cliente.getDireccionIP().equals(ip)
                    && cliente.getPuerto() == puerto) {

                cliente.actualizarActividad();
                return cliente;
            }
        }

        // Comprobar límite de jugadores
        if (clientes.size() >= MAX_JUGADORES) {
            return null;
        }

        // Buscar identificador disponible
        int id = clientes.size() + 1;

        ClienteConectado nuevoCliente =
                new ClienteConectado(id, ip, puerto);

        clientes.add(nuevoCliente);

        System.out.println(
                "Jugador " + id + " conectado: " + ip
        );

        return nuevoCliente;
    }

    public synchronized ClienteConectado buscarCliente(int id) {

        for (ClienteConectado cliente : clientes) {

            if (cliente.getId() == id) {
                return cliente;
            }
        }

        return null;
    }




    public synchronized List<ClienteConectado> getClientes() {
        return new ArrayList<>(clientes);
    }

    public synchronized int cantidadClientes() {
        return clientes.size();
    }

    public synchronized boolean partidaCompleta() {
        return clientes.size() == MAX_JUGADORES;
    }


    public synchronized ClienteConectado buscarCliente(
            java.net.InetAddress ip,
            int puerto) {

        for (ClienteConectado cliente : clientes) {

            if (cliente.getDireccionIP().equals(ip)
                    && cliente.getPuerto() == puerto
                    && cliente.isConectado()) {

                return cliente;
            }
        }

        return null;
    }

}
