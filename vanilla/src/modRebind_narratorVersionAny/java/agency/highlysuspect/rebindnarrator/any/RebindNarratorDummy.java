package agency.highlysuspect.rebindnarrator.any;

public class RebindNarratorDummy implements RebindNarrator {
	@Override
	public boolean isCorrectKey(int glfwKeyToken) {
		return false;
	}
	
	@Override
	public boolean correctModifiersPressed() {
		return false;
	}
}
