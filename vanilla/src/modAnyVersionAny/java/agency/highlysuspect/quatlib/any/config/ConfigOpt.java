package agency.highlysuspect.quatlib.any.config;

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
	public abstract String write(T thing);
	public abstract T parse(String s) throws ConfigException.OptionParseException;
	
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
	public void validate(T thing) throws ConfigException.ValidationException {
		for(Validator<T> validator : validators)
			validator.validate(thing);
	}
	
	public ConfigOpt<T> addValidator(Validator<T> v) {
		validators.add(v);
		return this;
	}
	
	public interface Validator<T> {
		void validate(T thing) throws ConfigException.ValidationException;
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
	
	@Override
	public <E extends Throwable> void accept(ConfigVisitor<E> visitor) throws E {
		visitor.visitOpt(this);
	}
	
	public static class StringOpt extends ConfigOpt<String> {
		public StringOpt(String name, String defaultValue, String... comment) {
			super(name, defaultValue, comment);
		}
		
		@Override
		public String write(String thing) {
			return thing;
		}
		
		@Override
		public String parse(String s) {
			return s;
		}
	}
	
	public static class BoolOpt extends ConfigOpt<Boolean> {
		public BoolOpt(String name, Boolean defaultValue, String... comment) {
			super(name, defaultValue, comment);
		}
		
		@Override
		public String write(Boolean thing) {
			return Boolean.toString(thing);
		}
		
		@Override
		public Boolean parse(String s) throws ConfigException.OptionParseException {
			s = s.toLowerCase(Locale.ROOT).trim();
			return switch(s) {
				case "true" -> true;
				case "false" -> false;
				case null, default -> throw new ConfigException.OptionParseException("Expected 'true' or 'false', but got " + s, this);
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
		public void validate(Integer thing) throws ConfigException.ValidationException {
			super.validate(thing);
			
			if(min != Integer.MIN_VALUE && thing < min) {
				if(min == 0) throw new ConfigException.ValidationException("Value " + thing + " cannot be negative", this);
				else throw new ConfigException.ValidationException("Value " + thing + " cannot be below " + min, this);
			}
			
			if(max != Integer.MAX_VALUE && thing > max) {
				throw new ConfigException.ValidationException("Value " + thing + " cannot be above " + max, this);
			}
		}
		
		@Override
		public String write(Integer thing) {
			return Integer.toString(thing);
		}
		
		@Override
		public Integer parse(String s) throws ConfigException.OptionParseException {
			try {
				return Integer.parseInt(s.trim());
			} catch (NumberFormatException e) {
				throw new ConfigException.OptionParseException("Expected an integer, but got " + s, this, e);
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
