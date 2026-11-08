package goblinbob.mobends.core.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import goblinbob.mobends.MoBends;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds the files of one kind ({@code assets/<namespace>/bends/<folder>/**.json}: types,
 * extensions) in every pack the game reads resources from: the mods', the enabled resource packs
 * and the server's. Unlike 1.12's, this version's resource manager can list a folder, and a file
 * that several packs have is listed once per pack.
 * <p>
 * Order, lowest priority first (as the game layers them): the packs from the bottom up, then the
 * files of one pack by namespace and path. A type found later replaces one found earlier with the
 * same id.
 */
final class TypeFileDiscovery {
    record TypeFile(String source, JsonElement json) {
    }

    private record Found(int pack, ResourceLocation location, Resource resource) {
    }

    private TypeFileDiscovery() {
    }

    /** The files under {@code bends/<folder>/} of every pack, in the order described above; those that can't be read are said and left out. */
    static List<TypeFile> discover(String folder) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Map<String, Integer> packOrder = new HashMap<>();
        manager.listPacks().map(PackResources::packId).forEach(id -> packOrder.putIfAbsent(id, packOrder.size()));

        List<Found> found = new ArrayList<>();
        Map<ResourceLocation, List<Resource>> stacks = manager.listResourceStacks("bends/" + folder, location -> location.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, List<Resource>> stack : stacks.entrySet()) {
            for (Resource resource : stack.getValue()) {
                found.add(new Found(packOrder.getOrDefault(resource.sourcePackId(), Integer.MAX_VALUE), stack.getKey(), resource));
            }
        }
        found.sort(Comparator.comparingInt(Found::pack)
                .thenComparing(f -> f.location().getNamespace())
                .thenComparing(f -> f.location().getPath()));

        List<TypeFile> files = new ArrayList<>();
        for (Found file : found) {
            String source = file.resource().sourcePackId() + "/" + file.location();
            try (Reader reader = file.resource().openAsReader()) {
                files.add(new TypeFile(source, JsonParser.parseReader(reader)));
            } catch (Exception e) {
                MoBends.LOGGER.error("Mo' Bends: could not read {}: {}", source, e.getMessage());
            }
        }
        return files;
    }
}
