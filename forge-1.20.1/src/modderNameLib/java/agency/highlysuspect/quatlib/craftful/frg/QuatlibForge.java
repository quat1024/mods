package agency.highlysuspect.quatlib.craftful.frg;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod(QuatlibBase.MODID)
public class QuatlibForge extends QuatlibMc {
	public QuatlibForge() {
		super();
	}
	
	@SuppressWarnings("DataFlowIssue") //dfu param
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return new BlockEntityType<>(factory::create, Set.of(blocks), null); //Access widened by forge
	}
	
	public static QuatlibForge inst() {
		return (QuatlibForge) QuatlibBase.INST;
	}
}
