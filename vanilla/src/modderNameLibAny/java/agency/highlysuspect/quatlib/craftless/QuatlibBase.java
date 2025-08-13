package agency.highlysuspect.quatlib.craftless;

import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.Reg;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.*;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Objects;

public abstract class QuatlibBase {
	public QuatlibBase(PhysicalSide side, PhysicalLoader loader) {
		INST = this;
		this.side = side;
		this.loader = loader;
		this.watcher = makeSharedConfigFileWatcher();
		this.rlBridge = makeResourceLocationBridge();
	}
	
	public static final String MODID = "modder_name_lib";
	public static final String NAME = "ModderNameLib";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	
	public final PhysicalSide side;
	public final PhysicalLoader loader;
	
	public final FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public final SharedConfigFileWatcher watcher;
	public final ResourceLocationBridge<ResourceLocation> rlBridge;
	
	protected abstract SharedConfigFileWatcher makeSharedConfigFileWatcher();
	protected abstract ResourceLocationBridge<ResourceLocation> makeResourceLocationBridge();
	
	public abstract Reg<?> createReg(RegType<?> type);
	public abstract void registerDispenserBehavior(Latch<? extends ItemLike> item, DispenseItemBehavior behavior);
	public abstract <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks);
	public abstract <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier);
	
	protected static QuatlibBase INST;
	public static QuatlibBase inst() {
		return Objects.requireNonNull(INST, "QuatlibBase is null");
	}
}
