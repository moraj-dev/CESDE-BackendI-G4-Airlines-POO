package com.airlinespoo.core.repository;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación base de {@link Repository} que guarda los datos en un mapa en memoria.
 *
 * <p>Es una clase <b>abstracta</b>: nadie debe crear un repositorio "genérico". Cada módulo
 * la extiende para su entidad (por ejemplo, {@code CustomerRepository}), define el nombre de
 * la entidad con {@link #entityName()} y agrega sus búsquedas propias apoyándose en
 * {@link #values()}.</p>
 *
 * <p>Usa un {@link LinkedHashMap}: busca por id sin recorrer todo y conserva el orden de
 * inserción.</p>
 *
 * @param <T>  tipo de la entidad que guarda
 * @param <ID> tipo del identificador de la entidad
 */
public abstract class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    private final Map<ID, T> store = new LinkedHashMap<>();

    /**
     * Nombre de la entidad en español y en minúscula, usado en los mensajes de error
     * (por ejemplo, "cliente", "vuelo").
     *
     * @return el nombre de la entidad
     */
    protected abstract String entityName();

    @Override
    public T create(T entity) {
        Validator.requireNotNull(entity, entityName());
        ID id = Validator.requireNotNull(entity.getId(), "id de " + entityName());
        if (store.containsKey(id)) {
            throw new DuplicateException(entityName(), id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public T update(T entity) {
        Validator.requireNotNull(entity, entityName());
        ID id = Validator.requireNotNull(entity.getId(), "id de " + entityName());
        if (!store.containsKey(id)) {
            throw new NotFoundException(entityName(), id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return store.remove(id) != null;
    }

    @Override
    public boolean existsById(ID id) {
        return store.containsKey(id);
    }

    @Override
    public long count() {
        return store.size();
    }

    /**
     * Entrega una <b>copia</b> de todas las entidades para que las clases hijas
     * implementen sus búsquedas complementarias ({@code findBy...}, {@code existsBy...},
     * {@code countBy...}) sin exponer el mapa interno.
     *
     * @return copia de las entidades guardadas, en orden de inserción
     */
    protected List<T> values() {
        return new ArrayList<>(store.values());
    }
}
