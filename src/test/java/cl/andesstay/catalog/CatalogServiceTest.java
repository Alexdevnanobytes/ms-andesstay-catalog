package cl.andesstay.catalog;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "internal.token=test-secret"
})
class CatalogServiceTest {
    @Autowired CatalogService service;
    @Test void overlappingDatesAreRejectedAndReleasedDatesAvailable() {
        Unit u = service.create(new CatalogController.UnitRequest("C-101", UnitType.CABANA, "Cabaña test", new BigDecimal("30000")));
        LocalDate start = LocalDate.now().plusDays(5);
        service.allocate(new CatalogController.AllocationRequest("r-1", u.getId(), start, start.plusDays(3)));
        assertFalse(service.available(u.getId(), start.plusDays(2), start.plusDays(4)));
        assertThrows(ResponseStatusException.class, () -> service.allocate(new CatalogController.AllocationRequest("r-2", u.getId(), start.plusDays(2), start.plusDays(4))));
        assertTrue(service.available(u.getId(), start.plusDays(3), start.plusDays(5)));
        service.release("r-1");
        assertTrue(service.available(u.getId(), start, start.plusDays(3)));
    }
}
