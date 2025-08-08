package agency.highlysuspect.quatlib.craftful;

import agency.highlysuspect.quatlib.craftful.bridge.ResourceLocationBridgeImpl;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.bridge.ResourceLocationBridge;
import agency.highlysuspect.quatlib.craftless.util.SharedConfigFileWatcher;

public class QuatlibMc extends QuatlibBase {
	public final ResourceLocationBridge idBridge = new ResourceLocationBridgeImpl();
	
	@Override
	protected SharedConfigFileWatcher makeSharedConfigFileWatcher() {
		return new SharedConfigFileWatcher(LOG);
	}
	
	public static QuatlibMc inst() {
		return (QuatlibMc) QuatlibBase.INST;
	}
}
