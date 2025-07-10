package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.rebindnarrator.craftful.RebindNarratorVanilla1_21_1;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class BasicFabricImpl extends RebindNarratorVanilla1_21_1 {
	public final KeyMapping NARRATOR_KEY = new KeyMapping(
		"options.narrator",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		"key.categories.misc"
	);
	
	void registerKeyMapping() {
		KeyBindingHelper.registerKeyBinding(NARRATOR_KEY);
	}
	
	@Override
	public boolean isCorrectKey(int glfwKeyToken) {
		return glfwKeyToken == KeyBindingHelper.getBoundKeyOf(NARRATOR_KEY).getValue();
	}
}
