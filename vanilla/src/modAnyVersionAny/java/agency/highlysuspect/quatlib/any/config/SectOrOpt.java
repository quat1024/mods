package agency.highlysuspect.quatlib.any.config;

import java.util.List;

public interface SectOrOpt {
	String getName();
	List<String> getComment();
	
	<E extends Throwable> void accept(ConfigVisitor<E> visitor) throws E;
}
