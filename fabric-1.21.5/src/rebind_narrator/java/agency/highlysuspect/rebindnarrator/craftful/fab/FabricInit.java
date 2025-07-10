package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.rebindnarrator.craftful.VanillaNarratorKeyPredicate;
import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import agency.highlysuspect.rebindnarrator.craftless.RebindNarratorModBase;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class FabricInit extends RebindNarratorModBase implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		super.init();
		// ... mods like AMECS, no more useless keys ...
		//interesting: https://www.curseforge.com/minecraft/mc-mods/amecs-reborn
		
		KeyBindingHelper.registerKeyBinding(NARRATOR_KEY);
	}
	
	public final KeyMapping NARRATOR_KEY = new KeyMapping(
		"options.narrator",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		"key.categories.misc"
	);
	
	@Override
	public NarratorKeyPredicate makeKeyPredicate() {
		return new FabricNarratorKeyPredicate();
	}
	
	public class FabricNarratorKeyPredicate extends VanillaNarratorKeyPredicate {
		@Override
		public boolean isCorrectKey(int glfwKeyToken) {
			return glfwKeyToken == KeyBindingHelper.getBoundKeyOf(NARRATOR_KEY).getValue();
		}
	}
}
