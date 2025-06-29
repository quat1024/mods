package agency.highlysuspect.quatlib.any.failure;

import java.util.Arrays;
import java.util.List;

public class Section {
	public Section(String header, String... body) {
		this.header = header;
		this.body = Arrays.asList(body);
	}
	
	public final String header;
	public final List<String> body;
}
