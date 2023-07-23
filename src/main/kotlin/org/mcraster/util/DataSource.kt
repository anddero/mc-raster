package org.mcraster.util

import java.io.Closeable

sealed interface DataSource<T> {

    fun <R> use(block: (Sequence<T>) -> R): R

    fun forEach(action: (T) -> Unit) = use { it.forEach(action) }

    fun <U> transform(transformer: (Sequence<T>) -> Sequence<U>): DataSource<U> =
        TransformedDataSource(this, transformer)

    fun <U> map(mapper: (T) -> U): DataSource<U> = transform { it.map(mapper) }

    fun onEach(action: (T) -> Unit): DataSource<T> = transform { it.onEach(action) }

    fun filter(predicate: (T) -> Boolean): DataSource<T> = transform { it.filter(predicate) }

    fun first(): T = use { it.first() }

    fun firstTwo(): Pair<T, T> = use { val iterator = it.iterator(); Pair(iterator.next(), iterator.next()) }

    companion object {
        private fun <T> dataSource(sourceLoader: () -> (Sequence<T>)): DataSource<T> = SimpleDataSource(sourceLoader)
        fun <T> emptyDataSource(): DataSource<T> = dataSource { emptySequence() }
        fun <T> Sequence<T>.asDataSource(): DataSource<T> = dataSource { this }
        fun <C: Closeable, T> C.asDataSource(toSequence: (C) -> Sequence<T>): DataSource<T> = CloseableSourceDataSource(this, toSequence)
    }

    private class SimpleDataSource<T>(private val dataLoader: () -> (Sequence<T>)) : DataSource<T> {
        override fun <R> use(block: (Sequence<T>) -> R) = block(dataLoader.invoke())
    }

    private class TransformedDataSource<T, U>(
        private val dataSource: DataSource<T>,
        private val sequenceTransformer: (Sequence<T>) -> Sequence<U>
    ) : DataSource<U> {
        override fun <R> use(block: (Sequence<U>) -> R) = dataSource.use { block(sequenceTransformer(it)) }
    }

    private class CloseableSourceDataSource<C: Closeable, T>(
        private val source: C,
        private val sourceToSequence: (C) -> Sequence<T>
    ) : DataSource<T> {
        override fun <R> use(block: (Sequence<T>) -> R) = source.use { block(sourceToSequence(it)) }
    }

}
