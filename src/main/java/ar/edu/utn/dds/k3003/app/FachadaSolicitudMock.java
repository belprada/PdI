package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import java.util.List;
import java.util.NoSuchElementException;

public class FachadaSolicitudMock implements FachadaSolicitudes {

    public  FachadaSolicitudMock() {  }


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

    @Override
    public boolean estaActivo(String s) {
        return true;
    }

    @Override
    public void setFachadaFuente(FachadaFuente fachadaFuente) {

    }
}
