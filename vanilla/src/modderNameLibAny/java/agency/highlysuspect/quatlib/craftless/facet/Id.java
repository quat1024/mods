package agency.highlysuspect.quatlib.craftless.facet;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class Id implements Comparable<Id> {
	public final String ns, path;
	
	public Id(String ns, String path) {
		this.ns = Objects.requireNonNull(ns, () -> "null namespace; path is " + path);
		this.path = Objects.requireNonNull(path, () -> "null path; namespace is " + ns);
	}
	
	public static Id minecraft(String path) {
		return new Id("minecraft", path);
	}
	
	public String ns() {
		return ns;
	}
	
	public String path() {
		return path;
	}
	
	public Id mapPath(UnaryOperator<String> op) {
		return new Id(ns, op.apply(path));
	}
	
	public Id prefixFolders(@NotNull String prefix) {
		Objects.requireNonNull(prefix, () -> "null prefix passed to prefixPath on " + this);
		return new Id(ns, prefix.endsWith("/") ? prefix + path : prefix + "/" + path);
	}
	
	public Id formatPath(Object... args) {
		return new Id(ns, String.format(path, args));
	}
	
	public String toLangKey(@NotNull String domain) {
		return domain + "." + ns + "." + path.replace('/', '.');
	}
	
	@Override
	public String toString() {
		return ns + ":" + path;
	}
	
	public String toStringOmitMinecraft() {
		return ns.equals("minecraft") ? path : toString();
	}
	
	@Override
	public int compareTo(@NotNull Id other) {
		int x = ns.compareTo(other.ns);
		if(x != 0) return x;
		else return path.compareTo(other.path);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Id id = (Id) o;
		return ns.equals(id.ns) && path.equals(id.path);
	}
	
	@Override
	public int hashCode() {
		return 31 * ns.hashCode() + path.hashCode();
	}
}
