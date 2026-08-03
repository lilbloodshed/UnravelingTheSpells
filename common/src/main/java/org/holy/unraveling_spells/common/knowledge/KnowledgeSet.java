package org.holy.unraveling_spells.common.knowledge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Loader-independent storage used by the platform-specific player data implementation.
 */
public final class KnowledgeSet<T> {
    private final Set<T> values = new HashSet<>();

    public Set<T> values() {
        return values;
    }

    public boolean contains(T value) {
        return values.contains(value);
    }

    public void add(T value) {
        values.add(value);
    }

    public void remove(T value) {
        values.remove(value);
    }

    public void replaceWith(Collection<? extends T> source) {
        values.clear();
        values.addAll(source);
    }
}
