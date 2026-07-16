package co.edu.escuelaing.alphaeci.identity_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class IdentityServiceApplicationTests {

	/**
	 * Fija una base H2 en memoria y desactiva la infraestructura de mensajería para el smoke test
	 * de carga de contexto. Se usa {@link DynamicPropertySource} porque sus valores tienen mayor
	 * precedencia que las variables de entorno del SO; el runner de CI exporta
	 * {@code SPRING_DATASOURCE_URL} apuntando a un Postgres, y sin esto ganaría esa URL sobre la de
	 * H2 dejando un driver H2 con una URL postgresql (mismatch de driver). Mantiene el test
	 * independiente de Postgres/RabbitMQ, que no siempre están disponibles.
	 */
	@DynamicPropertySource
	static void testProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url",
				() -> "jdbc:h2:mem:identity_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
		registry.add("spring.datasource.username", () -> "sa");
		registry.add("spring.datasource.password", () -> "");
		registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
		// Sin broker en CI: no crear el RabbitAdmin que auto-declara exchanges al arrancar
		// (abriría una conexion a RabbitMQ y tumbaria la carga del contexto).
		registry.add("spring.rabbitmq.dynamic", () -> "false");
		registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
	}

	@Test
	void contextLoads() {
	}

}
