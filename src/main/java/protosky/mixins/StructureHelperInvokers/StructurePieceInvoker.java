package protosky.mixins.StructureHelperInvokers;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(StructurePiece.class)
public interface StructurePieceInvoker {
    @Invoker("fillWithOutline")
    public void invokeFillWithOutline(StructureWorldAccess world, BlockBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState outline, BlockState inside, boolean cantReplaceAir);

    @Invoker("addBlock")
    public void invokeAddBlock(StructureWorldAccess world, BlockState block, int x, int y, int z, BlockBox box);

    @Invoker("fillDownwards")
    public void invokeFillDownwards(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox box);

    @Invoker("offsetPos")
    public BlockPos.Mutable invokeOffsetPos(int x, int y, int z);

    @Accessor("boundingBox")
    void setBoundingBox(BlockBox boundingBox);

    @Accessor("boundingBox")
    BlockBox getBoundingBox();
}
