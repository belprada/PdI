package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.client.SolicitudesProxy;
import ar.edu.utn.dds.k3003.dto.PdIDTO2;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
@Service
public class Fachada {

  private final PdIRepository pdiRepository;
  private final ObjectMapper objectMapper;

  public Fachada(PdIRepository pdiRepository) {
    this.pdiRepository = pdiRepository;
    this.objectMapper = new ObjectMapper();
  }

  public PdIDTO2 procesar(PdIDTO2 var1) {


    PdI pdiNuevo = new PdI(var1.getId(), var1.getHechoId());
    this.pdiRepository.save(pdiNuevo);

    return new PdIDTO2(pdiNuevo.getId(), pdiNuevo.getHecho());
  }

  public PdIDTO2 buscarPdIPorId(String var1) {
    return this.pdiRepository.findById(var1)
            .map(pdi -> new PdIDTO2(pdi.getId(), pdi.getHecho()))
            .orElseThrow(() -> new NoSuchElementException(var1 + " no existe"));
  }

  public List<PdIDTO2> buscarPorHecho(String var1) {
    return this.pdiRepository.findByHecho(var1)
            .stream()
            .map(pdi -> new PdIDTO2(pdi.getId(), pdi.getHecho()))
            .toList();
  }

  public List<PdIDTO2> buscarTodos() {
    return this.pdiRepository.findAll()
            .stream()
            .map(pdi -> new PdIDTO2(pdi.getId(), pdi.getHecho()))
            .toList();
  }
  public List<PdIDTO2> procesarLista(List<PdIDTO2> listaDto) {
    return listaDto.stream()
            .map(this::procesar) // reusa tu procesar() individual
            .toList();
  }
  public PdIDTO2 actualizarPorId(String id, PdIDTO2 dto) {
    PdI existente = pdiRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + id));

    // actualizar campos
    existente.setHecho(dto.getHechoId());

    PdI guardado = pdiRepository.save(existente);
    return new PdIDTO2(guardado.getId(), guardado.getHecho());
  }
  public List<PdIDTO2> actualizarPorHecho(String hecho, PdIDTO2 dto) {
    List<PdI> lista = pdiRepository.findByHecho(hecho);

    if (lista.isEmpty()) {
      throw new NoSuchElementException("No existen PdIs con hecho " + hecho);
    }

    lista.forEach(pdi -> pdi.setHecho(dto.getHechoId()));
    pdiRepository.saveAll(lista);

    return lista.stream()
            .map(pdi -> new PdIDTO2(pdi.getId(), pdi.getHecho()))
            .toList();
  }

}
