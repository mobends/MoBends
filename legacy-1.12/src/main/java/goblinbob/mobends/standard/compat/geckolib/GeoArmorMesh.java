package goblinbob.mobends.standard.compat.geckolib;

import goblinbob.mobends.standard.client.model.armor.ArmorMesh;
import goblinbob.mobends.standard.client.model.armor.ArmorWrapper;
import goblinbob.mobends.standard.client.model.armor.IPartWrapper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.HashMap;
import java.util.Map;

/**
 * A GeckoLib armour model's faces, in the parts of an armour wrapper: each armour bone's (and its
 * children's) in the part GeckoLib copies the vanilla rotation of to it, a limb's cut at the
 * wrapper's joint, and the bones outside them in the body. Built anew every frame, as the bones
 * below the armour bones move with GeckoLib's own animation.
 * <p>
 * GeckoLib poses an armour bone with the vanilla part's rotation alone, about its own pivot; here
 * the part's animation poses it instead, the bone's own rotation left out as GeckoLib leaves it out.
 * A face goes where it is on a vanilla model at rest: GeckoLib's units are blocks, with y up from
 * the feet, and a vanilla part's are pixels, from its rotation point and with y down.
 */
class GeoArmorMesh
{
    /** Where GeckoLib's armour renderer draws its y = 0, in vanilla model space (pixels from the top of the model). */
    static final float GEO_ORIGIN_Y = (1.501F - 0.01F) * 16;

    /** The faces in one part of the wrapper, in its vanilla coordinates. */
    static final class Piece
    {
        final IPartWrapper part;
        /** Where the vanilla part's rotation point is at rest. */
        final float pivotX, pivotY, pivotZ;
        final ArmorMesh upper = new ArmorMesh();
        final ArmorMesh lower = new ArmorMesh();

        Piece(IPartWrapper part, float pivotX, float pivotY, float pivotZ)
        {
            this.part = part;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
        }
    }

    final Piece head, body, rightArm, leftArm, rightLeg, leftLeg;
    private final Piece[] pieces;
    private final Map<String, Piece> pieceByBone = new HashMap<>();

    private final MatrixStack stack = new MatrixStack();
    private final float[] polygon = new float[4 * ArmorMesh.STRIDE];
    private final float[] scratch = ArmorMesh.scratchFor(4);
    private final Vector4f position = new Vector4f();
    private final Vector3f normal = new Vector3f();

    GeoArmorMesh(ArmorWrapper wrapper)
    {
        // ModelBiped's rotation points.
        head = new Piece(wrapper.getHeadParts(), 0, 0, 0);
        body = new Piece(wrapper.getBodyParts(), 0, 0, 0);
        rightArm = new Piece(wrapper.getRightArmParts(), -5, 2, 0);
        leftArm = new Piece(wrapper.getLeftArmParts(), 5, 2, 0);
        rightLeg = new Piece(wrapper.getRightLegParts(), -1.9F, 12, 0);
        leftLeg = new Piece(wrapper.getLeftLegParts(), 1.9F, 12, 0);
        pieces = new Piece[] { head, body, rightArm, leftArm, rightLeg, leftLeg };
    }

    /** Puts the faces {@code model} draws now (its bones as GeckoLib's animation and the renderer's slot left them) in the pieces. */
    void build(GeoModel model, GeoArmorRenderer<?> renderer)
    {
        pieceByBone.clear();
        pieceByBone.put(renderer.headBone, head);
        pieceByBone.put(renderer.bodyBone, body);
        pieceByBone.put(renderer.rightArmBone, rightArm);
        pieceByBone.put(renderer.leftArmBone, leftArm);
        pieceByBone.put(renderer.rightLegBone, rightLeg);
        pieceByBone.put(renderer.leftLegBone, leftLeg);
        pieceByBone.put(renderer.rightBootBone, rightLeg);
        pieceByBone.put(renderer.leftBootBone, leftLeg);

        for (Piece piece : pieces)
        {
            piece.upper.clear();
            piece.lower.clear();
        }

        for (GeoBone bone : model.topLevelBones)
        {
            addBone(bone, body);
        }
    }

