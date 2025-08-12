package agency.highlysuspect.quatlib.craftful.fab;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class QuatlibFabric extends QuatlibMc implements ModInitializer {
	@Override
	public void onInitialize() {
		//...do stuff...
		
		FabricLoader.getInstance().invokeEntrypoints("modder_name_lib:after", AfterQuatlibInitializer.class, AfterQuatlibInitializer::onInitialize);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		//access-widened by fabric api
		return BlockEntityType.Builder.of(factory::create, blocks).build(null);
	}
	
	public static QuatlibFabric inst() {
		return (QuatlibFabric) QuatlibBase.INST;
	}
}
