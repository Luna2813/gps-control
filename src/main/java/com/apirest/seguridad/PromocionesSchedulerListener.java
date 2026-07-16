package com.apirest.seguridad;

import com.apirest.db.VehiculoGPSDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class PromocionesSchedulerListener implements ServletContextListener {

    private ScheduledExecutorService ejecutor;

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ejecutor = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "promociones-vencidas");
            hilo.setDaemon(true);
            return hilo;
        });

        ejecutor.scheduleWithFixedDelay(
                () -> new VehiculoGPSDAO().finalizarPromocionesVencidas(),
                0,
                1,
                TimeUnit.HOURS
        );
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        if (ejecutor != null) {
            ejecutor.shutdownNow();
        }
    }
}
