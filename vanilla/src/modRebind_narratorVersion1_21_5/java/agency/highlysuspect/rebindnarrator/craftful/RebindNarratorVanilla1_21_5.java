package agency.highlysuspect.rebindnarrator.craftful;

import agency.highlysuspect.rebindnarrator.craftless.RebindNarrator;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class RebindNarratorVanilla1_21_5 implements RebindNarrator {
	@Override
	public boolean isCorrectKey(int glfwKeyToken) {
		return glfwKeyToken == GLFW.GLFW_KEY_B;
	}
	
	@Override
	public boolean correctModifiersPressed() {
		return Screen.hasControlDown();
	}
}
