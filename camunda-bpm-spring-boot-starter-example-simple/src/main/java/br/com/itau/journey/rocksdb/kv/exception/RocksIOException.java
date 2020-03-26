package br.com.itau.journey.rocksdb.kv.exception;

/**
 * Base class for exceptions related to IO operations against Key-Value Store.
 */
public abstract class RocksIOException extends Exception {

    public RocksIOException(final String message) {
        super(message);
    }

    public RocksIOException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
