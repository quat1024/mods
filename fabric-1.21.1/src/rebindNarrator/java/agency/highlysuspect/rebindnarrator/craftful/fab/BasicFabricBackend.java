package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class BasicFabricBackend implements RebindNarratorFabric.Backend {
	public final KeyMapping NARRATOR_KEY = new KeyMapping(
		"options.narrator",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		"key.categories.misc"
	);
	
	@Override
	public void registerKeybinding() {
		KeyBindingHelper.registerKeyBinding(NARRATOR_KEY);
	}
	
	@Override
	public NarratorKeyPredicate makeKeyPredicate() {
		return new NarratorKeyPredicate() {
			@Override
			public boolean isCorrectKey(int glfwKeyToken) {
				int boundKeyToken = KeyBindingHelper.getBoundKeyOf(NARRATOR_KEY).getValue();
				return boundKeyToken != -1 && glfwKeyToken == boundKeyToken;
			}
			
			@Override
			public boolean correctModifiersPressed() {
				if(RebindNarratorFabric.inst().config.get(RebindNarratorFabric.CTRL)) return Screen.hasControlDown();
				else return true;
			}
		};
	}
}
