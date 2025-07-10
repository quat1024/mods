package agency.highlysuspect.quatlib.any.failure;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FailureBin {
	List<Report2> warnings = new ArrayList<>(4);
	List<Report2> errors = new ArrayList<>(4);
	
	public ContextChain detail(String initialMessage) {
		return new ContextChain(this, null, initialMessage);
	}
	
	public void addWarning(ContextChain context, @Nullable Throwable cause) {
		warnings.add(new Report2(context, cause));
	}
	
	public void addError(ContextChain context, @Nullable Throwable cause) {
		errors.add(new Report2(context, cause));
	}
	
	public void addWarning(Report2 failure) {
		warnings.add(failure);
	}
	
	public void addError(Report2 failure) {
		errors.add(failure);
	}
	
	public void reportWarnings(Report2.Report2Formatter formatter) {
		for(Report2 warning : warnings) formatter.report(Report2.Report2Formatter.ReportType.WARNING, warning);
	}
	
	public void reportErrors(Report2.Report2Formatter formatter) {
		for(Report2 error : errors) formatter.report(Report2.Report2Formatter.ReportType.ERROR, error);
	}
	
	public boolean noErrors() {
		return errors.isEmpty();
	}
}
