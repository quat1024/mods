package agency.highlysuspect.rebindnarrator.craftful.fab.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/// don't ship this ///

@Mixin(Minecraft.class)
public class TestMixin {
	//test shadow
	@Shadow private Path resourcePackDirectory;
	
	//test this refmap works
	@Inject(method = "tick", at = @At("HEAD"))
	private void asdasdasdOnTick(CallbackInfo ci) {
		//yeah
	}
}
