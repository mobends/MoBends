package goblinbob.mobends.core.asset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.VanillaDraw;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The models of the downloaded assets (1.12's {@code AssetModels}): block models as Blockbench
 * writes them ({@code elements}, their faces' {@code uv}, {@code rotation} and {@code texture},
 * and the {@code display.head} transform they're worn with), each read from its file the first
 * time it's drawn. 1.12 baked them as blocks are baked, each face spanning the whole texture
 * (the part's texture, whatever the face names); they're read the same way here, into quads drawn
 * straight into an entity's buffer. A face whose texture the model doesn't define ({@code #missing})
 * isn't drawn, as in 1.12. Render thread only.
 */
public final class AssetModels {
    private static final Map<String, Optional<AssetModel>> MODELS = new HashMap<>();
    private static final float RESCALE_22_5 = 1.0F / (float) Math.cos(Math.PI / 8) - 1.0F;
    private static final float RESCALE_45 = 1.0F / (float) Math.cos(Math.PI / 4) - 1.0F;

    private AssetModels() {
    }

    /** The model of the asset at {@code path} ({@code models/x.json}), or null if it can't be read. */
    @Nullable
    public static AssetModel get(String path) {
        return MODELS.computeIfAbsent(path, AssetModels::load).orElse(null);
    }

    static void clearCache() {
        MODELS.clear();
    }

    private static Optional<AssetModel> load(String path) {
        try (Reader reader = Files.newBufferedReader(AssetsModule.fileOf(path), StandardCharsets.UTF_8)) {
            return Optional.of(read(JsonParser.parseReader(reader).getAsJsonObject()));
        } catch (IOException | RuntimeException e) {
            MoBends.LOGGER.warn("Couldn't read the asset model {}: {}", path, e.toString());
            return Optional.empty();
        }
    }

    /** A model: its quads, in blocks, and the transform it's worn with on the head. */
    public record AssetModel(List<Quad> quads, Matrix4f headTransform) {
        /**
         * Draws the model in {@code poseStack}'s frame, through its head transform, every vertex
         * coloured {@code color} (ARGB).
         */
        public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
            poseStack.pushPose();
            VanillaDraw.mulPose(poseStack, headTransform);
            PoseStack.Pose pose = poseStack.last();
            for (Quad quad : quads) {
                for (int i = 0; i < 4; i++) {
                    Vector3f position = quad.positions[i];
                    VanillaDraw.vertex(consumer, pose, position.x, position.y, position.z, color, quad.us[i], quad.vs[i], overlay, light,
                        quad.normal.x, quad.normal.y, quad.normal.z);
                }
            }
            poseStack.popPose();
        }
    }

    /** A face: its corners in blocks, their texture coordinates (0 to 1 across the texture), its normal. */
    public record Quad(Vector3f[] positions, float[] us, float[] vs, Vector3f normal) {
    }

    static AssetModel read(JsonObject json) {
        Map<String, String> textures = new HashMap<>();
        JsonObject texturesJson = json.has("textures") ? json.getAsJsonObject("textures") : new JsonObject();
        for (Map.Entry<String, JsonElement> entry : texturesJson.entrySet()) {
            textures.put(entry.getKey(), entry.getValue().getAsString());
        }
        List<Quad> quads = new ArrayList<>();
        if (json.has("elements")) {
            for (JsonElement element : json.getAsJsonArray("elements")) {
                readElement(element.getAsJsonObject(), textures, quads);
            }
        }
        if (quads.isEmpty()) {
            throw new JsonParseException("The model has no faces to draw");
        }
        Matrix4f head = new Matrix4f();
        JsonObject display = json.has("display") ? json.getAsJsonObject("display") : null;
        if (display != null && display.has("head")) {
            head = readTransform(display.getAsJsonObject("head"));
        }
        return new AssetModel(List.copyOf(quads), head);
    }

    /**
     * An item transform, as vanilla reads it: a translation in pixels (up to 80 either way), turns
     * about X, Y and Z in degrees, and a scale (up to 4); applied in that order, as 1.12 applied
     * the head transform.
     */
    private static Matrix4f readTransform(JsonObject json) {
        Vector3f translation = vector(json, "translation", 0.0F);
        translation.set(clamp(translation.x, 80.0F), clamp(translation.y, 80.0F), clamp(translation.z, 80.0F)).mul(1.0F / 16.0F);
        Vector3f rotation = vector(json, "rotation", 0.0F);
        Vector3f scale = vector(json, "scale", 1.0F);
        scale.set(clamp(scale.x, 4.0F), clamp(scale.y, 4.0F), clamp(scale.z, 4.0F));
        return new Matrix4f()
            .translate(translation)
            .rotateX((float) Math.toRadians(rotation.x))
            .rotateY((float) Math.toRadians(rotation.y))
            .rotateZ((float) Math.toRadians(rotation.z))
            .scale(scale);
    }

    private static void readElement(JsonObject element, Map<String, String> textures, List<Quad> quads) {
        Vector3f from = vector(element, "from", 0.0F).mul(1.0F / 16.0F);
        Vector3f to = vector(element, "to", 0.0F).mul(1.0F / 16.0F);
        Matrix4f rotation = elementRotation(element.has("rotation") ? element.getAsJsonObject("rotation") : null);
        JsonObject faces = element.getAsJsonObject("faces");
        if (faces == null) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
            Direction direction = Direction.byName(entry.getKey());
            JsonObject face = entry.getValue().getAsJsonObject();
            if (direction == null || resolveTexture(face.has("texture") ? face.get("texture").getAsString() : null, textures) == null) {
                continue;
            }
            quads.add(bakeFace(from, to, direction, face, rotation));
        }
    }

    /** What a face's {@code #name} names, through the model's textures; null if it names nothing. */
    @Nullable
    private static String resolveTexture(@Nullable String name, Map<String, String> textures) {
        for (int depth = 0; name != null && name.startsWith("#") && depth < 16; depth++) {
            name = textures.get(name.substring(1));
        }
        return name == null || name.startsWith("#") || name.isEmpty() ? null : name;
    }

    /** The element's rotation about its origin (vanilla's {@code FaceBakery.applyElementRotation}), as a matrix in blocks. */
    private static Matrix4f elementRotation(@Nullable JsonObject json) {
        Matrix4f matrix = new Matrix4f();
        if (json == null) {
            return matrix;
        }
        float angle = json.has("angle") ? json.get("angle").getAsFloat() : 0.0F;
        if (angle == 0.0F) {
            return matrix;
        }
        Vector3f origin = vector(json, "origin", 8.0F).mul(1.0F / 16.0F);
        String axis = json.has("axis") ? json.get("axis").getAsString() : "y";
        Vector3f axisVector;
        Vector3f rescale;
        switch (axis) {
            case "x" -> {
                axisVector = new Vector3f(1, 0, 0);
                rescale = new Vector3f(0, 1, 1);
            }
            case "z" -> {
                axisVector = new Vector3f(0, 0, 1);
                rescale = new Vector3f(1, 1, 0);
            }
            default -> {
                axisVector = new Vector3f(0, 1, 0);
                rescale = new Vector3f(1, 0, 1);
            }
        }
        if (json.has("rescale") && json.get("rescale").getAsBoolean()) {
            rescale.mul(Math.abs(angle) == 22.5F ? RESCALE_22_5 : RESCALE_45).add(1, 1, 1);
        } else {
            rescale.set(1, 1, 1);
        }
        return matrix.translate(origin)
            .scale(rescale)
            .rotate(new Quaternionf().rotationAxis((float) Math.toRadians(angle), axisVector))
            .translate(origin.negate(new Vector3f()));
    }

    /**
     * A face of the box from {@code from} to {@code to}, its corners in vanilla's order
     * ({@code FaceInfo}) and its texture coordinates as vanilla's {@code BlockFaceUV} turns them.
     */
    private static Quad bakeFace(Vector3f from, Vector3f to, Direction direction, JsonObject face, Matrix4f rotation) {
        Vector3f[] corners = corners(from, to, direction);
        float[] uv = face.has("uv") ? floats(face.getAsJsonArray("uv"), 4) : defaultUv(from, to, direction);
        int turns = face.has("rotation") ? Math.floorMod(face.get("rotation").getAsInt() / 90, 4) : 0;
        float[] us = new float[4];
        float[] vs = new float[4];
        for (int i = 0; i < 4; i++) {
            int shifted = (i + turns) % 4;
            us[i] = uv[shifted == 0 || shifted == 1 ? 0 : 2] / 16.0F;
            vs[i] = uv[shifted == 0 || shifted == 3 ? 1 : 3] / 16.0F;
            rotation.transformPosition(corners[i]);
        }
        Vector3f normal = new Vector3f(corners[2]).sub(corners[0]).cross(new Vector3f(corners[3]).sub(corners[1]));
        if (normal.lengthSquared() > 0.0F) {
            normal.normalize();
        } else {
            normal.set(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        }
        return new Quad(corners, us, vs, normal);
    }

    /** The face's corners, in vanilla's {@code FaceInfo} order (counter-clockwise from outside). */
    private static Vector3f[] corners(Vector3f a, Vector3f b, Direction direction) {
        return switch (direction) {
            case DOWN -> new Vector3f[] {v(a.x, a.y, b.z), v(a.x, a.y, a.z), v(b.x, a.y, a.z), v(b.x, a.y, b.z)};
            case UP -> new Vector3f[] {v(a.x, b.y, a.z), v(a.x, b.y, b.z), v(b.x, b.y, b.z), v(b.x, b.y, a.z)};
            case NORTH -> new Vector3f[] {v(b.x, b.y, a.z), v(b.x, a.y, a.z), v(a.x, a.y, a.z), v(a.x, b.y, a.z)};
            case SOUTH -> new Vector3f[] {v(a.x, b.y, b.z), v(a.x, a.y, b.z), v(b.x, a.y, b.z), v(b.x, b.y, b.z)};
            case WEST -> new Vector3f[] {v(a.x, b.y, a.z), v(a.x, a.y, a.z), v(a.x, a.y, b.z), v(a.x, b.y, b.z)};
            case EAST -> new Vector3f[] {v(b.x, b.y, b.z), v(b.x, a.y, b.z), v(b.x, a.y, a.z), v(b.x, b.y, a.z)};
        };
    }

    /** The texture coordinates vanilla gives a face without {@code uv}: its own extent, in pixels. */
    private static float[] defaultUv(Vector3f from, Vector3f to, Direction direction) {
        float x0 = from.x * 16, y0 = from.y * 16, z0 = from.z * 16, x1 = to.x * 16, y1 = to.y * 16, z1 = to.z * 16;
        return switch (direction) {
            case DOWN -> new float[] {x0, 16 - z1, x1, 16 - z0};
            case UP -> new float[] {x0, z0, x1, z1};
            case NORTH -> new float[] {16 - x1, 16 - y1, 16 - x0, 16 - y0};
            case SOUTH -> new float[] {x0, 16 - y1, x1, 16 - y0};
            case WEST -> new float[] {z0, 16 - y1, z1, 16 - y0};
            case EAST -> new float[] {16 - z1, 16 - y1, 16 - z0, 16 - y0};
        };
    }

    private static Vector3f v(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    private static Vector3f vector(JsonObject json, String key, float fallback) {
        if (!json.has(key)) {
            return new Vector3f(fallback);
        }
        float[] values = floats(json.getAsJsonArray(key), 3);
        return new Vector3f(values[0], values[1], values[2]);
    }

    private static float[] floats(JsonArray array, int count) {
        if (array.size() != count) {
            throw new JsonParseException("Expected " + count + " numbers, got " + array);
        }
        float[] values = new float[count];
        for (int i = 0; i < count; i++) {
            values[i] = array.get(i).getAsFloat();
        }
        return values;
    }

    private static float clamp(float value, float limit) {
        return Math.max(-limit, Math.min(limit, value));
    }
}
