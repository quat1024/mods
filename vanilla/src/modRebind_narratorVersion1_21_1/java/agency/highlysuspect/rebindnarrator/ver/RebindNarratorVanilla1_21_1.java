package agency.highlysuspect.rebindnarrator.ver;

import agency.highlysuspect.rebindnarrator.any.RebindNarrator;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class RebindNarratorVanilla1_21_1 implements RebindNarrator {
	@Override
	public boolean isCorrectKey(int glfwKeyToken) {
		return glfwKeyToken == GLFW.GLFW_KEY_B;
	}
	
	@Override
	public boolean correctModifiersPressed() {
		return Screen.hasControlDown();
	}
}
