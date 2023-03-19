package protosky.mixins.testing;

import net.minecraft.structure.SimpleStructurePiece;
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
import protosky.mixins.StructureHelperInvokers.SimpleStructurePieceInvoker;
import protosky.mixins.StructureHelperInvokers.StructurePieceInvoker;

@Mixin(SimpleStructurePiece.class)
public class SimpleStructurePieceMixin {
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    void generateMixin(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        SimpleStructurePiece This = ((SimpleStructurePiece)(Object)this);
        StructurePieceInvoker structurePieceInvoker = ((StructurePieceInvoker) This);
        SimpleStructurePieceInvoker invoker = (SimpleStructurePieceInvoker) This;

        invoker.getPlacementData().setBoundingBox(chunkBox);
        structurePieceInvoker.setBoundingBox(invoker.getTemplate().calculateBoundingBox(invoker.getPlacementData(), invoker.getPos()));
        ci.cancel();
    }
}
