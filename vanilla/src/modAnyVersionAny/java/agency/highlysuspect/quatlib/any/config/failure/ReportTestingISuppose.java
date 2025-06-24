package agency.highlysuspect.quatlib.any.config.failure;

public class ReportTestingISuppose {
	public void frobnicate() {
		throw new Report("Failed to frobnicate", new AhShitIFuckedUpRealBadException());
	}
	
	public void configureDoohicky(String filename) {
		try {
			frobnicate();
		} catch (Throwable e) {
			throw Report.modify(e, it -> it
				.addMessage("While attempting to florple '" + filename + "'")
				.addNoteSection("Does the file '" + filename + "' exist?")
			);
		}
	}
	
	public void reticulateSplines(String filename, int spline) {
		try {
			configureDoohicky(filename);
		} catch (Throwable e) {
			throw Report.modify(e, it -> it
				.addMessage("While reticulating " + spline + " splines")
			);
		}
	}
	
	public static void main(String[] args) {
		try {
			new ReportTestingISuppose().reticulateSplines("tootoo.txt", 64);
		} catch (Throwable e) {
			new ConsoleReportFormatter().report(e);
		}
	}
	
	public static class AhShitIFuckedUpRealBadException extends RuntimeException {
		public AhShitIFuckedUpRealBadException() {
			super("osiudiusdiuauidudsuidsd");
		}
	}
}
