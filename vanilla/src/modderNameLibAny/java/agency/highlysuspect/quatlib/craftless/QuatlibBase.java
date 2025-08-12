package agency.highlysuspect.quatlib.craftless;

import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import agency.highlysuspect.quatlib.craftless.failure.FailureLogger;
import agency.highlysuspect.quatlib.craftless.failure.FailureRoot;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class QuatlibBase {
	public QuatlibBase() {
		INST = this;
	}
	
	public static final String MODID = "modder_name_lib";
	public static final String NAME = "ModderNameLib";
	public static final LogFacade LOG = new Slf4jLogFacade(NAME);
	protected static QuatlibBase INST;
	
	public final FailureRoot failures = new FailureRoot(NAME).addListener(new FailureLogger(LOG));
	public final SharedConfigFileWatcher watcher = makeSharedConfigFileWatcher();
	public final ResourceLocationBridge<ResourceLocation> rlBridge = makeResourceLocationBridge();
	
	protected abstract SharedConfigFileWatcher makeSharedConfigFileWatcher();
	protected abstract ResourceLocationBridge<ResourceLocation> makeResourceLocationBridge();
	
	public abstract <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks);
	
	public static QuatlibBase inst() {
		return INST;
	}
}
