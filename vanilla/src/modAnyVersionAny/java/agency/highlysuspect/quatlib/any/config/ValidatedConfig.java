package agency.highlysuspect.quatlib.any.config;

import agency.highlysuspect.quatlib.any.config.failure.Report;
import agency.highlysuspect.quatlib.any.config.sn.SnView;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValidatedConfig implements ReadableConfig {
	private final LinkedHashMap<ConfigOpt<?>, Object> validOptions = new LinkedHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigOpt<? extends T> opt) {
		T obj = (T) validOptions.get(opt);
		return obj == null ? opt.getDefaultValue() : obj;
	}
	
	public ValidatedConfig parseAndValidate(MatchedUnparsedConfig matched) throws Report {
		for(Map.Entry<ConfigOpt<?>, SnView> e : matched.entrySet()) {
			ConfigOpt<?> opt = e.getKey();
			SnView sn = e.getValue();
			if(sn == null) continue;
			
			validOptions.put(opt, parseAndValidateImpl(opt, sn));
		}
		return this;
	}
	
	//just need to name the generic
	private <T> T parseAndValidateImpl(ConfigOpt<T> opt, SnView view) throws Report {
		//TODO: catch exceptions and add them to a warnings list, then ignore the failing option
		try {
			T parsed = opt.parse(view);
			T corrected = opt.correct(parsed);
			opt.validate(corrected);
			return corrected;
		} catch (Throwable e) {
			throw Report.modify(e, it -> it.addMessage("Problem while parsing option '" + view.path() + "'"));
		}
	}
	
	//for debugging pretty much
	@Override
	public String toString() {
		StringBuilder bob = new StringBuilder();
		validOptions.forEach((key, val) ->
			bob.append(key.getName()).append(" -> ").append(val).append('\n'));
		return bob.toString();
	}
}
