package org.holy.unraveling_spells.common.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Parses loader-independent entries in the form {@code identifier=amount}.
 */
public final class CostOverrideParser {
    private CostOverrideParser() {
    }

    public static <T> Map<T, Integer> parse(
            List<? extends String> entries,
            Function<String, T> identifierParser) {
        Map<T, Integer> costs = new LinkedHashMap<>();

        for (String entry : entries) {
            int separator = entry.lastIndexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                continue;
            }

            T identifier = identifierParser.apply(entry.substring(0, separator).trim());
            if (identifier == null) {
                continue;
            }

            try {
                costs.put(identifier,
                        Integer.parseInt(entry.substring(separator + 1).trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        return costs;
    }
}
