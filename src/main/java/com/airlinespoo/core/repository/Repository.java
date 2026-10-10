package com.airlinespoo.core.repository;

import com.airlinespoo.core.model.Identifiable;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de almacenamiento que implementan todos los repositorios del sistema.
 *
 * <p>Sus métodos reflejan el CRUD de una base de datos y se llaman igual en todos los
 * módulos. Hoy se implementan en memoria; más adelante habrá implementaciones con
 * JDBC y JPA que cumplan este mismo contrato, sin que servicios ni menús cambien.</p>
 *
 * <table>
 *   <caption>Equivalencia con SQL</caption>
 *   <tr><th>Método</th><th>Equivale a</th></tr>
 *   <tr><td>{@link #create}</td><td>INSERT</td></tr>
 *   <tr><td>{@link #findById}</td><td>SELECT ... WHERE id = ?</td></tr>
 *   <tr><td>{@link #findAll}</td><td>SELECT ...</td></tr>
 *   <tr><td>{@link #update}</td><td>UPDATE</td></tr>
 *   <tr><td>{@link #deleteById}</td><td>DELETE ... WHERE id = ?</td></tr>
 *   <tr><td>{@link #existsById}</td><td>SELECT 1 ... WHERE id = ?</td></tr>
 *   <tr><td>{@link #count}</td><td>SELECT COUNT(*)</td></tr>
 * </table>
 *
 * <p><b>Importante:</b> el repositorio no contiene reglas de negocio ni lanza
 * {@code NotFoundException}; eso es responsabilidad del servicio.</p>
 *
 * @param <T>  tipo de la entidad que guarda
 * @param <ID> tipo del identificador de la entidad
 */
public interface Repository<T extends Identifiable<ID>, ID> {

    /**
     * Guarda una entidad nueva (INSERT).
     *
     * @param entity entidad a guardar
     * @return la misma entidad guardada
     * @throws com.airlinespoo.core.exception.DuplicateException si ya existe una con ese id
     */
    T create(T entity);

    /**
     * Busca por identificador (SELECT ... WHERE id = ?).
     *
     * @param id identificador buscado
     * @return la entidad, o {@link Optional#empty()} si no existe
     */
    Optional<T> findById(ID id);

    /**
     * Lista todas las entidades (SELECT).
     *
     * @return una <b>copia</b> de los datos; modificarla no altera el repositorio
     */
    List<T> findAll();

    /**
     * Reemplaza una entidad que ya existe (UPDATE).
     *
     * @param entity entidad con los datos nuevos; se localiza por su id
     * @return la misma entidad actualizada
     * @throws com.airlinespoo.core.exception.NotFoundException si no existe una con ese id
     */
    T update(T entity);

    /**
     * Elimina por identificador (DELETE ... WHERE id = ?).
     *
     * @param id identificador de la entidad a eliminar
     * @return {@code true} si eliminó algo; {@code false} si no existía
     */
    boolean deleteById(ID id);

    /**
     * Indica si existe una entidad con ese identificador.
     *
     * @param id identificador buscado
     * @return {@code true} si existe
     */
    boolean existsById(ID id);

    /**
     * Cuenta las entidades guardadas (SELECT COUNT(*)).
     *
     * @return la cantidad de entidades
     */
    long count();
}
