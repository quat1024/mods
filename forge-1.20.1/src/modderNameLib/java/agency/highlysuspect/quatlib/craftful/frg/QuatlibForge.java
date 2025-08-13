package agency.highlysuspect.quatlib.craftful.frg;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftful.frg.client.QuatlibClientForge;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import agency.highlysuspect.quatlib.craftless.util.MyMenuSupplier;
import agency.highlysuspect.quatlib.craftless.util.PhysicalSide;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod(QuatlibBase.MODID)
public class QuatlibForge extends QuatlibMc {
	public QuatlibForge() {
		super();
		this.modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modBus.register(this);
		
		//if i was forge i would simply have client entrypoints
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuatlibClientForge::new);
	}
	
	public final IEventBus modBus;
	private final Map<Latch<? extends ItemLike>, DispenseItemBehavior> dispenseBehaviorsToRegister = new HashMap<>();
	
	@Override
	protected PhysicalSide findSide() {
		return FMLEnvironment.dist.isClient() ? PhysicalSide.CLIENT : PhysicalSide.DEDICATED_SERVER;
	}
	
	@Override
	public Reg<?> createReg(RegType<?> type) {
		ForgeReg<?> reg = new ForgeReg<>(convertRegType(type));
		modBus.register(reg);
		return reg;
	}
	
	@Override
	public void registerDispenserBehavior(Latch<? extends ItemLike> item, DispenseItemBehavior behavior) {
		dispenseBehaviorsToRegister.put(item, behavior);
	}
	
	@SuppressWarnings("DataFlowIssue") //dfu param
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return new BlockEntityType<>(factory::create, Set.of(blocks), null); //Access widened by forge
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		return new MenuType<>(supplier::create, FeatureFlagSet.of()); //Access widened by forge
	}
	
	@Override
	public CreativeModeTab.Builder makeCreativeModeTabBuilder() {
		return CreativeModeTab.builder(); //forge-added zero arg method
	}
	
	@SubscribeEvent
	private void actuallyRegisterDispenserBehaviors(FMLCommonSetupEvent e) {
		e.enqueueWork(() -> {
			dispenseBehaviorsToRegister.forEach((handle, behavior) -> DispenserBlock.registerBehavior(handle.get(), behavior));
			dispenseBehaviorsToRegister.clear();
		});
	}
	
	public static QuatlibForge inst() {
		return (QuatlibForge) QuatlibBase.INST;
	}
}
