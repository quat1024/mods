package agency.highlysuspect;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class Hiiihellofabric {
	static {
		System.out.println(FabricLoader.class);
		System.out.println(MinecraftServer.class); //are you remapped
	}
}
