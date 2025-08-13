package agency.highlysuspect.quatlib.craftful;

import agency.highlysuspect.quatlib.craftful.bridge.ResourceLocationBridgeImpl;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.util.PhysicalLoader;
import agency.highlysuspect.quatlib.craftless.util.PhysicalSide;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public abstract class QuatlibMc extends QuatlibBase {
	public QuatlibMc(PhysicalSide side, PhysicalLoader loader) {
		super(side, loader);
	}
	
	@Override
	protected SharedConfigFileWatcher makeSharedConfigFileWatcher() {
		return new SharedConfigFileWatcher(LOG);
	}
	
	@Override
	protected ResourceLocationBridge<ResourceLocation> makeResourceLocationBridge() {
		return new ResourceLocationBridgeImpl();
	}
	
	protected ResourceKey<?> convertRegType(RegType<?> type) {
		if(type == RegType.BLOCKS) return BuiltInRegistries.BLOCK.key();
		else if(type == RegType.ITEMS) return BuiltInRegistries.ITEM.key();
		else if(type == RegType.CREATIVE_TABS) return BuiltInRegistries.CREATIVE_MODE_TAB.key();
		else if(type == RegType.BLOCK_ENTITY_TYPES) return BuiltInRegistries.BLOCK_ENTITY_TYPE.key();
		else if(type == RegType.SOUND_EVENTS) return BuiltInRegistries.SOUND_EVENT.key();
		else if(type == RegType.MENU_TYPES) return BuiltInRegistries.MENU.key();
		else throw new UnsupportedOperationException("Don't know what BuiltInRegistry " + type + " converts to");
	}
	
	public static QuatlibMc inst() {
		return (QuatlibMc) QuatlibBase.INST;
	}
}
