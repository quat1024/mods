package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.util.SnocList;

import java.util.function.BiConsumer;

public class FlatteningSnVisitor implements SnVisitor<RuntimeException> {
	public FlatteningSnVisitor(BiConsumer<SnocList<String>, String> action, SnocList<String> path) {
		this.action = action;
		this.path = path;
	}
	
	public FlatteningSnVisitor(BiConsumer<SnocList<String>, String> action) {
		this(action, SnocList.empty());
	}
	
	final BiConsumer<SnocList<String>, String> action;
	final protected SnocList<String> path;
	
	@Override
	public void visitString(String s) {
		action.accept(path, s);
	}
	
	@Override
	public void mapItem(String k, Sn<?> item) {
		new FlatteningSnVisitor(action, path.snoc(k)).visit(item);
	}
	
	@Override
	public void listItem(int i, Sn<?> item) {
		new FlatteningSnVisitor(action, path.snoc(String.valueOf(i))).visit(item);
	}
	
	@Override
	public void openMap(SnMap map) {
	
	}
	
	@Override
	public void closeMap(SnMap map) {
	
	}
	
	@Override
	public void openList(SnList list) {
	
	}
	
	@Override
	public void closeList(SnList list) {
	
	}
}
