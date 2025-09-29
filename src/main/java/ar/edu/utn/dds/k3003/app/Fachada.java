package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.dto.PdIDTO;
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

  public Fachada(PdIRepository pdiRepository,
                 SolicitudesRestClient solicitudesRestClient,
                 ImageAnalysisService imageAnalysisService) {
    this.pdiRepository = pdiRepository;
    this.solicitudesRestClient = solicitudesRestClient;
    this.imageAnalysisService = imageAnalysisService;
  }

  public PdIDTO procesar(PdIDTO pdIDTO) {
    PdI pdiNuevo = dtoToPDI(pdIDTO);

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

        return pdiToDto(pdiGuardado);
      }
    } catch (Exception e) {
      throw new NoSuchElementException("No existe hecho activo bajo el id " + pdiNuevo.getHechoId());
    }

    return pdiToDto(pdiNuevo);
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
      pdi.setOcrText(result.getOcrText());
      pdi.setEtiquetasIA(result.getLabels());
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

  /**
   * Método para reprocesar PDIs que fallaron o están pendientes
   */
  public PdIDTO reprocesarImagen(String pdiId) {
    PdI pdi = pdiRepository.findById(pdiId)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + pdiId));

    if (!StringUtils.hasText(pdi.getImagenUrl())) {
      throw new IllegalStateException("PDI no tiene imagen para procesar");
    }

    procesarImagen(pdi);
    return pdiToDto(pdi);
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
            .map(this::procesar)
            .toList();
  }

  public PdIDTO actualizarPorId(String id, PdIDTO dto) {
    PdI existente = pdiRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No existe PdI con id " + id));

    // Verificar si cambió la URL de imagen
    boolean imagenCambio = !StringUtils.hasText(existente.getImagenUrl()) &&
            StringUtils.hasText(dto.imagenUrl()) ||
            StringUtils.hasText(existente.getImagenUrl()) &&
                    !existente.getImagenUrl().equals(dto.imagenUrl());

    // Actualizar campos
    existente.setHechoId(dto.hechoId());
    existente.setDescripcion(dto.descripcion());
    existente.setLugar(dto.lugar());
    existente.setMomento(dto.momento());
    existente.setContenido(dto.contenido());
    existente.setImagenUrl(dto.imagenUrl());

    // Si cambió la imagen, reprocesar
    if (imagenCambio && StringUtils.hasText(dto.imagenUrl())) {
      existente.setEstadoProcesamiento(PdI.EstadoProcesamiento.PENDIENTE);
      existente.setOcrText(null);
      existente.setEtiquetasIA(null);
      existente.setFechaProcesamiento(null);
    }

    PdI guardado = pdiRepository.save(existente);

    // Procesar imagen si es necesario
    if (imagenCambio && StringUtils.hasText(dto.imagenUrl())) {
      procesarImagen(guardado);
    }

    return pdiToDto(guardado);
  }

  public List<PdIDTO> actualizarPorHecho(String hecho, PdIDTO dto) {
    List<PdI> lista = pdiRepository.findByHechoId(hecho);

    if (lista.isEmpty()) {
      throw new NoSuchElementException("No existen PdIs con hecho " + hecho);
    }

    lista.forEach(pdi -> {
      pdi.setHechoId(dto.hechoId());
      // Aplicar otros campos según necesidad
    });
    pdiRepository.saveAll(lista);

    return lista.stream()
            .map(this::pdiToDto)
            .toList();
  }

  public PdI dtoToPDI(PdIDTO pdiDTO) {
    if (StringUtils.hasText(pdiDTO.imagenUrl())) {
      // Constructor para PDI con imagen
      return new PdI(
              pdiDTO.hechoId(),
              pdiDTO.descripcion(),
              pdiDTO.lugar(),
              pdiDTO.momento(),
              pdiDTO.contenido(),
              pdiDTO.imagenUrl());
    } else {
      // Constructor legacy para PDI sin imagen
      return new PdI(
              pdiDTO.hechoId(),
              pdiDTO.descripcion(),
              pdiDTO.lugar(),
              pdiDTO.momento(),
              pdiDTO.contenido(),
              pdiDTO.etiquetas());
    }
  }

  private PdIDTO pdiToDto(PdI pdi) {
    return PdIDTO.fromProcessedEntity(pdi);
  }

  public String borrarTodo() {
    pdiRepository.deleteAll();
    return "Se eliminaron todas las solicitudes";
  }
}