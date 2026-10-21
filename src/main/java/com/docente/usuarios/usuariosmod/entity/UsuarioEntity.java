package com.docente.usuarios.usuariosmod.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Usuario administrado desde el panel. La tabla {@code usuarios} se crea /
 * actualiza automáticamente por Hibernate (spring.jpa.hibernate.ddl-auto=update).
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(nullable = false, unique = true, length = 180)
	private String email;

	@Column(length = 40)
	private String telefono;

	@Column(length = 80)
	private String departamento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RolUsuario rol = RolUsuario.VIEWER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoUsuario estado = EstadoUsuario.ACTIVE;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@Column(name = "ultima_actividad")
	private LocalDateTime ultimaActividad;

	protected UsuarioEntity() {
		// requerido por JPA
	}

	public UsuarioEntity(String nombre, String email, String telefono, String departamento) {
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
		this.departamento = departamento;
	}

	@PrePersist
	void antesDeGuardar() {
		if (this.creadoEn == null) {
			this.creadoEn = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	public EstadoUsuario getEstado() {
		return estado;
	}

	public void setEstado(EstadoUsuario estado) {
		this.estado = estado;
	}

	public LocalDateTime getCreadoEn() {
		return creadoEn;
	}

	public LocalDateTime getUltimaActividad() {
		return ultimaActividad;
	}

	public void setUltimaActividad(LocalDateTime ultimaActividad) {
		this.ultimaActividad = ultimaActividad;
	}
}
