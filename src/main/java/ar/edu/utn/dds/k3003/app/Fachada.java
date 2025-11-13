package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.dto.PdIDTO;
import ar.edu.utn.dds.k3003.mappers.PdIMapper;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.rest_client.SolicitudesRestClient;
import ar.edu.utn.dds.k3003.service.ImageAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class Fachada {

  private final PdIRepository pdiRepository;
  private final SolicitudesRestClient solicitudesRestClient;
  private final ImageAnalysisService imageAnalysisService;
  private final PdIMapper mapper;

  public Fachada(PdIRepository pdiRepository,
                 SolicitudesRestClient solicitudesRestClient,
                 ImageAnalysisService imageAnalysisService,
                 PdIMapper mapper) {
    this.pdiRepository = pdiRepository;
    this.solicitudesRestClient = solicitudesRestClient;
    this.imageAnalysisService = imageAnalysisService;
    this.mapper = mapper;
  }


  public PdIDTO procesar(PdIDTO pdIDTO) {
    PdI pdiNuevo = mapper.toEntity(pdIDTO);

    try {
      log.info("Verificando hecho con id: " + pdiNuevo.getHechoId());
      if(solicitudesRestClient.estaActivo(pdiNuevo.getHechoId())) {
        log.info("Guardando pdi con id: " + pdiNuevo.getHechoId());

        // Guardar primero el PDI en estado PENDIENTE
        pdiNuevo.setEstadoProcesamiento(PdI.EstadoProcesamiento.PENDIENTE);
        PdI pdiGuardado = this.pdiRepository.save(pdiNuevo);

        // Si tiene imagen, procesar análisis
        if (StringUtils.hasText(pdiGuardado.getImagenUrl())) {
          procesarImagen(pdiGuardado);
        } else {
          // Si no tiene imagen, marcar como completado
          pdiGuardado.setEstadoProcesamiento(PdI.EstadoProcesamiento.COMPLETADO);
          pdiGuardado.setFechaProcesamiento(LocalDateTime.now());
          pdiGuardado = this.pdiRepository.save(pdiGuardado);
        }

        return mapper.toDto(pdiGuardado);
      }
    } catch (Exception e) {
      throw new NoSuchElementException("No existe hecho activo bajo el id " + pdiNuevo.getHechoId());
    }

    return mapper.toDto(pdiNuevo);
  }

  private void procesarImagen(PdI pdi) {
    try {
      log.info("Iniciando procesamiento de imagen para PDI id: {}", pdi.getId());

      // Marcar como procesando
      pdi.setEstadoProcesamiento(PdI.EstadoProcesamiento.PROCESANDO);
      pdiRepository.save(pdi);

      // Procesar la imagen
      ImageAnalysisService.ImageAnalysisResult result =
              imageAnalysisService.processImage(pdi.getImagenUrl());

      // Actualizar PDI con los resultados
      pdi.setOcrText(result.ocrText());
      pdi.setEtiquetasIA(result.labels());
      pdi.setEstadoProcesamiento(PdI.EstadoProcesamiento.COMPLETADO);
      pdi.setFechaProcesamiento(LocalDateTime.now());

      pdiRepository.save(pdi);

      log.info("Procesamiento de imagen completado para PDI id: {}", pdi.getId());

    } catch (Exception e) {
      log.error("Error procesando imagen para PDI id {}: {}", pdi.getId(), e.getMessage());

      // Marcar como error
      pdi.setEstadoProcesamiento(PdI.EstadoProcesamiento.ERROR);
      pdi.setFechaProcesamiento(LocalDateTime.now());
      pdiRepository.save(pdi);
    }
  }

  public PdIDTO reprocesarImagen(String pdiId) {
    PdI pdi = pdiRepository.findById(pdiId)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + pdiId));

    if (!StringUtils.hasText(pdi.getImagenUrl())) {
      throw new IllegalStateException("PDI no tiene imagen para procesar");
    }

    procesarImagen(pdi);
    return mapper.toDto(pdi);
  }

  public PdIDTO buscarPdIPorId(String var1) {
    log.info("Buscando PdI con id: " + var1);
    PdI pdi = pdiRepository.findById(var1)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + var1));
    return mapper.toDto(pdi);
  }

  public List<PdIDTO> buscarPorHecho(String hechoId) {
    try {
      log.info("Buscando PdI por hechoId: " + hechoId);
      return pdiRepository.findByHechoId(hechoId)
              .stream()
              .map(mapper::toDto)
              .toList();
    } catch (Exception e) {
      throw new NoSuchElementException("No existen PdIs con hecho " + hechoId);
    }
  }

  public List<PdIDTO> buscarTodos() {
    log.info("Buscando todos los PdIs");
    return this.pdiRepository.findAll()
            .stream()
            .map(mapper::toDto)
            .toList();
  }

  public List<PdIDTO> procesarLista(List<PdIDTO> listaDto) {
    return listaDto.stream()
            .map(this::procesar)
            .toList();
  }

  public PdIDTO actualizarPorId(String id, PdIDTO dto) {
    PdI existente = pdiRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + id));

    boolean imagenCambio = !StringUtils.hasText(existente.getImagenUrl()) &&
            StringUtils.hasText(dto.imagenUrl()) ||
            StringUtils.hasText(existente.getImagenUrl()) &&
                    !existente.getImagenUrl().equals(dto.imagenUrl());

    // Usar mapper para actualizar los campos del entity con el DTO
    mapper.updateEntityFromDto(dto, existente);

    if (imagenCambio && StringUtils.hasText(dto.imagenUrl())) {
      existente.setEstadoProcesamiento(PdI.EstadoProcesamiento.PENDIENTE);
      existente.setOcrText(null);
      existente.setEtiquetasIA(null);
      existente.setFechaProcesamiento(null);
    }

    PdI guardado = pdiRepository.save(existente);

    if (imagenCambio && StringUtils.hasText(dto.imagenUrl())) {
      procesarImagen(guardado);
    }

    return mapper.toDto(guardado);
  }

  public List<PdIDTO> actualizarPorHecho(String hecho, PdIDTO dto) {
    List<PdI> lista = pdiRepository.findByHechoId(hecho);

    if (lista.isEmpty()) {
      throw new NoSuchElementException("No existen PdIs con hecho " + hecho);
    }

    lista.forEach(pdi -> mapper.updateEntityFromDto(dto, pdi));
    pdiRepository.saveAll(lista);

    return lista.stream()
            .map(mapper::toDto)
            .toList();
  }

  public String borrarTodo() {
    pdiRepository.deleteAll();
    return "Se eliminaron todas las solicitudes";
  }
}
