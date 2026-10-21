package com.docente.usuarios.usuariosmod;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docente.usuarios.usuariosmod.entity.UsuarioEntity;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
}
