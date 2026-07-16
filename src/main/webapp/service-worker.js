self.addEventListener("push", evento => {
    let datos = {
        titulo: "GPS Control",
        mensaje: "Tiene una nueva notificación",
        url: "./"
    };

    if (evento.data) {
        try {
            datos = { ...datos, ...evento.data.json() };
        } catch {
            datos.mensaje = evento.data.text();
        }
    }

    evento.waitUntil(self.registration.showNotification(datos.titulo, {
        body: datos.mensaje,
        icon: "./favicon.ico",
        badge: "./favicon.ico",
        tag: datos.tipo || "gps-control",
        data: { url: datos.url }
    }));
});

self.addEventListener("notificationclick", evento => {
    evento.notification.close();
    const destino = evento.notification.data?.url || "./";
    evento.waitUntil(clients.matchAll({ type: "window", includeUncontrolled: true })
        .then(ventanas => {
            for (const ventana of ventanas) {
                if ("focus" in ventana) return ventana.focus();
            }
            return clients.openWindow(destino);
        }));
});
