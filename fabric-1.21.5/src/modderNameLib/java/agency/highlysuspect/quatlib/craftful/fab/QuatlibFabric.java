package agency.highlysuspect.quatlib.craftful.fab;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibInitializer;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import agency.highlysuspect.quatlib.craftless.util.MyMenuSupplier;
import agency.highlysuspect.quatlib.craftless.util.PhysicalSide;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
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
	protected PhysicalSide findSide() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? PhysicalSide.CLIENT : PhysicalSide.DEDICATED_SERVER;
	}
	
	@Override
	public Reg<?> createReg(RegType<?> type) {
		return new FabricReg<>(convertRegType(type));
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		//return BlockEntityType.Builder.of(factory::create, blocks).build(null); //what. where's Builder
		return FabricBlockEntityTypeBuilder.create(factory::create, blocks).build();
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		return new MenuType<>(supplier::create, FeatureFlagSet.of()); //Access widened by fabric
	}
	
	public static QuatlibFabric inst() {
		return (QuatlibFabric) QuatlibBase.INST;
	}
}
