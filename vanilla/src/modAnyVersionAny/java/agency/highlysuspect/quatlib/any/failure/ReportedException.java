package agency.highlysuspect.quatlib.any.failure;

public class ReportedException extends Exception {
	//the only way to make a ReportedException is by putting an error in a FailureBucket.
	//which means everything is already reported. no need to include information in the exception
	protected ReportedException() {
		super();
	}
	
	//for filling out catch blocks
	public static void fake() throws ReportedException {}
}