    private void addBone(GeoBone bone, Piece piece)
    {
        final Piece own = pieceByBone.get(bone.name);

        // As GeckoLib's renderer transforms a bone, but an armour bone's rotation is its part's.
        stack.push();
        stack.translate(bone);
        stack.moveToPivot(bone);
        if (own == null)
        {
            stack.rotate(bone);
        }
        stack.scale(bone);
        stack.moveBackFromPivot(bone);

        if (own != null)
        {
            piece = own;
        }

        if (!bone.isHidden())
        {
            for (GeoCube cube : bone.childCubes)
            {
                addCube(cube, piece);
            }
        }
        if (!bone.childBonesAreHiddenToo())
        {
            for (GeoBone child : bone.childBones)
            {
                addBone(child, piece);
            }
        }

        stack.pop();
    }

    private void addCube(GeoCube cube, Piece piece)
    {
        stack.push();
        stack.moveToPivot(cube);
        stack.rotate(cube);
        stack.moveBackFromPivot(cube);

        final Matrix4f modelMatrix = stack.getModelMatrix();
        final Matrix3f normalMatrix = stack.getNormalMatrix();
        final float cut = piece.part.getCutPlane();

        for (GeoQuad quad : cube.quads)
        {
            if (quad == null)
            {
                continue;
            }

            normal.set(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            normalMatrix.transform(normal);
            // GeckoLib's own fix for the shading of flat cubes.
            if ((cube.size.y == 0 || cube.size.z == 0) && normal.x < 0) normal.x *= -1;
            if ((cube.size.x == 0 || cube.size.z == 0) && normal.y < 0) normal.y *= -1;
            if ((cube.size.x == 0 || cube.size.y == 0) && normal.z < 0) normal.z *= -1;

            for (int i = 0; i < 4; i++)
            {
                position.set(quad.vertices[i].position.x, quad.vertices[i].position.y, quad.vertices[i].position.z, 1);
                modelMatrix.transform(position);

                final int at = i * ArmorMesh.STRIDE;
                polygon[at] = -position.x * 16 - piece.pivotX;
                polygon[at + 1] = GEO_ORIGIN_Y - position.y * 16 - piece.pivotY;
                polygon[at + 2] = position.z * 16 - piece.pivotZ;
                polygon[at + 3] = quad.vertices[i].textureU;
                polygon[at + 4] = quad.vertices[i].textureV;
                polygon[at + 5] = -normal.x;
                polygon[at + 6] = -normal.y;
                polygon[at + 7] = normal.z;
            }

            if (Float.isNaN(cut))
            {
                piece.upper.addPolygon(polygon, 0, 4);
            }
            else
            {
                ArmorMesh.slice(polygon, 4, cut, piece.upper, piece.lower, scratch);
            }
        }

        stack.pop();
    }

    /** Draws the pieces, each in its part as posed. The model's texture has to be bound. */
    void draw(float scale, float red, float green, float blue, float alpha)
    {
        for (Piece piece : pieces)
        {
            draw(piece, scale, red, green, blue, alpha);
        }
    }

    static void draw(Piece piece, float scale, float red, float green, float blue, float alpha)
    {
        if (!piece.upper.isEmpty())
        {
            GlStateManager.pushMatrix();
            piece.part.applyTransform(scale);
            draw(piece.upper, scale, red, green, blue, alpha);
            GlStateManager.popMatrix();
        }
        if (!piece.lower.isEmpty())
        {
            GlStateManager.pushMatrix();
            piece.part.applyLowerTransform(scale);
            draw(piece.lower, scale, red, green, blue, alpha);
            GlStateManager.popMatrix();
        }
    }

    private static void draw(ArmorMesh mesh, float scale, float red, float green, float blue, float alpha)
    {
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        final float[] data = mesh.getData();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (int i = 0; i < mesh.getVertexCount(); i++)
        {
            final int at = i * ArmorMesh.STRIDE;
            buffer.pos(data[at] * scale, data[at + 1] * scale, data[at + 2] * scale)
                  .tex(data[at + 3], data[at + 4])
                  .color(red, green, blue, alpha)
                  .normal(data[at + 5], data[at + 6], data[at + 7])
                  .endVertex();
        }
        Tessellator.getInstance().draw();
    }
}
