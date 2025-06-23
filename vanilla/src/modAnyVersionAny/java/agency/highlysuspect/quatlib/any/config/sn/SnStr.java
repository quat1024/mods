package agency.highlysuspect.quatlib.any.config.sn;

import org.jetbrains.annotations.NotNull;

public record SnStr(@NotNull String value) implements Sn<SnStr> {
	public static SnStr of(String value) {
		return new SnStr(value);
	}
	
	@Override
	public SnStr copy() {
		return this; //immutable
	}
	
	@Override
	public <E extends Throwable> void accept(SnVisitor<E> visitor) throws E {
		visitor.visitString(value);
	}
}
