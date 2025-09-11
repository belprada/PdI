package ar.edu.utn.dds.k3003.rest_client;

import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SolicitudesRestClient {

    private final RestClient restClient;

    public SolicitudesRestClient() {
        var env = System.getenv();
        String endpoint = env.getOrDefault("DDS_SOLICITUDES", "http://localhost:8080");

        this.restClient = RestClient.builder()
                .baseUrl(endpoint)
                .build();
    }

    public List<SolicitudDTO> findSolicitudesByHechoId(String id) {
        return restClient.get()
                .uri("/api/solicitudes/{hechoId}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitudDTO>>() {});
    }

//    public boolean estaActivo

}
