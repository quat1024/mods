package agency.highlysuspect.quatlib.craftful.bridge;

import agency.highlysuspect.quatlib.craftless.bridge.Id;
import net.minecraft.resources.ResourceLocation;

public class IdImpl implements Id<ResourceLocation> {
	public static final IdImpl INSTANCE = new IdImpl();
	
	@Override
	public ResourceLocation id(String namespace, String path) {
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
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
