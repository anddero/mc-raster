package org.mcraster.util

import java.io.Closeable

/**
 * A convenient wrapper to load and process large sequences of data without the need to define
 * custom classes for each type of data. This utility focuses on a design to do all operations
 * lazily, only at the moment the data actually needs to be used, and uses lambdas extensively.
 *
 * All terminal operations invoke the loading and reading of the data. Once a terminal operation
 * completes, the source is closed, invalidating all further operations on it.
 *
 * Implementations should not open/load any sources eagerly on construction, or on lazy operation
 * calls. Implementation should not load all data to memory at once, when a terminal operation is
 * called. Instead, data should be loaded as lazily as possible (one by one, or in chunks),
 * balancing between performance and memory use.
 */
sealed class LazyData<T> { // TODO Can prolly get rid of it in favor of raw Sequence

    /**
     * Terminal operation. Invoke the given block to consume the sequence of data.
     */
    abstract fun <R> use(consume: (Sequence<T>) -> R): R

    /**
     * Terminal operation. Read only the first two items and return them.
     */
    fun firstTwo(): Pair<T, T> = use { it.take(2).toList().zipWithNext().single() }

    /**
     * Lazy operation. Use it to carry out any transformations of the underlying sequence.
     */
    fun <U> transform(transform: (Sequence<T>) -> Sequence<U>): LazyData<U> =
        TransformedLazyData(
            lazyData = this,
            transform = transform
        )

    companion object {
        /**
         * Define a data source by providing the means of initializing or opening the source.
         */
        fun <T> lazyData(load: () -> (Sequence<T>)): LazyData<T> = SimpleLazyData(load = load)
        fun <T> emptyLazyData(): LazyData<T> = SimpleLazyData { emptySequence() }
        fun <C: Closeable> closeableLazyData(load: () -> C): CloseableLazyDataLoader<C> =
            CloseableLazyDataLoader(open = load) { false }
    }

    class CloseableLazyDataLoader<C: Closeable>(
        private val open: () -> C,
        private val ignoreCloseFailure: (Throwable) -> Boolean
    ) {
        /**
         * If something is thrown when the closeable is closed, catch it and determine
         * whether it can be ignored or it should be rethrown.
         * Callback should return True if the failure can be ignored.
         */
        fun ignoreCloseFailure(ignoreCloseFailure: (Throwable) -> Boolean): CloseableLazyDataLoader<C> {
            return CloseableLazyDataLoader(open = open, ignoreCloseFailure = ignoreCloseFailure)
        }
        /**
         * Specify the logic on how to open the closeable source.
         */
        fun <R> transform(toSequence: (C) -> Sequence<R>): LazyData<R> =
            CloseableLazyData(open = open, toSequence = toSequence, ignoreCloseFailure = ignoreCloseFailure)
    }

    private class CloseableLazyData<C: Closeable, T>(
        private val open: () -> C,
        private val toSequence: (C) -> Sequence<T>,
        private val ignoreCloseFailure: (Throwable) -> Boolean
    ) : LazyData<T>() {
        override fun <R> use(consume: (Sequence<T>) -> R): R {
            val openedSource = open()
            var result: Result<R>? = null
            try {
                openedSource.use { src ->
                    result = Result.success(consume(toSequence(src)))
                }
            } catch (t: Throwable) {
                if (result == null) throw t
                if (!ignoreCloseFailure(t)) throw t
            }
            return result!!.getOrThrow()
        }
    }

    private class SimpleLazyData<T>(private val load: () -> (Sequence<T>)) : LazyData<T>() {
        override fun <R> use(consume: (Sequence<T>) -> R): R = consume(load())
    }

    private class TransformedLazyData<T, U>(
        private val lazyData: LazyData<T>,
        private val transform: (Sequence<T>) -> Sequence<U>
    ) : LazyData<U>() {
        override fun <R> use(consume: (Sequence<U>) -> R) = lazyData.use { seq -> consume(transform(seq)) }
    }

}
