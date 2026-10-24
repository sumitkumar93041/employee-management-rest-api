package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JWTServiceTest {

	private JWTService jwtService;

	@BeforeEach
	void setup() {
		jwtService = new JWTService();
	}

	private UserDetails userDetails(String username) {
		return User.withUsername(username).password("hash").authorities("USER").build();
	}

	@Test
	void shouldGenerateTokenWithThreeParts() {
		String token = jwtService.generateToken("sumit");

		assertEquals(3, token.split("\\.").length);
	}

	@Test
	void shouldExtractSameUsernameFromToken() {
		String token = jwtService.generateToken("sumit");

		String username = jwtService.extractUserName(token);

		assertEquals("sumit", username);
	}

	@Test
	void shouldValidateTokenForCorrectUser() {
		String token = jwtService.generateToken("sumit");

		assertTrue(jwtService.validateToken(token, userDetails("sumit")));
	}

	@Test
	void shouldRejectTokenForDifferentUser() {
		String token = jwtService.generateToken("sumit");

		assertFalse(jwtService.validateToken(token, userDetails("someone-else")));
	}

	@Test
	void shouldRejectTokenSignedWithAnotherKey() {
		String foreignToken = new JWTService().generateToken("sumit");

		assertThrows(JwtException.class, () -> jwtService.extractUserName(foreignToken));
	}

	@Test
	void shouldRejectGarbageToken() {
		assertThrows(JwtException.class, () -> jwtService.extractUserName("abc"));
	}

	@Test
	void shouldRejectExpiredToken() {
		String secret = (String) ReflectionTestUtils.getField(jwtService, "secretKey");
		SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

		String expiredToken = Jwts.builder()
				.subject("sumit")
				.issuedAt(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000L))
				.expiration(new Date(System.currentTimeMillis() - 60 * 60 * 1000L))
				.signWith(key)
				.compact();

		assertThrows(JwtException.class, () -> jwtService.extractUserName(expiredToken));
	}
}