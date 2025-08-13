package agency.highlysuspect.quatlib.craftful.neo;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import agency.highlysuspect.quatlib.craftless.util.PhysicalSide;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(QuatlibBase.MODID)
public class QuatlibNeoforge extends QuatlibMc {
	public QuatlibNeoforge() {
		super();
	}
	
	@Override
	protected PhysicalSide findSide() {
		return FMLEnvironment.dist.isClient() ? PhysicalSide.CLIENT : PhysicalSide.DEDICATED_SERVER;
	}
	
	@SuppressWarnings("DataFlowIssue")
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return BlockEntityType.Builder.of(factory::create).build(null);
	}
	
	public static QuatlibNeoforge inst() {
		return (QuatlibNeoforge) QuatlibBase.INST;
	}
}
