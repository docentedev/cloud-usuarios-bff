package com.docente.usuarios.usuariosmod;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.docente.usuarios.usuariosmod.dto.Usuario;

/**
 * CRUD de la tabla {@code usuarios}. Todo el controller queda protegido por
 * {@code SecurityConfig}: exige JWT de Cognito y pertenencia al grupo
 * "administrador".
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

	private final UsuariosService usuarios;

	public UsuariosController(UsuariosService usuarios) {
		this.usuarios = usuarios;
	}

	@GetMapping
	public List<Usuario> listar() {
		return this.usuarios.listar();
	}

	@GetMapping("/{id}")
	public Usuario obtener(@PathVariable Long id) {
		return this.usuarios.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Usuario crear(@RequestBody Usuario request) {
		return this.usuarios.crear(request);
	}

	@PutMapping("/{id}")
	public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario request) {
		return this.usuarios.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id) {
		this.usuarios.eliminar(id);
	}
}
