package com.docente.usuarios.usuariosmod.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Roles del panel. En JSON se serializan en minúsculas (admin, editor, viewer)
 * para que calcen con el front; en la base se guardan con el nombre del enum.
 */
public enum RolUsuario {

	ADMIN, EDITOR, VIEWER;

	@JsonValue
	public String valor() {
		return name().toLowerCase();
	}

	@JsonCreator
	public static RolUsuario desde(String valor) {
		if (valor == null || valor.isBlank()) {
			return VIEWER;
		}
		return valueOf(valor.trim().toUpperCase());
	}
}
