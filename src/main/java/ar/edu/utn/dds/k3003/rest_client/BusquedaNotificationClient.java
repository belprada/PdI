package ar.edu.utn.dds.k3003.rest_client;

import ar.edu.utn.dds.k3003.dto.PdIDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class BusquedaNotificationClient {

    private final RestClient restClient;
    private final boolean enabled;

    public BusquedaNotificationClient() {
        String endpoint = System.getenv().getOrDefault("DDS_BUSQUEDA", "http://localhost:8085");
        this.enabled = !endpoint.isBlank();

        if (enabled) {
            this.restClient = RestClient.builder()
                    .baseUrl(endpoint)
                    .build();
            log.info("✅ BusquedaNotificationClient configurado: {}", endpoint);
        } else {
            this.restClient = null;
            log.warn("⚠️ DDS_BUSQUEDA no configurado");
        }
    }

    @Async
    public void notificarPdICreado(PdIDTO pdi) {
        if (!enabled) return;

        try {
            log.info("📤 Notificando PDI procesado: {}", pdi.id());
            restClient.post()
                    .uri("/api/indexacion/pdi")
                    .body(pdi)
                    .retrieve()
                    .toBodilessEntity();
            log.info("✅ PDI notificado: {}", pdi.id());
        } catch (Exception e) {
            log.error("❌ Error notificando PDI {}: {}", pdi.id(), e.getMessage());
        }
    }
}