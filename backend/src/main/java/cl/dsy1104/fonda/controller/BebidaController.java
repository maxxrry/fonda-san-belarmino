package cl.dsy1104.fonda.controller;

import cl.dsy1104.fonda.dto.BebidaRequest;
import cl.dsy1104.fonda.dto.BebidaResponse;
import cl.dsy1104.fonda.service.BebidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST del catalogo. Solo HTTP: recibe, delega en BebidaService y elige
 * el codigo de estado. Los errores (400, 404, 409) los arma ManejadorGlobalErrores.
 */
@RestController
@RequestMapping("/api/bebidas")
public class BebidaController {

    private final BebidaService bebidaService;

    public BebidaController(BebidaService bebidaService) {
        this.bebidaService = bebidaService;
    }

    @GetMapping
    public List<BebidaResponse> listar(@RequestParam(required = false) String nombre) {
        return bebidaService.listar(nombre);
    }

    @GetMapping("/{id}")
    public BebidaResponse buscarPorId(@PathVariable Long id) {
        return bebidaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<BebidaResponse> crear(@Valid @RequestBody BebidaRequest request) {
        BebidaResponse creada = bebidaService.crear(request);
        // Location: URL de esta misma peticion + /{id} -> .../api/bebidas/5
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    public BebidaResponse actualizar(@PathVariable Long id, @Valid @RequestBody BebidaRequest request) {
        return bebidaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        bebidaService.eliminar(id);
    }

    @PatchMapping("/{id}/restriccion")
    public BebidaResponse restringir(@PathVariable Long id) {
        return bebidaService.restringir(id);
    }
}
