package com.docente.usuarios.usuariosmod;

import java.util.List;

import com.docente.usuarios.usuariosmod.dto.Usuario;

public interface UsuariosService {

	List<Usuario> listar();

	Usuario obtener(Long id);

	Usuario crear(Usuario request);

	Usuario actualizar(Long id, Usuario request);

	void eliminar(Long id);
}
