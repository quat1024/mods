package agency.highlysuspect.quatlib.any.config.failure;

import java.io.PrintStream;
import java.util.List;

public class ConsoleReportFormatter implements ReportFormatter {
	public ConsoleReportFormatter(PrintStream out) {
		this.out = out;
	}
	
	public ConsoleReportFormatter() {
		this(System.err);
	}
	
	private final PrintStream out;
	
	public void report(Throwable throwable) {
		if(throwable instanceof Report report) {
			try {
				showReport(report);
			} catch (Throwable dgaf) {
				//Well shit that shouldn't happen
				out.println();
				out.println("An error occured when attempting to report an error!!!");
				dgaf.printStackTrace(out);
				
				out.println();
				out.println("Here is the error I was going to report:");
				report.printStackTrace(out);
			}
		}
		else throwable.printStackTrace(out);
	}
	
	private void showReport(Report report) {
		List<String> messageStack = report.getMessageStack();
		if(messageStack.isEmpty()) {
			out.println("Error:");
		} else {
			out.println("Error: " + messageStack.get(0));
			for(int i = 0; i < messageStack.size(); i++) {
				out.print("  ");
				out.print(i + 1);
				out.print(": ");
				out.println(messageStack.get(i));
			}
		}
		
		//sections
		if(!report.getSections().isEmpty()) {
			out.println();
			for(Section section : report.getSections()) {
				out.println(section.header + ":");
				for(String bodyLine : section.body) {
					out.println("  " + bodyLine);
				}
			}
		}
		
		Throwable rootCause = report.getRootCause();
		if(rootCause != null) {
			out.println();
			out.println("Root cause:");
			rootCause.printStackTrace(out);
		}
		
		int i = 1;
		for(Throwable suppressed : report.getSuppressed()) {
			out.println();
			out.println("Suppressed exception " + i + ":");
			suppressed.printStackTrace(out);
		}
	}
}
