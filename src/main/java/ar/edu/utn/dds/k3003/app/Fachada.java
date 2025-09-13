package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.rest_client.SolicitudesRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class Fachada {

  private final PdIRepository pdiRepository;
  private final SolicitudesRestClient solicitudesRestClient;


  public Fachada(PdIRepository pdiRepository, SolicitudesRestClient solicitudesRestClient) {
    this.pdiRepository = pdiRepository;
    this.solicitudesRestClient = solicitudesRestClient;
  }

  public PdIDTO procesar(PdIDTO pdIDTO) {

    PdI pdiNuevo = dtoToPDI(pdIDTO);
    try {
      log.info("Verificando hecho con id: " + pdiNuevo.getHechoId());
      if(solicitudesRestClient.estaActivo(pdiNuevo.getHechoId())) {
      log.info("Guardando pdi con id: " + pdiNuevo.getHechoId());
      this.pdiRepository.save(pdiNuevo);

      }
    }  catch (Exception e) {
        throw new NoSuchElementException("No existe hecho activo bajo el id " + pdiNuevo.getHechoId());
    }
    return pdiToDto(pdiNuevo);

  }

  public PdIDTO buscarPdIPorId(String var1) {

      log.info("Buscando PdI con id: " + var1);
      PdI pdi = pdiRepository.findById(var1)
              .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + var1));
      return pdiToDto(pdi);

  }

  public List<PdIDTO> buscarPorHecho(String hechoId) {

    List<PdIDTO> pdis;
    try {
      log.info("Buscando PdI por hechoId: " + hechoId);
      pdis = pdiRepository.findByHechoId(hechoId).stream().map(this::pdiToDto).toList();
    } catch (Exception e) {
      throw new NoSuchElementException("No existen PdIs con hecho " + hechoId);
    }

    return pdis;
  }

  public List<PdIDTO> buscarTodos() {
    log.info("Buscando todos los PdIs");
    return this.pdiRepository.findAll()
            .stream()
            .map(this::pdiToDto)
            .toList();
  }

  public List<PdIDTO> procesarLista(List<PdIDTO> listaDto) {
    return listaDto.stream()
            .map(this::procesar) // reusa tu procesar() individual
            .toList();
  }
  public PdIDTO actualizarPorId(String id, PdIDTO dto) {
    PdI existente = pdiRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + id));

    // actualizar campos
    existente.setHechoId(dto.hechoId());

    PdI guardado = pdiRepository.save(existente);
    return new PdIDTO(String.valueOf(guardado.getId()), guardado.getHechoId());
  }

  public List<PdIDTO> actualizarPorHecho(String hecho, PdIDTO dto) {
    List<PdI> lista = pdiRepository.findByHechoId(hecho);

    if (lista.isEmpty()) {
      throw new NoSuchElementException("No existen PdIs con hecho " + hecho);
    }

    lista.forEach(pdi -> pdi.setHechoId(dto.hechoId()));
    pdiRepository.saveAll(lista);

    return lista.stream()
            .map(pdi -> new PdIDTO(String.valueOf(pdi.getId()), pdi.getHechoId()))
            .toList();
  }

  public PdI dtoToPDI(PdIDTO pdiDTO) {
      return new PdI(
              pdiDTO.hechoId(),
              pdiDTO.descripcion(),
              pdiDTO.lugar(),
              pdiDTO.momento(),
              pdiDTO.contenido(),
              pdiDTO.etiquetas());
  }
  private PdIDTO pdiToDto(PdI pdi) {
    return new PdIDTO(
            String.valueOf(pdi.getId()),
            pdi.getHechoId(),
            pdi.getDescripcion(),
            pdi.getLugar(),
            pdi.getMomento(),
            pdi.getContenido(),
            pdi.getEtiquetas()
    );
  }
  public String borrarTodo() {
    pdiRepository.deleteAll();
    return "Se eliminaron todas las solicitudes";
  }
}
