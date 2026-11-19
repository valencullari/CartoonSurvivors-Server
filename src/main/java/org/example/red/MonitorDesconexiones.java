package org.example.red;

import org.example.red.gestor.GestorConexiones;
import org.example.red.gestor.GestorDesconexiones;
import org.example.red.gestor.GestorSesiones;
import org.example.red.modelo.ClienteConectado;
import java.util.List;

public class MonitorDesconexiones {
    private final Object bloqueo;
    private final GestorConexiones conexiones;
    private final GestorDesconexiones detector;
    private final GestorSesiones sesiones;
    private volatile boolean activo;
    private Thread hilo;

    public MonitorDesconexiones(Object bloqueo, GestorConexiones conexiones,
                               GestorDesconexiones detector, GestorSesiones sesiones) {
        this.bloqueo = bloqueo;
        this.conexiones = conexiones;
        this.detector = detector;
        this.sesiones = sesiones;
    }

    public synchronized void iniciar() {
        if (activo) return;
        activo = true;
        hilo = new Thread(this::comprobar, "HiloControlDesconexiones");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void comprobar() {
        while (activo) {
            try {
                Thread.sleep(1000);
                synchronized (bloqueo) {
                    if (!activo) return;
                    List<ClienteConectado> desconectados = detector.detectarDesconexiones(conexiones);
                    if (!desconectados.isEmpty()) {
                        ClienteConectado cliente = desconectados.get(0);
                        System.out.println("Jugador " + cliente.getId() + " desconectado inesperadamente.");
                        sesiones.finalizarSesion(cliente.getDireccionIP(), cliente.getPuerto());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public synchronized void detener() {
        activo = false;
        if (hilo != null) {
            hilo.interrupt();
            hilo = null;
        }
    }
}
