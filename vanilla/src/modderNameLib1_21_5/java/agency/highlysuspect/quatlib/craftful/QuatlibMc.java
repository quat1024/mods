package agency.highlysuspect.quatlib.craftful;

import agency.highlysuspect.quatlib.craftful.bridge.ResourceLocationBridgeImpl;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class QuatlibMc extends QuatlibBase {
	@Override
	protected SharedConfigFileWatcher makeSharedConfigFileWatcher() {
		return new SharedConfigFileWatcher(LOG);
	}
	
	@Override
	protected ResourceLocationBridge<ResourceLocation> makeResourceLocationBridge() {
		return new ResourceLocationBridgeImpl();
	}
	
	@Override
	public BlockItem basicBlockItem(Block b) {
		return new BlockItem(b, new Item.Properties());
	}
	
	public static QuatlibMc inst() {
		return (QuatlibMc) QuatlibBase.INST;
	}
}
