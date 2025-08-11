package agency.highlysuspect.quatlib.craftless;

import agency.highlysuspect.quatlib.craftless.util.LogFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogFacade implements LogFacade {
	public Slf4jLogFacade(Logger logger) {
		this.logger = logger;
	}
	
	public Slf4jLogFacade(String name) {
		this(LoggerFactory.getLogger(name));
	}
	
	public final Logger logger;
	
	@Override
	public void info(String pattern, Object... args) {
		logger.info(pattern, args);
	}
	
	@Override
	public void warn(String pattern, Object... args) {
		logger.warn(pattern, args);
	}
	
	@Override
	public void error(String pattern, Object... args) {
		logger.error(pattern, args);
	}
}
