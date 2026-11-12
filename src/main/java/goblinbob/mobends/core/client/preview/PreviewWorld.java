package goblinbob.mobends.core.client.preview;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The world the previews of the Settings window stand in: an endless flat floor and nothing else,
 * so what an animator finds around a preview (ground, ledges, raycasts) doesn't depend on where the
 * player is. It is never ticked, and no world events are posted for it.
 * <p>
 * Each preview entity is registered with the bender it shows, which animates it whatever type
 * would win for that entity in the game (see {@link #getSelection}). Its data stays in the entity
 * database until the world is {@link #close() closed}.
 */
public class PreviewWorld extends World
{

    /** The height of the floor's top: entities stand at this y. */
    public static final int FLOOR_Y = 64;
    /** The chunks kept before they are made anew; a walking preview keeps entering new ones. */
    private static final int MAX_CHUNKS = 64;

    /** Preview entities get negative ids, which no entity of the game has (the database keys by id). */
    private static int nextEntityId = -1;

    private final Map<EntityLivingBase, EntityBender<?>> benders = new IdentityHashMap<>();
    private final Map<EntityLivingBase, EntityTypeRegistry.Selection> selections = new IdentityHashMap<>();

    public PreviewWorld()
    {
        super(new SaveHandlerMP(), new WorldInfo(new WorldSettings(0L, GameType.SURVIVAL, false, false, WorldType.FLAT), "MoBendsPreview"),
                new WorldProviderSurface(), new Profiler(), true);
        this.provider.registerWorld(this);
        this.chunkProvider = this.createChunkProvider();
        this.mapStorage = new SaveDataMemoryStorage();
    }

    /** Makes the entity a preview of {@code bender}, standing on the floor at the origin. */
    public void register(EntityLivingBase entity, EntityBender<?> bender)
    {
        entity.setEntityId(nextEntityId--);
        entity.setPositionAndRotation(0.5D, FLOOR_Y, 0.5D, 0F, 0F);
        // The constructor turns the head a random way, and nothing ticks the entity to catch the
        // previous yaw up: the head would turn between the two with the partial ticks.
        entity.rotationYawHead = entity.prevRotationYawHead = 0F;
        entity.renderYawOffset = entity.prevRenderYawOffset = 0F;
        entity.prevPosX = entity.lastTickPosX = entity.posX;
        entity.prevPosY = entity.lastTickPosY = entity.posY;
        entity.prevPosZ = entity.lastTickPosZ = entity.posZ;
        entity.onGround = true;
        this.entitiesById.addKey(entity.getEntityId(), entity);
        this.benders.put(entity, bender);
    }

    /**
     * The type the preview entity is animated by: the first one (in precedence order) that gives it
     * the bender it shows, preferring one whose selector holds for it. Null if there is none, or
     * the entity isn't a preview of this world.
     */
    @Nullable
    public EntityTypeRegistry.Selection getSelection(EntityLivingBase entity)
    {
        EntityBender<?> bender = benders.get(entity);
        if (bender == null)
        {
            return null;
        }
        return selections.computeIfAbsent(entity, e -> EntityTypeRegistry.INSTANCE.selectionAs(bender, e));
    }

    /** Lets go of one preview entity: its data leaves the entity database on the next tick. */
    public void remove(EntityLivingBase entity)
    {
        this.entitiesById.removeObject(entity.getEntityId());
        this.benders.remove(entity);
        this.selections.remove(entity);
    }

    /** Lets go of the preview entities: their data leaves the entity database on the next tick. */
    public void close()
    {
        this.entitiesById.clearMap();
        this.benders.clear();
        this.selections.clear();
    }

    @Override
    public boolean spawnEntityInWorld(Entity entity)
    {
        // Nothing an entity's constructor might spawn joins the preview.
        return false;
    }

    @Override
    protected IChunkProvider createChunkProvider()
    {
        return new FloorChunkProvider();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
    {
        return true;
    }

    @Override
    protected int getRenderDistanceChunks()
    {
        return 2;
    }

    private class FloorChunkProvider implements IChunkProvider
    {

        private final Map<Long, Chunk> chunks = new HashMap<>();

        @Override
        public boolean chunkExists(int x, int z)
        {
            return true;
        }

        @Override
        public Chunk provideChunk(int x, int z)
        {
            final long key = ChunkCoordIntPair.chunkXZ2Int(x, z);
            Chunk chunk = chunks.get(key);
            if (chunk == null)
            {
                if (chunks.size() >= MAX_CHUNKS)
                {
                    chunks.clear();
                }
                chunk = makeFloor(x, z);
                chunks.put(key, chunk);
            }
            return chunk;
        }

        @Override
        public Chunk provideChunk(BlockPos pos)
        {
            return provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
        }

        private Chunk makeFloor(int x, int z)
        {
            final ChunkPrimer primer = new ChunkPrimer();
            final IBlockState grass = Blocks.grass.getDefaultState();
            final IBlockState dirt = Blocks.dirt.getDefaultState();
            for (int blockX = 0; blockX < 16; ++blockX)
            {
                for (int blockZ = 0; blockZ < 16; ++blockZ)
                {
                    for (int y = FLOOR_Y - 4; y < FLOOR_Y; ++y)
                    {
                        primer.setBlockState(blockX, y, blockZ, y == FLOOR_Y - 1 ? grass : dirt);
                    }
                }
            }
            final Chunk chunk = new Chunk(PreviewWorld.this, primer, x, z);
            chunk.generateSkylightMap();
            return chunk;
        }

        @Override
        public void populate(IChunkProvider provider, int x, int z)
        {
        }

        @Override
        public boolean populateChunk(IChunkProvider provider, Chunk chunk, int x, int z)
        {
            return false;
        }

        @Override
        public boolean saveChunks(boolean saveAll, IProgressUpdate progress)
        {
            return true;
        }

        @Override
        public boolean unloadQueuedChunks()
        {
            return false;
        }

        @Override
        public boolean canSave()
        {
            return false;
        }

        @Override
        public String makeString()
        {
            return "PreviewChunkCache: " + chunks.size();
        }

        @Override
        public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos)
        {
            return Collections.emptyList();
        }

        @Override
        public BlockPos getStrongholdGen(World world, String name, BlockPos pos)
        {
            return null;
        }

        @Override
        public int getLoadedChunkCount()
        {
            return chunks.size();
        }

        @Override
        public void recreateStructures(Chunk chunk, int x, int z)
        {
        }

        @Override
        public void saveExtraData()
        {
        }

    }

}
