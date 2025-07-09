package agency.highlysuspect.quatlib.any.failure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Report extends Exception {
	public Report(String message, Throwable cause) {
		super(cause);
		this.messageStack.add(message);
	}
	
	public Report(String message) {
		this(message, null);
	}
	
	public Report(Throwable cause) {
		this(fallbackMessage(cause), cause);
	}
	
	//add additional context to an error
	public static Report modify(Throwable t, Consumer<ModifiableReport> action) {
		if(t instanceof Report report) return report.modify(action);
		else {
			return new Report(fallbackMessage(t), t).modify(action);
		}
	}
	
	public static Report withMessage(Throwable t, String message) {
		return Report.modify(t, it -> it.addMessage(message));
	}
	
	//Throwables can have a null message, this is an attempt to pick something sensible if there is no message
	//Somewhat defensive...
	private static @NotNull String fallbackMessage(Throwable t) {
		String message = t.getMessage();
		if(message == null) {
			StackTraceElement[] stack = t.getStackTrace();
			if(stack.length > 0) {
				StackTraceElement tip = stack[0];
				
				String tipSimpleName = tip.getClassName();
				int dot = tipSimpleName.lastIndexOf('.');
				if(dot != -1 && !tipSimpleName.endsWith(".")) tipSimpleName = tipSimpleName.substring(dot + 1);
				
				message = "An unnamed " + t.getClass().getSimpleName() + " thrown by " + tipSimpleName + "." + tip.getMethodName();
			}
		}
		
		if(message == null) {
			message = "An unnamed " + t.getClass().getSimpleName();
		}
		
		return message;
	}
	
	private final List<String> messageStack = new ArrayList<>();
	private final List<Section> sections = new ArrayList<>();
	
	//modifications to the report are guarded by this other type
	//it's given to you in modify(), where i wrap your code in a trycatch
	private final ModifiableReport handle = new ModifiableReport() {
		@Override
		public ModifiableReport addMessage(String message) {
			messageStack.add(message);
			return handle;
		}
		
		@Override
		public ModifiableReport addSection(String category, String... body) {
			sections.add(new Section(category, body));
			return handle;
		}
	};
	
	protected Report modify(Consumer<ModifiableReport> action) {
		try {
			action.accept(handle);
		} catch (Throwable e) {
			try {
				sections.add(new Section("Warning", "An exception occured when attempting to add more context to this error report:", e.getMessage()));
				addSuppressed(e);
			} catch (Throwable doubleFault) {
				Error ahShit = new Error("Double-fault when attempting to add context to an error report, this shouldn't ever happen!!", doubleFault);
				ahShit.addSuppressed(this);
				throw ahShit;
			}
		}
		return this;
	}
	
	@Override
	public String getMessage() {
		return String.join(", ", messageStack);
	}
	
	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}
	
	//TODO: should these climb getCause and look for more Reports?
	// thinking about stuff like, "a Report caused by a RuntimeException caused by a Report"
	
	public List<String> getMessageStack() {
		return messageStack;
	}
	
	public List<Section> getSections() {
		return sections;
	}
	
	public @Nullable Throwable getRootCause() {
		Throwable t = this;
		while(t instanceof Report) t = t.getCause();
		return t;
	}
	
	public void logToSystemError() {
		new ConsoleReportFormatter(System.err).report(this);
	}
	
	public void logTo(ReportFormatter formatter) {
		formatter.report(this);
	}
	
	public interface ModifiableReport {
		ModifiableReport addMessage(String message);
		ModifiableReport addSection(String category, String... body);
		
		default ModifiableReport addNoteSection(String... note) {
			return addSection("Note", note);
		}
	}
}
