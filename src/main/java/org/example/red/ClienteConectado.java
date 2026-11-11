
package org.example.red;

import java.net.InetAddress;

public class ClienteConectado {

    private final int id;
    private final InetAddress direccionIP;
    private final int puerto;

    private long ultimoMensaje;

    private boolean conectado;

    public ClienteConectado(
            int id,
            InetAddress direccionIP,
            int puerto) {

        this.id = id;
        this.direccionIP = direccionIP;
        this.puerto = puerto;

        this.ultimoMensaje = System.currentTimeMillis();
        this.conectado = true;
    }

    public int getId() {
        return id;
    }

    public InetAddress getDireccionIP() {
        return direccionIP;
    }

    public int getPuerto() {
        return puerto;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void actualizarActividad() {
        ultimoMensaje = System.currentTimeMillis();
    }

    public long getUltimoMensaje() {
        return ultimoMensaje;
    }

    public boolean estaInactivo(long tiempoMaximo) {
        return System.currentTimeMillis() - ultimoMensaje > tiempoMaximo;
    }

    public void desconectar() {
        conectado = false;
    }
}
