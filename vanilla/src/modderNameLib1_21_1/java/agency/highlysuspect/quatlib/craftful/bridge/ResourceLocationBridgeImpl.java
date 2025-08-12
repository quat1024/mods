package agency.highlysuspect.quatlib.craftful.bridge;

import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationBridgeImpl implements ResourceLocationBridge<ResourceLocation> {
	@Override
	public ResourceLocation make(String ns, String path) {
		return ResourceLocation.fromNamespaceAndPath(ns, path);
	}
	
	@Override
	public String ns(ResourceLocation rl) {
		return rl.getNamespace();
	}
	
	@Override
	public String path(ResourceLocation rl) {
		return rl.getPath();
	}
}
