package agency.highlysuspect.quatlib.any.failure;

import agency.highlysuspect.quatlib.any.util.LogFacade;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//this type of report comes from a ContextChain, which passes information *downwards*
public class Report2 extends Exception {
	public Report2(ContextChain context, @Nullable Throwable cause) {
		super(String.join(", ", context.collectMessages()), cause);
		this.context = context;
		this.sections = context.collectSections();
	}
	
	public Report2(ContextChain context) {
		this(context, null);
	}
	
	public final ContextChain context;
	public final List<Section> sections;
	
	public interface Report2Formatter {
		void report(ReportType type, Report2 report);
		
		enum ReportType {
			ERROR, WARNING
		}
	}
	
	public static class LogFacadeReport2Formatter implements Report2Formatter {
		public LogFacadeReport2Formatter(LogFacade log) {
			this.log = log;
		}
		
		private final LogFacade log;
		
		@Override
		public void report(ReportType type, Report2 report) {
			//context
			log.warn("{}", switch(type) {
				case ERROR -> "Error:";
				case WARNING -> "Warning:";
			});
			for(String message : report.context.collectMessages())
				log.warn(" - {}", message);
			
			//sections
			List<Section> sections = report.context.collectSections();
			if(!sections.isEmpty()) {
				log.warn("");
				for(Section section : sections) {
					log.warn("{}:", section.header);
					for(String line : section.body) {
						log.warn("  {}", line);
					}
				}
			}
			
			//root cause
			Throwable rootCause = report.getCause();
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
}
