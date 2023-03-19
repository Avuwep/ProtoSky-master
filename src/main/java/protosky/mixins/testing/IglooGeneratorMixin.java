package protosky.mixins.testing;

import net.minecraft.structure.IglooGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IglooGenerator.Piece.class)
public class IglooGeneratorMixin {
    /*@Inject(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        SimpleStructurePieceInvoker simplePieceInvoker = ((SimpleStructurePieceInvoker) structurePiece);
        SimpleStructurePiece simplePiece = ((SimpleStructurePiece) structurePiece);
        IglooGeneratorPieceInvoker iglooGeneratorPieceInvoker = ((IglooGeneratorPieceInvoker) structurePiece);
        final Identifier TOP_TEMPLATE = new Identifier("igloo/top");
        final Identifier MIDDLE_TEMPLATE = new Identifier("igloo/middle");
        final Identifier BOTTOM_TEMPLATE = new Identifier("igloo/bottom");
        final Map<Identifier, BlockPos> OFFSETS_FROM_TOP = ImmutableMap.of(
                TOP_TEMPLATE, BlockPos.ORIGIN, MIDDLE_TEMPLATE, new BlockPos(2, -3, 4), BOTTOM_TEMPLATE, new BlockPos(0, -3, -2)
        );

        Identifier identifier = new Identifier(simplePieceInvoker.getTemplateIdString());
        StructurePlacementData structurePlacementData = iglooGeneratorPieceInvoker.invokeCreatePlacementData(simplePieceInvoker.getPlacementData().getRotation(), identifier);

        BlockPos blockPos = OFFSETS_FROM_TOP.get(identifier);
        BlockPos blockPos2 = simplePieceInvoker.getPos().add(StructureTemplate.transform(structurePlacementData, new BlockPos(3 - blockPos.getX(), 0, -blockPos.getZ())));

        int i = worldAccess.getTopY(Heightmap.Type.WORLD_SURFACE_WG, blockPos2.getX(), blockPos2.getZ());
        BlockPos posCache = simplePieceInvoker.getPos();

        simplePieceInvoker.setPos(simplePieceInvoker.getPos().add(0, i - 90 - 1, 0));
        simplePieceInvoker.setPos(simplePieceInvoker.getPos().add(0, 0, 0));
        simplePiece.generate(worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);

        if (identifier.equals(TOP_TEMPLATE)) {
            BlockPos blockPos4 = simplePieceInvoker.getPos().add(StructureTemplate.transform(structurePlacementData, new BlockPos(3, 0, 5)));
            BlockState blockState = worldAccess.getBlockState(blockPos4.down());
            if (!blockState.isAir() && !blockState.isOf(Blocks.LADDER)) {
                worldAccess.setBlockState(blockPos4, Blocks.SNOW_BLOCK.getDefaultState(), 3);
            }
        }

        simplePieceInvoker.setPos(posCache);

        ci.cancel();
    }*/
}
