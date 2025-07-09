package agency.highlysuspect.quatlib.any.failure;

import agency.highlysuspect.quatlib.any.util.LogFacade;

import java.util.List;

public class LogFacadeReportFormatter implements ReportFormatter {
	public LogFacadeReportFormatter(LogFacade log) {
		this.log = log;
	}
	
	private final LogFacade log;
	
	@Override
	public void report(Throwable throwable) {
		if(throwable instanceof Report report) {
			try {
				showReport(report);
			} catch (Throwable notGood) {
				log.warn("An error occured attempting to report an error!!!", notGood);
				log.warn("Here is the error I was trying to report: ", report);
			}
		} else log.warn("{}", throwable);
	}
	
	private void showReport(Report report) {
		//messages
		List<String> messageStack = report.getMessageStack();
		log.warn("Error:");
		for(int i = messageStack.size() - 1; i >= 0; i--) {
			log.warn(" - {}", messageStack.get(i));
		}
		
		//sections
		if(!report.getSections().isEmpty()) {
			log.warn(""); //tidy blank line
			for(Section section : report.getSections()) {
				log.warn("{}:", section.header);
				for(String bodyLine : section.body) {
					log.warn("  {}", bodyLine);
				}
			}
		}
		
		//root cause
		Throwable rootCause = report.getRootCause();
		if(rootCause != null) {
			log.warn("");
			log.warn("Root cause: ", rootCause);
		}
		
		//suppressed
		int i = 1;
		for(Throwable suppressed : report.getSuppressed()) {
			log.warn("");
			log.warn("Suppressed exception {}:", i, suppressed);
		}
	}
}
