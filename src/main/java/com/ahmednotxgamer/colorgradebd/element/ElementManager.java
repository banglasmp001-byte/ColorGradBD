package com.ahmednotxgamer.colorgradebd.element;

import com.ahmednotxgamer.colorgradebd.ColorGradeBD;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dynamically queries Minecraft's block registry to provide the element list.
 *
 * This is intentionally version-agnostic: it uses the current runtime's
 * Registries.BLOCK to discover all registered blocks, including modded ones.
 * No hardcoded block list is used.
 */
public class ElementManager {

    private static final ElementManager INSTANCE = new ElementManager();

    /**
     * Cached list of all selectable elements, built once from registries.
     * Rebuilt if explicitly invalidated.
     */
    private List<ElementEntry> allElements = null;

    /** Special environment/render categories not tied to block registry. */
    private static final List<ElementEntry> SPECIAL_ELEMENTS = List.of(
            new ElementEntry("colorgradebd:sky",    "Sky",    ElementEntry.Category.ENVIRONMENT),
            new ElementEntry("colorgradebd:clouds", "Clouds", ElementEntry.Category.ENVIRONMENT),
            new ElementEntry("colorgradebd:fog",    "Fog",    ElementEntry.Category.ENVIRONMENT),
            new ElementEntry("colorgradebd:sun",    "Sun",    ElementEntry.Category.ENVIRONMENT),
            new ElementEntry("colorgradebd:moon",   "Moon",   ElementEntry.Category.ENVIRONMENT),
            new ElementEntry("colorgradebd:stars",  "Stars",  ElementEntry.Category.ENVIRONMENT)
    );

    private ElementManager() {}

    public static ElementManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a combined list of special environment elements + all registered
     * Minecraft blocks, discovered at runtime. Result is cached after first call.
     */
    public List<ElementEntry> getAllElements() {
        if (allElements == null) {
            allElements = buildElementList();
        }
        return allElements;
    }

    /** Force rebuild of the cached element list (call after mod loading is complete). */
    public void invalidateCache() {
        allElements = null;
    }

    /**
     * Search elements by name query (case-insensitive, matches display name or registry key).
     */
    public List<ElementEntry> search(String query) {
        if (query == null || query.isBlank()) {
            return getAllElements();
        }
        String lower = query.toLowerCase(Locale.ROOT).trim();
        return getAllElements().stream()
                .filter(e -> e.getDisplayName().toLowerCase(Locale.ROOT).contains(lower)
                        || e.getRegistryKey().toLowerCase(Locale.ROOT).contains(lower))
                .collect(Collectors.toList());
    }

    private List<ElementEntry> buildElementList() {
        List<ElementEntry> list = new ArrayList<>();

        // Add special render categories first
        list.addAll(SPECIAL_ELEMENTS);

        // Dynamically iterate the current runtime's block registry
        try {
            for (Map.Entry<net.minecraft.registry.RegistryKey<Block>, Block> entry :
                    Registries.BLOCK.getEntrySet()) {
                Identifier id = entry.getKey().getValue();
                Block block = entry.getValue();
                // Skip the "air" block — it has no visual presence
                if (id.getPath().equals("air")) continue;

                String key = id.toString();
                String displayName = formatDisplayName(id.getPath());
                String namespace = id.getNamespace();
                ElementEntry.Category category = namespace.equals("minecraft")
                        ? ElementEntry.Category.VANILLA_BLOCK
                        : ElementEntry.Category.MODDED_BLOCK;

                list.add(new ElementEntry(key, displayName, category));
            }
        } catch (Exception e) {
            ColorGradeBD.LOGGER.error("[ColorGrade BD] Failed to enumerate block registry: {}", e.getMessage());
        }

        // Sort: specials first, then vanilla blocks alphabetically, then modded blocks
        list.sort(Comparator
                .comparingInt((ElementEntry e) -> e.getCategory().ordinal())
                .thenComparing(ElementEntry::getDisplayName, String.CASE_INSENSITIVE_ORDER));

        ColorGradeBD.LOGGER.info("[ColorGrade BD] Element list built: {} entries", list.size());
        return list;
    }

    /** Convert snake_case registry path to "Title Case With Spaces". */
    private String formatDisplayName(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}
