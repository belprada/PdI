package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.HttpStatus;
import java.util.*;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class SolicitudesProxy implements FachadaSolicitudes {

    private final String endpoint;
    private final SolicitudRetrofitClient service;

    public SolicitudesProxy(ObjectMapper objectMapper) {

        var env = System.getenv();
        this.endpoint = env.getOrDefault("https://two025-tp-entrega-2-stephieortiz.onrender.com/", "http://localhost:8081/");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(SolicitudRetrofitClient.class);
    }

    public SolicitudesProxy(String endpoint, SolicitudRetrofitClient service) {
        this.endpoint = endpoint;
        this.service = service;
    }

    @Override
    public SolicitudDTO agregar(SolicitudDTO solicitudDTO) {
        return null;
    }

    @Override
    public SolicitudDTO modificar(String s, EstadoSolicitudBorradoEnum estadoSolicitudBorradoEnum, String s1) throws NoSuchElementException {
        return null;
    }

    @Override
    public List<SolicitudDTO> buscarSolicitudXHecho(String s) {
        return List.of();
    }

    @Override
    public SolicitudDTO buscarSolicitudXId(String s) {
        return null;
    }

    @SneakyThrows
    @Override
    public boolean estaActivo(String s) {
        Response<SolicitudDTO> execute = service.get(s).execute();
        if (execute.isSuccessful()) {

            return !execute.body().estado().equals(EstadoSolicitudBorradoEnum.RECHAZADA);
        }
        if(execute.code() == HttpStatus.NOT_FOUND.getCode()) {
            throw new NoSuchElementException("no est activo la solicitud");
        }
        throw new RuntimeException("Error conectandose con solicitudes");
    }

    @Override
    public void setFachadaFuente(FachadaFuente fachadaFuente) {

    }
}