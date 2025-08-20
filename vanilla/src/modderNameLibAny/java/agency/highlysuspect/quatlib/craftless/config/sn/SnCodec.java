package agency.highlysuspect.quatlib.craftless.config.sn;

import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//I have become the very thing i sought to destroy
//For the time being validators/correctors still live in ConfigOpt
public interface SnCodec<T> {
	Sn<?> write(T thing);
	T parse(SnView sn, CtxChain ctx) throws ReportedException;
	
	static <T> SnCodec<List<T>> listOf(SnCodec<T> c) {
		return new SnCodec<List<T>>() {
			@Override
			public Sn<?> write(List<T> thingList) {
				SnList list = new SnList();
				for(T thing : thingList) list.add(c.write(thing));
				return list;
			}
			
			@Override
			public List<T> parse(SnView sn, CtxChain ctx) throws ReportedException {
				SnView.ListView list = sn.asList();
				List<T> r = new ArrayList<>(list.size());
				for(int i = 0; i < list.size(); i++) r.add(c.parse(list.get(i), ctx));
				return r;
			}
		};
	}
	
	SnCodec<String> STR = new SnCodec<>() {
		@Override
		public Sn<?> write(String thing) {
			return Sn.str(thing);
		}
		
		@Override
		public String parse(SnView sn, CtxChain ctx) throws ReportedException {
			return sn.asString();
		}
	};
	
	SnCodec<Boolean> BOOL = new SnCodec<>() {
		@Override
		public Sn<?> write(Boolean thing) {
			return Sn.str(Boolean.toString(thing));
		}
		
		@Override
		public Boolean parse(SnView sn, CtxChain ctx) throws ReportedException {
			String s = sn.asString().toLowerCase(Locale.ROOT).trim();
			return switch(s) {
				case "true" -> true;
				case "false" -> false;
				default -> throw ctx.detail("Expected 'true' or 'false' but got '" + s + "'").reportError();
			};
		}
	};
	
	SnCodec<Integer> INT = new SnCodec<>() {
		@Override
		public Sn<?> write(Integer thing) {
			return Sn.str(Integer.toString(thing));
		}
		
		@Override
		public Integer parse(SnView sn, CtxChain ctx) throws ReportedException {
			String s = sn.asString();
			try {
				return Integer.parseInt(s.trim());
			} catch (Throwable e) {
				throw ctx.cause(e).detail("Could not parse '" + s + "' as an integer").reportError();
			}
		}
	};
	
	SnCodec<Id> ID = new SnCodec<>() {
		@Override
		public Sn<?> write(Id thing) {
			return Sn.str(thing.toString());
		}
		
		@Override
		public Id parse(SnView sn, CtxChain ctx) throws ReportedException {
			return Id.parse(sn.asString());
		}
	};
}
