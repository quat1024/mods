package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.failure.ContextChain;
import agency.highlysuspect.quatlib.any.failure.Report;
import agency.highlysuspect.quatlib.any.failure.Report2;

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
	
	//serialization and deserialization
	public abstract Sn<?> write(T thing);
	public abstract T parse(SnView sn, ContextChain ctx) throws Report, Report2;
	
	//validation - report an error & return false if the value breaks some constraint
	public interface Validator<T> {
		boolean validate(T thing, ContextChain ctx);
	}
	
	//correction - report a warning & nudge the value if there's a way to make it adhere to constraints in an unambiguous way
	public interface Corrector<T> {
		T correct(T thing, ContextChain ctx);
	}
	
	public boolean validate(T thing, ContextChain ctx) {
		boolean ok = true;
		for(Validator<T> validator : validators)
			ok &= validator.validate(thing, ctx);
		return ok;
	}
	
	public T correct(T thing, ContextChain ctx) {
		T best = thing;
		for(Corrector<T> corrector : correctors)
			best = corrector.correct(best, ctx);
		return best;
	}
	
	public ConfigOpt<T> addValidator(Validator<T> v) {
		validators.add(v);
		return this;
	}
	
	public ConfigOpt<T> addCorrector(Corrector<T> c) {
		correctors.add(c);
		return this;
	}
	
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
		public String parse(SnView sn, ContextChain ctx) throws Report {
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
		public Boolean parse(SnView sn, ContextChain ctx) throws Report, Report2 {
			String s = sn.asString().toLowerCase(Locale.ROOT).trim();
			return switch(s) {
				case "true" -> true;
				case "false" -> false;
				default -> throw ctx.detail("Expected 'true' or 'false', but got '" + s + "'.").addErrorWithException();
			};
		}
	}
	
	public static class IntOpt extends ConfigOpt<Integer> {
		public IntOpt(String name, Integer defaultValue, String... comment) {
			super(name, defaultValue, comment);
			
			addCorrector((thing, ctx) -> {
				if(thing < min) {
					if(min == 0) ctx.detail("Value " + thing + " cannot be negative. Correcting.");
					else ctx.detail("Value " + thing + " is smaller than the minimum of " + min + ". Correcting.");
					return min;
				}
				
				if(thing > max) {
					ctx.detail("Value " + thing + " is larger than the maximum of " + max + ". Correcting.");
					return max;
				}
				
				return thing;
			});
			
			addValidator((thing, ctx) -> {
				if(min != Integer.MIN_VALUE && thing < min) {
					if(min == 0) ctx.detail("Value " + thing + " cannot be negative.").addError();
					else ctx.detail("Value " + thing + " cannot be below " + min + ".").addError();
					return false;
				}
				
				if(max != Integer.MAX_VALUE && thing > max) {
					ctx.detail("Value " + thing + " cannot be above " + max + ".").addError();
					return false;
				}
				
				return true;
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
		public Integer parse(SnView sn, ContextChain ctx) throws Report, Report2 {
			String s = sn.asString();
			
			try {
				return Integer.parseInt(s.trim());
			} catch (Throwable e) {
				throw ctx.detail("Could not parse '" + s + "' as an integer").addErrorWithException(e);
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
