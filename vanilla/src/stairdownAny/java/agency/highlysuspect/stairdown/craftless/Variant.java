package agency.highlysuspect.stairdown.craftless;

import agency.highlysuspect.quatlib.craftless.config.sn.Sn;
import agency.highlysuspect.quatlib.craftless.config.sn.SnCodec;
import agency.highlysuspect.quatlib.craftless.config.sn.SnMap;
import agency.highlysuspect.quatlib.craftless.config.sn.SnView;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") //idgaf
public class Variant {
	//Base block id used by generated blocks
	public String idPrefix;
	
	//If present, the item to craft this variant out of
	public Optional<Id> craftedFrom = Optional.empty();
	
	public boolean stair = true;
	public boolean slab = true;
	public boolean wall = true;
	
	public static final SnCodec<Variant> SN_CODEC = new SnCodec<>() {
		@Override
		public Sn<?> write(Variant thing) {
			SnMap map = new SnMap();
			
			map.put("idPrefix", Sn.str(thing.idPrefix));
			thing.craftedFrom.ifPresent(id -> map.put("craftedFrom", SnCodec.ID.write(id)));
			map.put("stair", Sn.str(String.valueOf(thing.stair)));
			map.put("slab", Sn.str(String.valueOf(thing.slab)));
			map.put("wall", Sn.str(String.valueOf(thing.wall)));
			
			return map;
		}
		
		@Override
		public Variant parse(SnView sn, CtxChain ctx) throws ReportedException {
			Variant v = new Variant();
			SnView.MapView map = sn.asMap();
			
			v.idPrefix = map.get("idPrefix").asString();
			if(map.containsKey("craftedFrom")) v.craftedFrom = Optional.of(SnCodec.ID.parse(map.get("craftedFrom"), ctx));
			
			//programming is my passion
			if(map.containsKey("stair")) v.stair = SnCodec.BOOL.parse(map.get("stair"), ctx);
			else v.stair = true;
			if(map.containsKey("slab")) v.slab = SnCodec.BOOL.parse(map.get("slab"), ctx);
			else v.slab = true;
			if(map.containsKey("wall")) v.wall = SnCodec.BOOL.parse(map.get("wall"), ctx);
			else v.wall = true;
			
			return v;
		}
	};
}
