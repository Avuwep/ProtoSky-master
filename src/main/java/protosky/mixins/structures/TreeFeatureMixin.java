package protosky.mixins.structures;


import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(TreeFeature.class)
public class TreeFeatureMixin {
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void onGenerate(StructureWorldAccess world, Random random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootPlacerReplacer, BiConsumer<BlockPos, BlockState> trunkPlacerReplacer, FoliagePlacer.BlockPlacer blockPlacer, TreeFeatureConfig config, CallbackInfoReturnable ci) {
        Chunk chunk = world.getChunk(pos);
        //Only generate trees if the chunks have been generated (Don't generate trees naturally but saplings should grow)
        if (!chunk.isLightOn()) {
            //System.out.println("Trees blocked!");
            ci.cancel();
        }
    }

}
