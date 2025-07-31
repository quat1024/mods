package agency.highlysuspect.quatlib.craftless.config.sn;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SnList extends ArrayList<Sn<?>> implements Sn<SnList> {
	public SnList() {}
	
	public SnList(@NotNull Collection<? extends Sn<?>> c) {
		super(c);
	}
	
	@Override
	public SnList copy() {
		List<Sn<?>> children2 = new SnList(this);
		for(Sn<?> elem : this) children2.add(elem.copy());
		return new SnList(children2);
	}
}
