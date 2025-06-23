package agency.highlysuspect.quatlib.any.config.sn;

import org.jetbrains.annotations.NotNull;

public record SnStr(@NotNull String value) implements Sn<SnStr> {
	public static SnStr of(String value) {
		return new SnStr(value);
	}
	
	@Override
	public SnStr copy() {
		return SnStr.of(value);
	}
}
