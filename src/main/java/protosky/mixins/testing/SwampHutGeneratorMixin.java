package protosky.mixins.testing;

import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import protosky.mixins.StructureHelperInvokers.ShiftableStructurePieceInvoker;

@Mixin(SwampHutGenerator.class)
public abstract class SwampHutGeneratorMixin {
    @Accessor("hasWitch")
    abstract boolean getHasWitch();

    @Accessor("hasWitch")
    abstract void setHasWitch(boolean hasWitch);

    @Invoker("spawnCat")
    abstract void invokeSpawnCat(ServerWorldAccess world, BlockBox box);

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        SwampHutGenerator This = ((SwampHutGenerator)(Object)this);
        ShiftableStructurePieceInvoker shiftableStructurePieceInvoker = ((ShiftableStructurePieceInvoker) This);

        shiftableStructurePieceInvoker.invokeAdjustToAverageHeight(world, chunkBox, 0);
        ci.cancel();
    }
}
