package agency.highlysuspect.quatlib.craftless.failure;

import agency.highlysuspect.quatlib.craftless.util.LogFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class FailureRoot implements FailureListener {
	public FailureRoot(String name) {
		this.name = name;
	}
	
	private final List<FailureListener> listeners = new ArrayList<>(2);
	private final ReentrantLock lock = new ReentrantLock();
	private final String name;
	
	@Override
	public void reportWarning(CtxChain warning) {
		lock.lock();
		try {
			if(listeners.isEmpty()) new FailureLogger(LogFacade.Sysout.INSTANCE).reportWarning(warning);
			else for(FailureListener listener : listeners) listener.reportWarning(warning);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void reportError(CtxChain error) {
		lock.lock();
		try {
			if(listeners.isEmpty()) new FailureLogger(LogFacade.Sysout.INSTANCE).reportError(error);
			else for(FailureListener listener : listeners) listener.reportError(error);
		} finally {
			lock.unlock();
		}
	}
	
	public FailureRoot addListener(FailureListener listener) {
		lock.lock();
		try {
			listeners.add(listener);
		} finally {
			lock.unlock();
		}
		return this;
	}
	
	public CtxChain context() {
		return new CtxChain.StringLink(null, this, name);
	}
}
