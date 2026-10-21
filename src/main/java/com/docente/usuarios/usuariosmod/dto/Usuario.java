package com.docente.usuarios.usuariosmod.dto;

import java.time.LocalDateTime;

import com.docente.usuarios.usuariosmod.entity.EstadoUsuario;
import com.docente.usuarios.usuariosmod.entity.RolUsuario;
import com.docente.usuarios.usuariosmod.entity.UsuarioEntity;

/**
 * Usuario tal como entra y sale de la API. En las peticiones se ignoran
 * {@code id}, {@code creadoEn} y {@code ultimaActividad}.
 */
public record Usuario(
		Long id,
		String nombre,
		String email,
		String telefono,
		String departamento,
		RolUsuario rol,
		EstadoUsuario estado,
		LocalDateTime creadoEn,
		LocalDateTime ultimaActividad) {

	public static Usuario desde(UsuarioEntity entity) {
		return new Usuario(entity.getId(), entity.getNombre(), entity.getEmail(), entity.getTelefono(),
				entity.getDepartamento(), entity.getRol(), entity.getEstado(), entity.getCreadoEn(),
				entity.getUltimaActividad());
	}
}
