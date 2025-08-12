package agency.highlysuspect.quatlib.craftful.neo;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.common.Mod;

@Mod(QuatlibBase.MODID)
public class QuatlibNeoforge extends QuatlibMc {
	public QuatlibNeoforge() {
		super();
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return new BlockEntityType<>(factory::create, blocks); //widened by forge
	}
	
	public static QuatlibNeoforge inst() {
		return (QuatlibNeoforge) QuatlibBase.INST;
	}
}
