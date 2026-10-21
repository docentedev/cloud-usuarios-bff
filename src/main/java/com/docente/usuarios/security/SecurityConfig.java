package com.docente.usuarios.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * La API es un Resource Server: cada request debe traer un JWT emitido por el
 * User Pool de Cognito. El token se valida contra el JWKS del pool (firma,
 * expiración, issuer y client_id), y los grupos del pool ("cognito:groups")
 * pasan a ser las authorities del usuario.
 */
@Configuration
public class SecurityConfig {

	/** Los access tokens de Cognito identifican el App Client en este claim. */
	private static final String CLIENT_ID_CLAIM = "client_id";

	/** Claim donde Cognito publica los grupos del usuario. */
	private static final String GROUPS_CLAIM = "cognito:groups";

	/** Grupo del pool que puede operar el CRUD de usuarios. */
	public static final String GRUPO_ADMINISTRADOR = "administrador";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/error").permitAll()
				.requestMatchers("/api/usuarios", "/api/usuarios/**").hasAuthority(GRUPO_ADMINISTRADOR)
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		return http.build();
	}

	/**
	 * Convierte el claim "cognito:groups" en authorities con el nombre exacto
	 * del grupo (sin prefijo ROLE_), para poder usar hasAuthority("administrador").
	 */
	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName(GROUPS_CLAIM);
		authorities.setAuthorityPrefix("");

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
			@Value("${cognito.client-id}") String clientId) {
		NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(issuerUri),
				new CognitoClientIdValidator(clientId)));
		return decoder;
	}

	/**
	 * Cognito no usa el mismo claim en todos sus tokens: los ID tokens traen
	 * {@code aud} con el App Client ID y los access tokens traen {@code client_id}.
	 */
	static final class CognitoClientIdValidator implements OAuth2TokenValidator<Jwt> {

		private final String clientId;

		CognitoClientIdValidator(String clientId) {
			this.clientId = clientId;
		}

		@Override
		public OAuth2TokenValidatorResult validate(Jwt token) {
			List<String> audience = token.getAudience();
			boolean validAudience = audience != null && audience.contains(this.clientId);
			boolean validClientId = this.clientId.equals(token.getClaimAsString(CLIENT_ID_CLAIM));
			if (validAudience || validClientId) {
				return OAuth2TokenValidatorResult.success();
			}
			return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token",
					"El token no fue emitido para el App Client " + this.clientId, null));
		}
	}
}
