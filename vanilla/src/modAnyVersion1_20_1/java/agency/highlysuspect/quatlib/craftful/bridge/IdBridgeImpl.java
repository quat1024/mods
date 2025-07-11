package agency.highlysuspect.quatlib.craftful.bridge;

import agency.highlysuspect.quatlib.craftless.bridge.IdBridge;
import net.minecraft.resources.ResourceLocation;

public class IdBridgeImpl implements IdBridge<ResourceLocation> {
	public static final IdBridgeImpl INSTANCE = new IdBridgeImpl();
	
	@Override
	public ResourceLocation id(String namespace, String path) {
		return new ResourceLocation(namespace, path);
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
