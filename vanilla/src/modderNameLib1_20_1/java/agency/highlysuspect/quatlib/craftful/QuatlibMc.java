package agency.highlysuspect.quatlib.craftful;

import agency.highlysuspect.quatlib.craftful.bridge.ResourceLocationBridgeImpl;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public abstract class QuatlibMc extends QuatlibBase {
	@Override
	protected SharedConfigFileWatcher makeSharedConfigFileWatcher() {
		return new SharedConfigFileWatcher(LOG);
	}
	
	@Override
	protected ResourceLocationBridge<ResourceLocation> makeResourceLocationBridge() {
		return new ResourceLocationBridgeImpl();
	}
	
	public abstract CreativeModeTab.Builder makeCreativeModeTabBuilder();
	
	public static QuatlibMc inst() {
		return (QuatlibMc) QuatlibBase.INST;
	}
}
