package agency.highlysuspect.quatlib.craftful.bridge;

import agency.highlysuspect.quatlib.craftless.bridge.Bridge;
import agency.highlysuspect.quatlib.craftless.bridge.Id;
import net.minecraft.resources.ResourceLocation;

public class BridgeImpl implements Bridge<ResourceLocation> {
	@Override
	public Id<ResourceLocation> id() {
		return IdImpl.INSTANCE;
	}
}
