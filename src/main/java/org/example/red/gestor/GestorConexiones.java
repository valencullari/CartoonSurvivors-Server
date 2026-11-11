
package org.example.red.gestor;

import org.example.red.modelo.ClienteConectado;

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

        ClienteConectado existente = buscarCliente(ip, puerto);

        if (existente != null) {
            existente.actualizarActividad();
            return existente;
        }

        if (cantidadClientes() >= MAX_JUGADORES) {
            return null;
        }

        int id = 1;

        while (buscarCliente(id) != null) {
            id++;
        }

        ClienteConectado nuevo = new ClienteConectado(
                id, ip, puerto
        );

        clientes.add(nuevo);

        System.out.println(
                "Jugador " + id + " conectado: " + ip
        );

        return nuevo;
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
            if (cliente.isConectado()
                    && cliente.getDireccionIP().equals(ip)
                    && cliente.getPuerto() == puerto) {
                return cliente;
            }
        }

        return null;
    }

    public synchronized boolean eliminarCliente(
            InetAddress ip,
            int puerto) {

        ClienteConectado cliente = buscarCliente(ip, puerto);

        if (cliente == null) {
            return false;
        }

        cliente.desconectar();
        clientes.remove(cliente);

        return true;
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
}
