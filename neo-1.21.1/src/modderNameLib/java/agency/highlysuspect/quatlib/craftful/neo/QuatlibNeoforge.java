package agency.highlysuspect.quatlib.craftful.neo;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.HashMap;
import java.util.Map;

@Mod(QuatlibBase.MODID)
public class QuatlibNeoforge extends QuatlibMc {
	public QuatlibNeoforge(IEventBus modBus) {
		super();
		this.modBus = modBus;
		
		modBus.register(this);
	}
	
	public final IEventBus modBus;
	private final Map<Latch<? extends ItemLike>, DispenseItemBehavior> dispenseBehaviorsToRegister = new HashMap<>();
	
	@Override
	protected PhysicalSide findSide() {
		return FMLEnvironment.dist.isClient() ? PhysicalSide.CLIENT : PhysicalSide.DEDICATED_SERVER;
	}
	
	@Override
	public Reg<?> createReg(RegType<?> type) {
		NeoReg<?> reg = new NeoReg<>(convertRegType(type));
		modBus.register(reg);
		return reg;
	}
	
	@Override
	public void registerDispenserBehavior(Latch<? extends ItemLike> item, DispenseItemBehavior behavior) {
		dispenseBehaviorsToRegister.put(item, behavior);
	}
	
	@SuppressWarnings("DataFlowIssue")
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return BlockEntityType.Builder.of(factory::create).build(null);
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		return new MenuType<>(supplier::create, FeatureFlagSet.of()); //Access widened by forge
	}
	
	@SubscribeEvent
	private void actuallyRegisterDispenserBehaviors(FMLCommonSetupEvent e) {
		e.enqueueWork(() -> {
			dispenseBehaviorsToRegister.forEach((handle, behavior) -> DispenserBlock.registerBehavior(handle.get(), behavior));
			dispenseBehaviorsToRegister.clear();
		});
	}
	
	public static QuatlibNeoforge inst() {
		return (QuatlibNeoforge) QuatlibBase.INST;
	}
}
