package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<List<PdIDTO>> listarPdI(@RequestParam(value = "hecho", required = false) String hecho) {
        if (hecho == null) {
            // Si no se proporciona el parámetro "hecho", devuelve todos los PdIDTO
            return ResponseEntity.ok(fachada.buscarTodos());
        } else {
            // Si se proporciona el parámetro "hecho", devuelve los PdIDTO filtrados por "hecho"
            return ResponseEntity.ok(fachada.buscarPorHecho(hecho));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdIDTO> obtenerPdI(@PathVariable String id) {
        return ResponseEntity.ok(fachada.buscarPdIPorId(id));
    }

    @PostMapping
    public ResponseEntity<PdIDTO> crearPdI(@RequestBody PdIDTO pdIDTO) {
        return ResponseEntity.ok(fachada.procesar(pdIDTO));
    }

}
