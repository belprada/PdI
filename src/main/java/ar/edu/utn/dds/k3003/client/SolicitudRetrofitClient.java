package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SolicitudRetrofitClient {

    @GET("solicitudes}")
    Call<SolicitudDTO> get(@Query("hecho_id") String hecho_id);
}