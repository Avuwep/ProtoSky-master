package protosky.mixins.StructureHelperInvokers;

import net.minecraft.structure.ShipwreckGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShipwreckGenerator.Piece.class)
public interface ShipwreckGeneratorPieceInvoker {
    @Accessor("grounded")
    boolean getGrounded();
}
