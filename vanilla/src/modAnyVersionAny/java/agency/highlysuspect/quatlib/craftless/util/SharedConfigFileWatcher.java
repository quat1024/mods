package agency.highlysuspect.quatlib.craftless.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedConfigFileWatcher {
	static final WatchService WATCHER;
	static final Object lock = new Object();
	static final Set<Path> watchedDirectories = new HashSet<>();
	static final Map<String, Runnable> changeActionsByFilename = new HashMap<>();
	
	static Thread watcherThread;
	static LogFacade log = LogFacade.Sysout.INSTANCE;
	
	public static void setLog(LogFacade newLog) {
		synchronized(lock) {
			log = newLog;
		}
	}
	
	public static void watch(Path path, Runnable onChangeAction) {
		synchronized(lock) {
			//if it fails to initialize
			if(WATCHER == null) {
				log.warn("Can't watch {} for changes (my WatchService is null)", path);
				return;
			}
			
			//you can only register directories with the watch service, not files
			Path dir = path.getParent();
			if(dir == null) throw new IllegalArgumentException("can't watch root of filesystem, path " + path);
			if(!watchedDirectories.contains(dir)) {
				try {
					dir.register(WATCHER, StandardWatchEventKinds.ENTRY_MODIFY);
				} catch (Exception e) {
					log.warn("Failed to register {} to WatchService", dir, e);
					return;
				}
				watchedDirectories.add(dir);
			}
			
			String filename = path.getFileName().toString();
			if(changeActionsByFilename.containsKey(filename)) {
				log.warn("Already watching {}", path);
			} else {
				changeActionsByFilename.put(filename, onChangeAction);
				log.info("Watching {} for changes", path);
			}
			
			if(watcherThread == null) {
				watcherThread = new Thread(SharedConfigFileWatcher::watchJob, "ModderNameLib Config Watcher");
				watcherThread.setPriority(Thread.MIN_PRIORITY); //be polite
				watcherThread.setDaemon(true); //Don't block JVM shutdown
				watcherThread.start();
			}
		}
	}
	
	//in the watcher thread
	private static void watchJob() {
		try {
			while(!Thread.interrupted()) {
				//block until there's a new event in this directory
				WatchKey key = WATCHER.take();
				if(!key.isValid()) continue;
				
				//look through the events
				for(WatchEvent<?> event : key.pollEvents()) {
					//did it modify a file, not something else
					if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && event.context() instanceof Path path) {
						//and do we care about this file
						Runnable action = changeActionsByFilename.get(path.getFileName().toString());
						if(action != null)
							action.run(); //yes we do
					}
				}
				
				//all done
				key.reset();
			}
		} catch (Throwable e) {
			log.warn("ModderNameLib filewatcher thread crashed", e);
			//and exit stage left
		}
	}
	
	static {
		WatchService theWatcher = null;
		try {
			theWatcher = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			log.warn("ModderNameLib failed to make WatchService", e);
		}
		
		WATCHER = theWatcher;
	}
}