package agency.highlysuspect.rebindnarrator.fab;

import agency.highlysuspect.rebindnarrator.ver.RebindNarratorVanilla1_21_5;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class BasicFabricImpl extends RebindNarratorVanilla1_21_5 {
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
