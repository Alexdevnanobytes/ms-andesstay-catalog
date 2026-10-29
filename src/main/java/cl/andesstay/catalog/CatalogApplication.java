// Punto de entrada del microservicio de catálogo (puerto 8082).
// Administra las unidades (habitaciones y cabañas), calcula disponibilidad y registra las asignaciones
// (qué unidad está ocupada por qué reserva). Datos en PostgreSQL: tablas AS_UNITS y AS_ALLOCATIONS.
package cl.andesstay.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication: configuración automática y escaneo de los componentes de este paquete.
@SpringBootApplication
public class CatalogApplication {
    public static void main(String[] args) { SpringApplication.run(CatalogApplication.class, args); }
}
