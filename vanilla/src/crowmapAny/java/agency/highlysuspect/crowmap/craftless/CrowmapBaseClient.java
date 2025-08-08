package agency.highlysuspect.crowmap.craftless;

public class CrowmapBaseClient {
	public CrowmapBaseClient() {
		INST = this;
	}
	
	public final TooltipStateMachine tooltipStateMachine = new TooltipStateMachine();
	
	protected static CrowmapBaseClient INST;
	public static CrowmapBaseClient inst() {
		return INST;
	}
}
