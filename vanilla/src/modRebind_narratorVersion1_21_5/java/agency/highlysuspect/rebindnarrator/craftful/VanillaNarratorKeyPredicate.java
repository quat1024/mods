package agency.highlysuspect.rebindnarrator.craftful;

import agency.highlysuspect.rebindnarrator.craftless.NarratorKeyPredicate;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class VanillaNarratorKeyPredicate implements NarratorKeyPredicate {
	@Override
	public boolean isCorrectKey(int glfwKeyToken) {
		return glfwKeyToken == GLFW.GLFW_KEY_B;
	}
	
	@Override
	public boolean correctModifiersPressed() {
		return Screen.hasControlDown();
	}
}
