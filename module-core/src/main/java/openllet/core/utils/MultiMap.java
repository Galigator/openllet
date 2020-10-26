package openllet.core.utils;

import java.util.Map;
import java.util.Set;

/**
 * Common interface for map that do not have perfect hash.
 *
 * @param <K> key
 * @param <V> values inside the buckets
 * @since 2.6.0
 */
public interface MultiMap<K, V> extends Map<K, Set<V>>
{
	//
}
