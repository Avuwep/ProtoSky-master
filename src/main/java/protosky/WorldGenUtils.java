package protosky;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import protosky.mixins.ProtoChunkAccessor;

import java.util.Map;
import java.util.Optional;

public class WorldGenUtils
{
    public static void deleteBlocks(ProtoChunk chunk, ServerWorld world)
    {
        ChunkSection[] sections = chunk.getSectionArray();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection chunkSection = sections[i];
            PalettedContainer<BlockState> blockStateContainer = new PalettedContainer<>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            ReadableContainer<RegistryEntry<Biome>> biomeContainer = chunkSection.getBiomeContainer();
            int chunkPos = chunkSection.getYOffset() >> 4;
            sections[i] = new ChunkSection(chunkPos, blockStateContainer, biomeContainer);
        }
        for (BlockPos bePos : chunk.getBlockEntityPositions()) {
            chunk.removeBlockEntity(bePos);
        }
        ((ProtoChunkAccessor) chunk).getLightSources().clear();
    }

    public static void genHeightMaps(ProtoChunk chunk) {
        // defined in Heightmap class constructor
        int elementBits = MathHelper.ceilLog2(chunk.getHeight() + 1);
        long[] emptyHeightmap = new PackedIntegerArray(elementBits, 256).getData();
        for (Map.Entry<Heightmap.Type, Heightmap> heightmapEntry : chunk.getHeightmaps())
        {
            heightmapEntry.getValue().setTo(chunk, heightmapEntry.getKey(), emptyHeightmap);
        }
    }


    public static void clearEntities(ProtoChunk chunk, ServerWorld world) {
        // erase entities
        if (!(world.getRegistryKey() == World.END)) {
            //chunk.getEntities().clear();
        } else {
            chunk.getEntities().removeIf(tag -> {
                String id = tag.getString("id");
                return !id.equals("minecraft:end_crystal") && !id.equals("minecraft:shulker") && !id.equals("minecraft:item_frame");
            });
        }
    }

    public static void genSpawnPlatform(Chunk chunk, ServerWorld world) {
        StructureTemplateManager man = world.getStructureTemplateManager();
        StructureTemplate s = null;

        // Get structure for this dimension
        if (world.getRegistryKey() == World.OVERWORLD) {
            Optional<StructureTemplate> op = man.getTemplate(new Identifier("protosky:spawn_overworld"));
            if (op.isPresent()) {
                s = op.get();
            }
        } else if (world.getRegistryKey() == World.NETHER) {
            Optional<StructureTemplate> op = man.getTemplate(new Identifier("protosky:spawn_nether"));
            if (op.isPresent()) {
                s = op.get();
            }
        }
        if (s == null) return;

        ChunkPos chunkPos = chunk.getPos();
        BlockPos blockPos = new BlockPos(chunkPos.x * 16, 64, chunkPos.z * 16);

        StructurePlacementData structurePlacementData = new StructurePlacementData().setUpdateNeighbors(true);

        int flags = 0;
        s.place(world, blockPos, blockPos, structurePlacementData, new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()), flags);
        world.setSpawnPos(blockPos.add(s.getSize().getX() / 2, s.getSize().getY() + 1, s.getSize().getZ() / 2), 0);
    }
}
