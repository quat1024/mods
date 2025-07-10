package agency.highlysuspect.quatlib.any.failure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextChain {
	public ContextChain(FailureBin bin, @Nullable ContextChain parent, @NotNull String message) {
		this.bin = bin;
		this.parent = parent;
		this.message = message;
	}
	
	public final FailureBin bin;
	public final @Nullable ContextChain parent;
	public final @NotNull String message;
	private List<Section> sections = null;
	
	/// adding more detail
	
	public ContextChain detail(String childMessage) {
		return new ContextChain(bin, this, childMessage);
	}
	
	/// section management? ///
	
	public ContextChain addSection(Section s) {
		if(sections == null) sections = new ArrayList<>(2);
		sections.add(s);
		return this;
	}
	
	public ContextChain addSection(String header, String... body) {
		return addSection(new Section(header, body));
	}
	
	public List<Section> getSections() {
		if(sections == null) return List.of();
		else return sections;
	}
	
	/// reporting ///
	
	public void addWarning() {
		addWarning(null);
	}
	
	public void addError() {
		addError(null);
	}
	
	public Report2 addErrorWithException() {
		return addErrorWithException(null);
	}
	
	public void addWarning(@Nullable Throwable cause) {
		bin.addWarning(this, cause);
	}
	
	public void addError(@Nullable Throwable cause) {
		bin.addError(this, cause);
	}
	
	public Report2 addErrorWithException(@Nullable Throwable cause) {
		addError(cause);
		return new Report2(this, cause);
	}
	
	public List<String> collectMessages() {
		return collectMessages(new ArrayList<>());
	}
	
	protected List<String> collectMessages(List<String> in) {
		in.add(message);
		if(parent != null) parent.collectMessages(in);
		return in;
	}
	
	public List<Section> collectSections() {
		return collectSections(new ArrayList<>());
	}
	
	protected List<Section> collectSections(List<Section> in) {
		in.addAll(getSections());
		if(parent != null) parent.collectSections(in);
		return in;
	}
}
