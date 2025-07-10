package agency.highlysuspect.rebindnarrator.craftless;

public interface RebindNarrator {
	/**
	 * GLFW sometimes calls these "key tokens", https://www.glfw.org/docs/3.3/input_guide.html#input_key .
	 * I believe this mapping originated in GLFW and is otherwise non-standard. They are not key codes or key scancodes.
	 *
	 * @param glfwKeyToken the token of the currently pressed key
	 * @return whether that key should toggle the narrator
	 */
	boolean isCorrectKey(int glfwKeyToken);
	
	/**
	 * @return whether the correct modifier keys are being pressed.
	 */
	boolean correctModifiersPressed();
}
