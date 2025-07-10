package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.CtxChain;
import agency.highlysuspect.quatlib.any.failure.ReportedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class ConfigOpt<T> implements SectOrOpt {
	public ConfigOpt(String name, T defaultValue, List<String> comment) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.comment = comment;
	}
	
	public ConfigOpt(String name, T defaultValue, String... comment) {
		this(name, defaultValue, Arrays.asList(comment));
	}
	
	private final String name;
	private final List<String> comment;
	private final T defaultValue;
	
	private final List<Validator<T>> validators = new ArrayList<>(1);
	private final List<Corrector<T>> correctors = new ArrayList<>(1);
	
	/// SER AND DE
	
	public abstract Sn<?> write(T thing);
	public abstract T parse(SnView sn, CtxChain ctx) throws ReportedException;
	
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
			super(name, defaultValue, comment);
		}
		
		@Override
		public Sn<?> write(String thing) {
			return Sn.str(thing);
		}
		
		@Override
		public String parse(SnView sn, CtxChain ctx) throws ReportedException {
			return sn.asString();
		}
	}
	
	public static class BoolOpt extends ConfigOpt<Boolean> {
		public BoolOpt(String name, Boolean defaultValue, String... comment) {
			super(name, defaultValue, comment);
		}
		
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
				case null, default -> throw ctx.detail("Expected 'true' or 'false' but got '" + s + "'").reportError();
			};
		}
	}
	
	public static class IntOpt extends ConfigOpt<Integer> {
		public IntOpt(String name, Integer defaultValue, String... comment) {
			super(name, defaultValue, comment);
			
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
		
		@Override
		public List<String> getComment() {
			if(min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) return super.getComment();
			
			ArrayList<String> c = new ArrayList<>(super.getComment());
			if(min != Integer.MIN_VALUE) c.add("Must be at least " + min + ".");
			if(max != Integer.MAX_VALUE) c.add("Must be at most " + max + ".");
			return c;
		}
	}
}
