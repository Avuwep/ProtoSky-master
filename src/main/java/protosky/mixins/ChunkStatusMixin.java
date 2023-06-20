package protosky.mixins;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import protosky.WorldGenUtils;
import protosky.stuctures.PillarHelper;
import protosky.stuctures.StructureHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static protosky.ProtoSkySettings.LOGGER;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin {
    @Inject(method = "method_51375", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus FEATURES. To find the inject method you need to read the bytecode. In Idea click View -> Show Bytecode
    // In there search for "features" (you need the quotes).
    //This is where blocks structures should get placed, now it's where the structures ProtoSky needs get placed.
    private static void FEATURES(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List<Chunk> chunks, Chunk chunk, CallbackInfo ci) {
        Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
        ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
        //This would normally generate structures, the blocks, not the bounding boxes.
        generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));
        //StructureHelper.handleStructures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator, false);
        //Blender.tickLeavesAndFluids(chunkRegion, chunk);

        //Generate do the structures then delete blocks while in the end to remove the end cities
        if (world.getRegistryKey() == World.END) {
            //This generates all the structures
            StructureHelper.handleStructures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator, true);
            //Delete all the terrain and end cities. I couldn't figure out how to generate just the shulkers and elytra, so I resorted to just generating the whole thing and deleting the blocks.
            WorldGenUtils.deleteBlocks(chunk, world);
            //Generate the end pillars. This is its own thing and not in handleStructures() because pillars are features not structures.
            PillarHelper.generate(world, chunk);

        //Do it the other way around when generating the ow as to leave the end portal frames.
        } else {
            //We need handle some structures before we delete blocks because they rely on blocks being there.
            StructureHelper.handleStructures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator, true);
            //Delete all the terrain and end cities. I couldn't figure out how to generate just the shulkers and elytra, so I resorted to just generating the whole thing and deleting the blocks.
            WorldGenUtils.deleteBlocks(chunk, world);
            //This generates all the structures
            StructureHelper.handleStructures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion), generator, false);
        }
        ci.cancel();
    }
    @Inject(method = "method_20614", at = @At("HEAD"), cancellable = false)
    //This is under ChunkStatus INITIALIZE_LIGHT. To find the inject method you need to read the bytecode. In Idea click View -> Show Bytecode
    // In there search for "initialize_light" (you need the quotes).
    //We need to move the heightmaps down to y = 0 after structures have been generated because some rely on the heightmap to move.
    //This used to be in the unused 'HEIGHTMAPS' status, but in 1.20 this was removed. Now we're using INITIALIZE_LIGHT.
    private static void INITIALIZE_LIGHT(ChunkStatus targetStatus, Executor executor, ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureTemplateManager, ServerLightingProvider lightingProvider, Function fullChunkConverter, List chunks, Chunk chunk, CallbackInfoReturnable<CompletableFuture> cir) {
        //Move the heightmaps down to y-64
        //This gets done here not above in FEATURES because there are multiple threads that generate features. One thread
        // may place a structure in a chunk then move the heightmap down to y=-64 when a second structure in the chunk on
        // a different thread still needs to be generated and requires the 'correct' heightmap
        //LOGGER.info("Light " + chunk.getPos());
        WorldGenUtils.genHeightMaps(chunk);

        //Don't cancel because we want the lighting to work.
        //ci.cancel();
    }

    @Inject(method = "method_17033", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus SPAWN. To find the inject method you need to read the bytecode. In Idea click View -> Show Bytecode
    // In there search for "spawn" (you need the quotes).
    //Spawning entities is skipped here. Even without this nothing would happen because entities from structures never
    // get generated because that is skipped above and non-structure entities have no blocks to spawn on so they don't spawn.
    // This is just an optimization
    private static void SPAWN(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List chunks, Chunk chunk, CallbackInfo ci) {
        ci.cancel();
    }
}