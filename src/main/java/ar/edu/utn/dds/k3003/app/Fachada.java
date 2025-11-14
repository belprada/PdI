package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.dto.PdIDTO;
import ar.edu.utn.dds.k3003.mappers.PdIMapper;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.rest_client.SolicitudesRestClient;
import ar.edu.utn.dds.k3003.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import io.micrometer.core.instrument.MeterRegistry;
import java.net.HttpURLConnection;
import java.net.URL;


import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class Fachada {

  private final PdIRepository pdiRepository;
  private final SolicitudesRestClient solicitudesRestClient;
  private final ImageAnalysisService imageAnalysisService;
  private final PdIMapper mapper;
  private final MeterRegistry meterRegistry; //Datadog

    private void registrarMetricasProcesamiento(PdI pdi, String origen, long startNanos) {
    String tipo = StringUtils.hasText(pdi.getImagenUrl()) ? "con_imagen" : "sin_imagen";
    String resultado;

    if (pdi.getEstadoProcesamiento() == PdI.EstadoProcesamiento.COMPLETADO) {
      resultado = "ok";
    } else if (pdi.getEstadoProcesamiento() == PdI.EstadoProcesamiento.ERROR) {
      resultado = "error";
    } else {
      resultado = "otro";
    }

    long duracionNanos = System.nanoTime() - startNanos;

    // Contador de PDIs procesados
    meterRegistry.counter(
            "metamapa.pdi.procesados",
            "resultado", resultado,
            "tipo", tipo,
            "origen", origen
    ).increment();

    // Tiempo de procesamiento de un PDI
    meterRegistry.timer(
            "metamapa.pdi.procesamiento",
            "resultado", resultado,
            "tipo", tipo,
            "origen", origen
    ).record(duracionNanos, TimeUnit.NANOSECONDS);
  }

  private void validarAccesibilidadImagen(String imagenUrl) {
    if (!StringUtils.hasText(imagenUrl)) {
      throw new IllegalArgumentException("URL de imagen vacía");
    }

    try {
      URL url = new URL(imagenUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("HEAD");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      int responseCode = connection.getResponseCode();

      if (responseCode >= 400) {
        throw new IllegalStateException(
                "Imagen no accesible. Código HTTP: " + responseCode
        );
      }
    } catch (Exception e) {
      // Cualquier problema de conexión/parsing lo consideramos error de imagen
      throw new IllegalStateException("No se pudo acceder a la imagen: " + e.getMessage(), e);
    }
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
          // Si no tiene imagen, medir el procesamiento "rápido"
          long start = System.nanoTime();
          // Si no tiene imagen, marcar como completado
          pdiGuardado.setEstadoProcesamiento(PdI.EstadoProcesamiento.COMPLETADO);
          pdiGuardado.setFechaProcesamiento(LocalDateTime.now());
          pdiGuardado = this.pdiRepository.save(pdiGuardado);

          registrarMetricasProcesamiento(pdiGuardado, "procesar", start);
        }

        return mapper.toDto(pdiGuardado);
      }
    } catch (Exception e) {
      meterRegistry.counter(
              "metamapa.pdi.procesados",
              "resultado", "error",
              "tipo", "desconocido",
              "origen", "procesar"
      ).increment();
      throw new NoSuchElementException("No existe hecho activo bajo el id " + pdiNuevo.getHechoId());
    }

    return mapper.toDto(pdiNuevo);
  }

  private void procesarImagen(PdI pdi) throws Exception{

    long start = System.nanoTime();
    try {
      log.info("Iniciando procesamiento de imagen para PDI id: {}", pdi.getId());

      // Marcar como procesando
      pdi.setEstadoProcesamiento(PdI.EstadoProcesamiento.PROCESANDO);
      pdiRepository.save(pdi);
      //validar que la imagen existe
      //validarAccesibilidadImagen(pdi.getImagenUrl());

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
      throw new Exception("Error al procesar imagen");

    }   finally {
    // Siempre registramos métricas con el estado final del PDI
    registrarMetricasProcesamiento(pdi, "procesar_imagen", start);
  }
  }

  public PdIDTO reprocesarImagen(String pdiId) throws Exception {
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

  public PdIDTO actualizarPorId(String id, PdIDTO dto) throws Exception {
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
