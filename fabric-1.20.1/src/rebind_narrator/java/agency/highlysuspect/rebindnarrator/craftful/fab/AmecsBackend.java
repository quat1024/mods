package agency.highlysuspect.rebindnarrator.craftful.fab;

import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AmecsBackend implements RebindNarratorFabric.Backend {
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
			for(KeyMapping key : RebindNarratorFabric.inst().nmukShim.selfAndAlternates(NARRATOR_KEY)) {
				int boundKeyToken = KeyBindingHelper.getBoundKeyOf(key).getValue();
				if(boundKeyToken == -1 || glfwKeyToken != boundKeyToken) continue;
				
				if(KeyBindingUtils.getBoundModifiers(key).isPressed())
					return true;
			}
			return false;
		}
		
		@Override
		public boolean correctModifiersPressed() {
			//With AMECS + NMUK there can be different sets of modifiers for each alternate key
			//so i check them in the above loop
			return true;
		}
	}
}
