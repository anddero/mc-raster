package org.mcraster.util

abstract class CachedMap<K, V>(initialMaxCacheSizeMB: Int) {

    // Necessary overloaded methods

    abstract fun getCacheLineSizeMB(): Int

    abstract fun Map.Entry<K, V>.isOlderThan(otherCacheLine: Map.Entry<K, V>): Boolean

    abstract fun onDrop(entry: Map.Entry<K, V>)

    abstract fun load(key: K): V

    // Public non-overloadable contract

    var maxCacheSizeMB = initialMaxCacheSizeMB
        set(value) {
            field = value
            reduceCacheSizeIfNecessary(false)
        }

    operator fun get(key: K) = loadedCacheLines[key] ?: loadToCache(key)

    fun forEachCacheLine(action: (Map.Entry<K, V>) -> Unit) = loadedCacheLines.forEach(action)

    fun getCacheLineKeys() = loadedCacheLines.keys

    // Implementation details

    private val loadedCacheLines: MutableMap<K, V> = mutableMapOf()

    private val cacheLineAgeComparator: Comparator<Map.Entry<K, V>> =
        Comparator { o1, o2 -> if (o1.isOlderThan(o2)) 1 else if (o2.isOlderThan(o1)) -1 else 0 }

    private fun loadToCache(key: K): V {
        reduceCacheSizeIfNecessary(true)
        val newCacheLine = load(key)
        loadedCacheLines[key] = newCacheLine
        println("Loaded new cache-line (size: ${loadedCacheLines.size})")
        return newCacheLine
    }

    private fun reduceCacheSizeIfNecessary(enableRoomForNewCacheLine: Boolean) {
        var maxCacheLineCount = maxCacheSizeMB / getCacheLineSizeMB()
        if (maxCacheLineCount <= 0) {
            System.err.println("Maximum cache size has been set too small ($maxCacheSizeMB MB), " +
                    "value will be ignored to store at least one cache line")
            maxCacheLineCount = 1
        }
        if (enableRoomForNewCacheLine) --maxCacheLineCount
        while (loadedCacheLines.size > maxCacheLineCount) removeOldestCacheLine()
    }

    private fun removeOldestCacheLine() {
        val entry = loadedCacheLines.maxOfWith(cacheLineAgeComparator) { it }
        loadedCacheLines.remove(entry.key)
        onDrop(entry)
        println("Reduced cache-lines to ${loadedCacheLines.size}")
    }

}
