package agency.highlysuspect.quatlib.craftless.util;

import java.util.function.Supplier;

public enum PhysicalSide {
	CLIENT,
	DEDICATED_SERVER;
	
	boolean isClient() {
		return this == CLIENT;
	}
	
	boolean isDedicatedServer() {
		return this == DEDICATED_SERVER;
	}
	
	public void runOnClient(Supplier<Runnable> guard) {
		if(this == CLIENT) guard.get().run();
	}
	
	public <T> T clientServer(Supplier<Supplier<T>> guardClient, Supplier<Supplier<T>> guardServer) {
		if(this == CLIENT) return guardClient.get().get();
		else return guardServer.get().get();
	}
}
