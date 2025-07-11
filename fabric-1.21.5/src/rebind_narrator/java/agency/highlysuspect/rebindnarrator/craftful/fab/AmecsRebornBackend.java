package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.Amecs;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;

public class AmecsRebornBackend implements RebindNarratorFabric.Backend {
	public final AmecsKeyBinding NARRATOR_KEY = new AmecsKeyBinding(
		"options.narrator",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		"key.categories.misc",
		new KeyModifiers().setControl(true)
	);
	
	@Override
	public void registerKeybinding() {
		KeyBindingHelper.registerKeyBinding(NARRATOR_KEY);
	}
	
	@Override
	public NarratorKeyPredicate makeKeyPredicate() {
		return new AmecsRebornFabricNarratorKeyPredicate();
	}
	
	public class AmecsRebornFabricNarratorKeyPredicate implements NarratorKeyPredicate {
		@Override
		public boolean isCorrectKey(int glfwKeyToken) {
			int boundKeyToken = KeyBindingHelper.getBoundKeyOf(NARRATOR_KEY).getValue();
			return boundKeyToken != -1 && glfwKeyToken == boundKeyToken;
		}
		
		@Override
		public boolean correctModifiersPressed() {
			return Amecs.CURRENT_MODIFIERS.equals(KeyBindingUtils.getBoundModifiers(NARRATOR_KEY));
		}
	}
}
