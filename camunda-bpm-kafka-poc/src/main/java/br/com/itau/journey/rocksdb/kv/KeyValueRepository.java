package br.com.itau.journey.rocksdb.kv;

import br.com.itau.journey.rocksdb.kv.exception.DeleteAllFailedException;
import br.com.itau.journey.rocksdb.kv.exception.DeleteFailedException;
import br.com.itau.journey.rocksdb.kv.exception.FindFailedException;
import br.com.itau.journey.rocksdb.kv.exception.SaveFailedException;
import br.com.itau.journey.rocksdb.mapper.exception.DeserializationException;
import br.com.itau.journey.rocksdb.mapper.exception.SerDeException;
import br.com.itau.journey.rocksdb.mapper.exception.SerializationException;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 *  Interface that defines operations against Key-Value Store.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface KeyValueRepository<K, V> {

    /**
     * Inserts key-value pair into RocksDB.
     *
     * @param key of value.
     * @param value that should be persisted.
     * @throws SerializationException when it's not possible to serialize entity.
     * @throws SaveFailedException when it's not possible to persist entity.
     */
    void save(K key, V value) throws IOException, SaveFailedException, SaveFailedException;

    /**
     * Try to find value for a given key.
     *
     * @param key of entity that should be retrieved.
     * @return Optional of entity.
     * @throws SerDeException when it's not possible to serialize/deserialize entity.
     * @throws FindFailedException when it's not possible to retrieve a wanted entity.
     */
    Optional<V> findByKey(K key) throws SerDeException, FindFailedException, SerDeException;

    /**
     * Try to find all entities from repository.
     *
     * @return Collection of entities.
     * @throws DeserializationException when it's not possible to deserialize entity.
     */
    Collection<V> findAll() throws DeserializationException;

    /**
     * Delete entity for a given key.
     *
     * @param key of entity that should be deleted.
     * @throws SerializationException when it's not possible to serialize entity.
     * @throws DeleteFailedException when it's not possible to delete a wanted entity.
     */
    void deleteByKey(K key) throws SerializationException, DeleteFailedException;

    /**
     * Deletes all entities from RocksDB.
     *
     * @throws DeleteAllFailedException when it's not possible to delete all entities.
     */
    void deleteAll() throws DeleteAllFailedException;
}
