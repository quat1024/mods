package agency.highlysuspect.rebindnarrator.craftful.frg;

import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(RebindNarratorBase.MODID)
public class FakeClientEntrypoint {
	public FakeClientEntrypoint() {
		//GOOD MODLOADER!!!!!!!!!!!!!!
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClassloadGuard::doIt);
	}
	
	public static class ClassloadGuard {
		public static void doIt() {
			new RebindNarratorForgeInit();
		}
	}
}
