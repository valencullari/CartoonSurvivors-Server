
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

        // Comprobar cantidad de jugadores conectados
        if (cantidadClientes() >= MAX_JUGADORES) {
            return null;
        }

        // Buscar identificador disponible
        int id = 1;

        while (buscarCliente(id) != null
                && buscarCliente(id).isConectado()) {

            id++;
        }

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

            if (cliente.getId() == id && cliente.isConectado()) {
                return cliente;
            }
        }

        return null;
    }

    public synchronized ClienteConectado buscarCliente(
            InetAddress ip,
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

    public synchronized List<ClienteConectado> getClientes() {
        return new ArrayList<>(clientes);
    }

    public synchronized int cantidadClientes() {

        int cantidad = 0;

        for (ClienteConectado cliente : clientes) {

            if (cliente.isConectado()) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public synchronized boolean partidaCompleta() {
        return cantidadClientes() == MAX_JUGADORES;
    }

    public synchronized List<ClienteConectado> detectarDesconectados(
            long tiempoMaximo) {

        List<ClienteConectado> desconectados = new ArrayList<>();

        for (ClienteConectado cliente : clientes) {

            if (cliente.isConectado()
                    && cliente.estaInactivo(tiempoMaximo)) {

                cliente.desconectar();
                desconectados.add(cliente);
            }
        }

        return desconectados;
    }
}
