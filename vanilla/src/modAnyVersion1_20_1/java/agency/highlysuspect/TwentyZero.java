package agency.highlysuspect;

import net.minecraft.server.MinecraftServer;

public class TwentyZero {
	static {
		System.out.println("only in 1.20.1");
		System.out.println("minecraftserver: " + MinecraftServer.class);
	}
	
	void foo(MinecraftServer server) {
		if(server.acceptsFailure()) {
			System.out.println("ok");
		}
	}
}
