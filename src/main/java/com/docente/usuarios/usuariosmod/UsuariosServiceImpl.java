package com.docente.usuarios.usuariosmod;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.docente.usuarios.usuariosmod.dto.Usuario;
import com.docente.usuarios.usuariosmod.entity.UsuarioEntity;

@Service
@Transactional
public class UsuariosServiceImpl implements UsuariosService {

	private final UsuarioRepository usuarios;

	public UsuariosServiceImpl(UsuarioRepository usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> listar() {
		return this.usuarios.findAll(Sort.by("id")).stream().map(Usuario::desde).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario obtener(Long id) {
		return Usuario.desde(buscar(id));
	}

	@Override
	public Usuario crear(Usuario request) {
		String email = normalizarEmail(request.email());
		if (this.usuarios.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con el email " + email);
		}
		UsuarioEntity entity = new UsuarioEntity(requerido(request.nombre(), "nombre"), email, request.telefono(),
				request.departamento());
		if (request.rol() != null) {
			entity.setRol(request.rol());
		}
		if (request.estado() != null) {
			entity.setEstado(request.estado());
		}
		return Usuario.desde(this.usuarios.save(entity));
	}

	@Override
	public Usuario actualizar(Long id, Usuario request) {
		UsuarioEntity entity = buscar(id);
		String email = normalizarEmail(request.email());
		this.usuarios.findByEmailIgnoreCase(email)
			.filter(otro -> !otro.getId().equals(id))
			.ifPresent(otro -> {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con el email " + email);
			});
		entity.setNombre(requerido(request.nombre(), "nombre"));
		entity.setEmail(email);
		entity.setTelefono(request.telefono());
		entity.setDepartamento(request.departamento());
		if (request.rol() != null) {
			entity.setRol(request.rol());
		}
		if (request.estado() != null) {
			entity.setEstado(request.estado());
		}
		return Usuario.desde(this.usuarios.save(entity));
	}

	@Override
	public void eliminar(Long id) {
		this.usuarios.delete(buscar(id));
	}

	private UsuarioEntity buscar(Long id) {
		return this.usuarios.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario con id " + id));
	}

	private static String normalizarEmail(String email) {
		return requerido(email, "email").toLowerCase();
	}

	private static String requerido(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo '" + campo + "' es obligatorio");
		}
		return valor.trim();
	}
}
