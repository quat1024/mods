package agency.highlysuspect.quatlib.craftless.failure;

public class ReportedException extends Exception {
	//the only way to make a ReportedException is by reporting an error with a CtxChain,
	//which means everything is already reported. no need to include information in the exception
	protected ReportedException() {
		super();
	}
	
	//for filling out catch blocks
	public static void fake() throws ReportedException {}
}
