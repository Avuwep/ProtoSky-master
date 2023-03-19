package protosky.mixins.StructureHelperInvokers;

import net.minecraft.block.Block;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleStructurePiece.class)
public interface SimpleStructurePieceInvoker {
    @Accessor("templateIdString")
    String getTemplateIdString();

    @Accessor("placementData")
    StructurePlacementData getPlacementData();

    @Accessor("template")
    StructureTemplate getTemplate();
    @Accessor("pos")
    BlockPos getPos();

    @Accessor("pos")
    void setPos(BlockPos pos);
}
