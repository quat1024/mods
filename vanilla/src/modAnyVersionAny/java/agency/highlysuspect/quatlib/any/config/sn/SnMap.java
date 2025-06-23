package agency.highlysuspect.quatlib.any.config.sn;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SnMap extends LinkedHashMap<String, Sn<?>> implements Sn<SnMap> {
	public SnMap() {}
	
	public SnMap(Map<? extends String, ? extends Sn<?>> m) {
		super(m);
	}
	
	@Override
	public SnMap copy() {
		Map<String, Sn<?>> children2 = new SnMap();
		forEach((key, val) -> children2.put(key, val.copy()));
		return new SnMap(children2);
	}
	
	@Override
	public <E extends Throwable> void accept(SnVisitor<E> visitor) throws E {
		visitor.openMap(this);
		for(Map.Entry<String, Sn<?>> entry : entrySet()) {
			visitor.mapItem(entry.getKey(), entry.getValue());
		}
		visitor.closeMap(this);
	}
}
