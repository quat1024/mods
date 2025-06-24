package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.failure.Report;
import agency.highlysuspect.quatlib.any.config.sn.Sn;
import agency.highlysuspect.quatlib.any.config.sn.SnView;
import agency.highlysuspect.quatlib.any.util.QuatUtil;
import org.jetbrains.annotations.Nullable;

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
	public abstract T parse(SnView sn) throws Report;
	
	//correction
	public T correct(T thing) {
		T best = thing;
		for(Corrector<T> corrector : correctors) {
			@Nullable T corrected = corrector.correct(best);
			if(corrected != null) best = corrected;
		}
		return best;
	}
	
	public ConfigOpt<T> addCorrector(Corrector<T> c) {
		correctors.add(c);
		return this;
	}
	
	//todo is this the right shape
	// feels like you'd want some notification that a config value was corrected
	public interface Corrector<T> {
		@Nullable T correct(T thing);
	}
	
	//validation
	public void validate(T thing) throws Report {
		for(Validator<T> validator : validators)
			validator.validate(thing);
	}
	
	public ConfigOpt<T> addValidator(Validator<T> v) {
		validators.add(v);
		return this;
	}
	
	public interface Validator<T> {
		void validate(T thing) throws Report;
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
		public String parse(SnView sn) throws Report {
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
		public Boolean parse(SnView sn) throws Report {
			String s = sn.asString().toLowerCase(Locale.ROOT).trim();
			return switch(s) {
				case "true" -> true;
				case "false" -> false;
				case null, default -> throw new Report("Expected 'true' or 'false', but got " + s);
			};
		}
	}
	
	public static class IntOpt extends ConfigOpt<Integer> {
		public IntOpt(String name, Integer defaultValue, String... comment) {
			super(name, defaultValue, comment);
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
		public Integer correct(Integer thing) {
			return QuatUtil.clamp(super.correct(thing), min, max);
		}
		
		@Override
		public void validate(Integer thing) throws Report {
			super.validate(thing);
			
			if(min != Integer.MIN_VALUE && thing < min) {
				if(min == 0) throw new Report("Value " + thing + " cannot be negative");
				else throw new Report("Value " + thing + " cannot be below " + min);
			}
			
			if(max != Integer.MAX_VALUE && thing > max) {
				throw new Report("Value " + thing + " cannot be above " + max);
			}
		}
		
		@Override
		public Sn<?> write(Integer thing) {
			return Sn.str(Integer.toString(thing));
		}
		
		@Override
		public Integer parse(SnView sn) throws Report {
			String s = sn.asString();
			
			try {
				return Integer.parseInt(s.trim());
			} catch (Throwable e) {
				throw new Report("Could not parse '" + s + "' as an integer", e);
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
