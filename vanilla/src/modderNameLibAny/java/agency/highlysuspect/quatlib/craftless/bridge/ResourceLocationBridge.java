package agency.highlysuspect.quatlib.craftless.bridge;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface ResourceLocationBridge {
	ResourceLocation id(String namespace, String path);
	
	String ns(ResourceLocation rl);
	String path(ResourceLocation rl);
	
	default ResourceLocation mapPath(ResourceLocation in, UnaryOperator<String> op) {
		return id(ns(in), op.apply(path(in)));
	}
	
	default Function<String, ResourceLocation> factory(String ns) {
		return path -> id(ns, path);
	}
}
