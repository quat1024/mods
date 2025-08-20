package agency.highlysuspect.quatlib.craftless.config;

import agency.highlysuspect.quatlib.craftless.config.sn.Sn;
import agency.highlysuspect.quatlib.craftless.config.sn.SnCodec;
import agency.highlysuspect.quatlib.craftless.config.sn.SnView;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.failure.CtxChain;
import agency.highlysuspect.quatlib.craftless.failure.ReportedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigOpt<T> implements SectOrOpt {
	public ConfigOpt(String name, T defaultValue, SnCodec<T> codec, List<String> comment) {
		this.name = name;
		this.comment = comment;
		this.defaultValue = defaultValue;
		this.codec = codec;
	}
	
	public ConfigOpt(String name, T defaultValue, SnCodec<T> codec, String... comment) {
		this(name, defaultValue, codec, Arrays.asList(comment));
	}
	
	private final String name;
	private final List<String> comment;
	private final T defaultValue;
	private final SnCodec<T> codec;
	
	private final List<Validator<T>> validators = new ArrayList<>(1);
	private final List<Corrector<T>> correctors = new ArrayList<>(1);
	
	/// SER AND DE
	
	public Sn<?> write(T thing) {
		return codec.write(thing);
	}
	
	public T parse(SnView sn, CtxChain ctx) throws ReportedException {
		return codec.parse(sn, ctx);
	}
	
	/// CORRECTION - fixing the value in an unambiguous way and reporting a warning
	
	public interface Corrector<T> {
		T correct(T thing, CtxChain ctx);
	}
	
	public T correct(T thing, CtxChain ctx) {
		T best = thing;
		for(Corrector<T> corrector : correctors)
			best = corrector.correct(best, ctx);
		return best;
	}
	
	public ConfigOpt<T> addCorrector(Corrector<T> c) {
		correctors.add(c);
		return this;
	}
	
	/// VALIDATION: reporting an error if the value is invalid
	
	public interface Validator<T> {
		void validate(T thing, CtxChain ctx) throws ReportedException;
	}
	
	public void validate(T thing, CtxChain ctx) throws ReportedException {
		for(Validator<T> validator : validators)
			validator.validate(thing, ctx);
	}
	
	public ConfigOpt<T> addValidator(Validator<T> v) {
		validators.add(v);
		return this;
	}
	
	/// YEAHG
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public List<String> getComment() {
		return comment;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public static class StringOpt extends ConfigOpt<String> {
		public StringOpt(String name, String defaultValue, String... comment) {
			super(name, defaultValue, SnCodec.STR, comment);
		}
	}
	
	public static class BoolOpt extends ConfigOpt<Boolean> {
		public BoolOpt(String name, Boolean defaultValue, String... comment) {
			super(name, defaultValue, SnCodec.BOOL, comment);
		}
	}
	
	public static class IntOpt extends ConfigOpt<Integer> {
		public IntOpt(String name, Integer defaultValue, String... comment) {
			super(name, defaultValue, SnCodec.INT, comment);
			
			addCorrector((thing, ctx) -> {
				if(thing < min) {
					if(min == 0) ctx.detail("Value " + thing + " cannot be negative").reportWarning();
					else ctx.detail("Value " + thing + " cannot be below " + min).reportWarning();
					return min;
				}
				
				if(thing > max) {
					ctx.detail("Value " + thing + " cannot be above " + max).reportWarning();
					return max;
				}
				
				return thing;
			});
			
			addValidator((thing, ctx) -> {
				if(min != Integer.MIN_VALUE && thing < min) {
					if(min == 0) throw ctx.detail("Value " + thing + " cannot be negative").reportError();
					else throw ctx.detail("Value " + thing + " cannot be below " + min).reportError();
				}
				
				if(max != Integer.MAX_VALUE && thing > max) {
					throw ctx.detail("Value " + thing + " cannot be above " + max).reportError();
				}
			});
		}
		
		private int min = Integer.MIN_VALUE;
		private int max = Integer.MAX_VALUE;
		
		public IntOpt setMin(int min) {
			this.min = min;
			return this;
		}
		
		public IntOpt setMax(int max) {
			this.max = max;
			return this;
		}
		
		@Override
		public List<String> getComment() {
			if(min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) return super.getComment();
			
			ArrayList<String> c = new ArrayList<>(super.getComment());
			if(min != Integer.MIN_VALUE) c.add("Must be at least " + min + ".");
			if(max != Integer.MAX_VALUE) c.add("Must be at most " + max + ".");
			return c;
		}
	}
	
	public static class IdOpt extends ConfigOpt<Id> {
		public IdOpt(String name, Id defaultValue, String... comment) {
			super(name, defaultValue, SnCodec.ID, comment);
		}
		
		private boolean ensureMinecraftParse = true;
		
		public IdOpt ensureMinecraftParse(boolean ensureMinecraftParse) {
			this.ensureMinecraftParse = ensureMinecraftParse;
			return this;
		}
		
		@Override
		public void validate(Id thing, CtxChain ctx) throws ReportedException {
			super.validate(thing, ctx);
			
			if(ensureMinecraftParse) {
				try {
					thing.toMinecraft();
				} catch (Exception e) {
					throw ctx.cause(e).detail("Failed to parse " + thing + " as a Minecraft ResourceLocation").reportError();
				}
			}
		}
	}
}
