package protosky.mixins;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin {
    @Inject(method = "method_20613", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus FEATURES. To find the inject method you need to read the bytecode.
    //This is where blocks structures should get placed, now it's where the structures ProtoSky needs get placed.
    private static void FEATURES(ChunkStatus targetStatus, Executor executor, ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureManager, ServerLightingProvider lightingProvider, Function function, List<Chunk> chunks, Chunk chunk, boolean regenerate, CallbackInfoReturnable<CompletableFuture> cir) {
        ProtoChunk protoChunk = (ProtoChunk) chunk;
        protoChunk.setLightingProvider(lightingProvider);
        if (regenerate || !chunk.getStatus().isAtLeast(targetStatus)) {
            Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));

            //This would normally generate structures, the blocks, not the bounding boxes.
            ChunkRegion chunkRegion = new ChunkRegion(world, chunks, targetStatus, 1);
            generator.generateFeatures(chunkRegion, chunk, world.getStructureAccessor().forRegion(chunkRegion));


            //Generate do the structures then delete blocks while in the end to remove the end cities
            if (world.getRegistryKey() == World.END) {
                ChunkRegion chunkRegion1 = new ChunkRegion(world, chunks, targetStatus, 1);
                //This generates all the structures
                StructureHelper.handleStructures(chunkRegion1, chunk, world.getStructureAccessor().forRegion(chunkRegion1), generator);
                //Delete all the terrain and end cities. I couldn't figure out how to generate just the shulkers and elytra, so I resorted to just generating the whole thing and deleting the blocks.
                WorldGenUtils.deleteBlocks((ProtoChunk) chunk, world);

                PillarHelper.generate(world, protoChunk);

            //Do it the other way around when generating the ow as to leave the end portal frames.
            } else {
                ChunkRegion chunkRegion1 = new ChunkRegion(world, chunks, targetStatus, 1);
                //Delete all the terrain and end cities. I couldn't figure out how to generate just the shulkers and elytra, so I resorted to just generating the whole thing and deleting the blocks.
                WorldGenUtils.deleteBlocks((ProtoChunk) chunk, world);
                //This generates all the structures
                StructureHelper.handleStructures(chunkRegion1, chunk, world.getStructureAccessor().forRegion(chunkRegion1), generator);
            }

            protoChunk.setStatus(targetStatus);
        }
        cir.setReturnValue(lightingProvider.retainData(chunk).thenApply(Either::left));
        //cir.setReturnValue(CompletableFuture.completedFuture(Either.left(chunk)));
    }
    @Inject(method = "method_38277", at = @At("HEAD"), cancellable = true)
    //This is under ChunkStatus HEIGHTMAPS. To find the inject method you need to read the bytecode.
    //Spawning for structures should be done here
    private static void HEIGHTMAPS(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List chunks, Chunk chunk, CallbackInfo ci) {
        //Move the heightmaps down to y0
        //This gets done here not above in FEATURES because there are multiple threads that generate features. It may place a structure in a chunk then move the heightmap
        // down to y=-64 when a second structure in the chunk on a different thread still needs it at the terrain's height.
        WorldGenUtils.genHeightMaps((ProtoChunk) chunk);
        ci.cancel();
    }

//    @Inject(method = "method_17033", at = @At("HEAD"), cancellable = true)
//    //This is under ChunkStatus SPAWN. To find the inject method you need to read the bytecode.
//    //Spawning entities should be skipped here
//    private static void SPAWN(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator generator, List chunks, Chunk chunk, CallbackInfo ci) {
//        ci.cancel();
//    }
}