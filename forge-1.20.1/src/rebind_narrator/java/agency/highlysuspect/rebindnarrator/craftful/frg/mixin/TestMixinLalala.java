package agency.highlysuspect.rebindnarrator.craftful.frg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public class TestMixinLalala {
	@Shadow
	private static ResourceLocation DEFAULT_FONT;
	
	@Shadow
	private RenderBuffers renderBuffers;
}
