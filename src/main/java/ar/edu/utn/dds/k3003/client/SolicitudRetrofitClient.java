package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface SolicitudRetrofitClient {

    @GET("solicitudes")
    Call<List<SolicitudDTO>> get(@Query("hechoId") String hechoId);
}