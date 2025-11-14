package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dto.PdIDTO;
import ar.edu.utn.dds.k3003.model.PdI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/PdIs")
public class PdIController {

    private final Fachada fachada;

    @Autowired
    public PdIController(Fachada fachada) {
        this.fachada = fachada;
    }

    @GetMapping
    public ResponseEntity<List<PdIDTO>> listarPdI(
            @RequestParam(value = "hecho", required = false) String hecho,
            @RequestParam(value = "estado", required = false) String estado) {

        if (hecho == null) {
            // Si no se proporciona el parámetro "hecho", devuelve todos los PdIDTO
            List<PdIDTO> pdis = fachada.buscarTodos();

            // Filtrar por estado si se proporciona
            if (estado != null) {
                try {
                    PdI.EstadoProcesamiento estadoEnum = PdI.EstadoProcesamiento.valueOf(estado.toUpperCase());
                    pdis = pdis.stream()
                            .filter(pdi -> estadoEnum.equals(pdi.estadoProcesamiento()))
                            .toList();
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            return ResponseEntity.ok(pdis);
        } else {
            // Si se proporciona el parámetro "hecho", devuelve los PdIDTO filtrados por "hecho"
            return ResponseEntity.ok(fachada.buscarPorHecho(hecho));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdIDTO> obtenerPdI(@PathVariable String id) {
        try {
            return ResponseEntity.ok(fachada.buscarPdIPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<PdIDTO> crearPdI(@RequestBody PdIDTO pdIDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(fachada.procesar(pdIDTO));

    }

    @PostMapping("/lote")
    public ResponseEntity<List<PdIDTO>> crearPdIs(@RequestBody List<PdIDTO> listaDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(fachada.procesarLista(listaDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PdIDTO> actualizarPdIPorId(@PathVariable String id,
                                                     @RequestBody PdIDTO dto) {
        try {
            return ResponseEntity.ok(fachada.actualizarPorId(id, dto));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<List<PdIDTO>> actualizarPorHecho(@RequestParam String hecho,
                                                           @RequestBody PdIDTO dto) {
        try {
            return ResponseEntity.ok(fachada.actualizarPorHecho(hecho, dto));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Nuevo endpoint para reprocesar imagen de un PDI específico
     */
    @PostMapping("/{id}/reprocesar")
    public ResponseEntity<PdIDTO> reprocesarImagen(@PathVariable String id) {
        try {
            return ResponseEntity.ok(fachada.reprocesarImagen(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para obtener solo PDIs con imágenes
     */
    @GetMapping("/con-imagen")
    public ResponseEntity<List<PdIDTO>> listarPdIsConImagen() {
        List<PdIDTO> pdis = fachada.buscarTodos()
                .stream()
                .filter(pdi -> pdi.imagenUrl() != null && !pdi.imagenUrl().trim().isEmpty())
                .toList();
        return ResponseEntity.ok(pdis);
    }

    /**
     * Endpoint para obtener estadísticas de procesamiento
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticas() {
        List<PdIDTO> todos = fachada.buscarTodos();

        long pendientes = todos.stream().filter(p ->
                PdI.EstadoProcesamiento.PENDIENTE.equals(p.estadoProcesamiento())).count();
        long procesando = todos.stream().filter(p ->
                PdI.EstadoProcesamiento.PROCESANDO.equals(p.estadoProcesamiento())).count();
        long completados = todos.stream().filter(p ->
                PdI.EstadoProcesamiento.COMPLETADO.equals(p.estadoProcesamiento())).count();
        long errores = todos.stream().filter(p ->
                PdI.EstadoProcesamiento.ERROR.equals(p.estadoProcesamiento())).count();
        long conImagen = todos.stream().filter(p ->
                p.imagenUrl() != null && !p.imagenUrl().trim().isEmpty()).count();

        EstadisticasDTO stats = new EstadisticasDTO(
                todos.size(),
                conImagen,
                pendientes,
                procesando,
                completados,
                errores
        );

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/borrarTodo")
    public ResponseEntity<String> borrarTodo() {
        return ResponseEntity.ok(fachada.borrarTodo());
    }

    /**
     * DTO para estadísticas
     */
    public record EstadisticasDTO(
            long totalPdIs,
            long conImagen,
            long pendientes,
            long procesando,
            long completados,
            long errores
    ) {}
}