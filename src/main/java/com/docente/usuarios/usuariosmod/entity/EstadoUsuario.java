package com.docente.usuarios.usuariosmod.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Estados del usuario. En JSON en minúsculas (active, pending, inactive);
 * en la base se guarda con el nombre del enum.
 */
public enum EstadoUsuario {

	ACTIVE, PENDING, INACTIVE;

	@JsonValue
	public String valor() {
		return name().toLowerCase();
	}

	@JsonCreator
	public static EstadoUsuario desde(String valor) {
		if (valor == null || valor.isBlank()) {
			return ACTIVE;
		}
		return valueOf(valor.trim().toUpperCase());
	}
}
