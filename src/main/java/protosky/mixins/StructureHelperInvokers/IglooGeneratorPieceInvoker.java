package protosky.mixins.StructureHelperInvokers;

import net.minecraft.structure.IglooGenerator;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(IglooGenerator.Piece.class)
public interface IglooGeneratorPieceInvoker {
    @Invoker("createPlacementData")
    StructurePlacementData invokeCreatePlacementData(BlockRotation rotation, Identifier identifier);
}
