package agency.highlysuspect.quatlib.any.config;

public interface ConfigVisitor<E extends Throwable> {
	void openSection(ConfigSection section) throws E;
	void closeSection(ConfigSection section) throws E;
	<T> void visitOpt(ConfigOpt<T> opt) throws E;
}
