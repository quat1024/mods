package agency.highlysuspect.quatlib.any.config.sn;

public interface SnVisitor<E extends Throwable> {
	void visitString(String s) throws E;
	
	void openMap(SnMap map) throws E;
	void closeMap(SnMap map) throws E;
	void mapItem(String k, Sn<?> item) throws E;
	
	void openList(SnList list) throws E;
	void closeList(SnList list) throws E;
	void listItem(int i, Sn<?> item) throws E;
	
	default void visit(Sn<?> sn) throws E {
		sn.accept(this);
	}
}
