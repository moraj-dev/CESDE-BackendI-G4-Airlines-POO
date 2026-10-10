package com.airlinespoo.core.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.model.Identifiable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    /** Entidad mínima solo para probar el repositorio base. */
    record Thing(Integer id, String name) implements Identifiable<Integer> {
        @Override
        public Integer getId() {
            return id;
        }
    }

    /** Repositorio concreto mínimo: extiende la base e implementa el único método abstracto. */
    static class ThingRepository extends InMemoryRepository<Thing, Integer> {
        @Override
        protected String entityName() {
            return "cosa";
        }

        /** Ejemplo de búsqueda complementaria apoyada en values(). */
        List<Thing> findByName(String name) {
            return values().stream().filter(t -> t.name().equals(name)).toList();
        }
    }

    private ThingRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ThingRepository();
    }

    @Test
    void createStoresAndFindByIdReturnsIt() {
        Thing thing = new Thing(1, "uno");
        assertSame(thing, repository.create(thing));
        assertEquals(Optional.of(thing), repository.findById(1));
        assertTrue(repository.existsById(1));
        assertEquals(1, repository.count());
    }

    @Test
    void createWithRepeatedIdThrowsDuplicate() {
        repository.create(new Thing(1, "uno"));
        assertThrows(DuplicateException.class, () -> repository.create(new Thing(1, "otro")));
        assertEquals(1, repository.count());
    }

    @Test
    void findByIdOfMissingReturnsEmptyAndDoesNotThrow() {
        assertEquals(Optional.empty(), repository.findById(99));
        assertFalse(repository.existsById(99));
    }

    @Test
    void updateReplacesExistingEntity() {
        repository.create(new Thing(1, "uno"));
        repository.update(new Thing(1, "UNO"));
        assertEquals("UNO", repository.findById(1).orElseThrow().name());
        assertEquals(1, repository.count());
    }

    @Test
    void updateOfMissingThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> repository.update(new Thing(7, "siete")));
    }

    @Test
    void deleteByIdReportsWhetherItRemovedSomething() {
        repository.create(new Thing(1, "uno"));
        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertEquals(0, repository.count());
    }

    @Test
    void findAllReturnsACopyAndKeepsInsertionOrder() {
        repository.create(new Thing(2, "dos"));
        repository.create(new Thing(1, "uno"));
        List<Thing> all = repository.findAll();
        assertEquals(List.of(new Thing(2, "dos"), new Thing(1, "uno")), all);

        all.clear();
        assertEquals(2, repository.count());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void subclassesCanBuildComplementarySearchesOnValues() {
        repository.create(new Thing(1, "a"));
        repository.create(new Thing(2, "b"));
        repository.create(new Thing(3, "a"));
        assertEquals(2, repository.findByName("a").size());
    }

    @Test
    void nullEntityIsRejectedWithTheSystemException() {
        assertThrows(AirlinesException.class, () -> repository.create(null));
        assertThrows(AirlinesException.class, () -> repository.create(new Thing(null, "sin id")));
    }

    @Test
    void errorMessagesAreInSpanishAndMentionTheEntity() {
        repository.create(new Thing(1, "uno"));
        DuplicateException duplicate = assertThrows(DuplicateException.class,
                () -> repository.create(new Thing(1, "otro")));
        assertTrue(duplicate.getMessage().contains("cosa"));
        NotFoundException notFound = assertThrows(NotFoundException.class,
                () -> repository.update(new Thing(5, "x")));
        assertTrue(notFound.getMessage().contains("cosa"));
    }
}
