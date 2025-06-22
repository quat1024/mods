package agency.highlysuspect.quatlib.any.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An immutable linked-list basically, where appending creates a new list but shares structure with the old one
 */
public class SnocList<T> extends AbstractCollection<T> {
	public SnocList(SnocList<T> head, T tail) {
		this.head = head;
		this.tail = tail;
		this.sizeCache = head.sizeCache + 1;
		this.hashCache = (head.hashCache * 31) ^ tail.hashCode();
	}
	
	public SnocList() {
		this.head = null;
		this.tail = null;
		this.sizeCache = 0;
		this.hashCache = 0;
	}
	
	private static final SnocList<?> EMPTY = new SnocList<>();
	@SuppressWarnings("unchecked")
	public static <T> SnocList<T> empty() {
		return (SnocList<T>) EMPTY;
	}
	
	public SnocList<T> snoc(T elem) {
		return new SnocList<>(this, elem);
	}
	
	final SnocList<T> head;
	final T tail;
	final int sizeCache;
	final int hashCache;
	
	@Override
	public int size() {
		return sizeCache;
	}
	
	public SnocList<T> head() {
		return head;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ForwardSnocerator<>(this);
	}
	
	public Iterator<T> revIterator() {
		return new ReverseSnocerator<>(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || getClass() != o.getClass()) return false;
		
		SnocList<?> other = (SnocList<?>) o;
		//same length?
		if(other.sizeCache != sizeCache) return false;
		//all empty lists are the same
		if(sizeCache == 0) return true;
		//simple hash check
		if(other.hashCache != hashCache) return false;
		
		//walk each list in lockstep
		SnocList<?> self = this;
		while(!self.isEmpty()) {
			Object myTail = self.tail;
			Object itTail = other.tail;
			if(!Objects.equals(myTail, itTail)) return false;
			self = self.head;
			other = other.head;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return hashCache;
	}
	
	@Override
	public String toString() {
		//ugh
		List<String> stringified = new ArrayList<>(sizeCache);
		for(T thing : this) stringified.add(thing.toString());
		return String.join(".", stringified);
	}
	
	static class ReverseSnocerator<T> implements Iterator<T> {
		public ReverseSnocerator(SnocList<T> list) {
			this.cursor = list;
		}
		
		SnocList<T> cursor;
		
		@Override
		public boolean hasNext() {
			return !cursor.isEmpty();
		}
		
		@Override
		public T next() {
			T thing = cursor.tail;
			cursor = cursor.head;
			return thing;
		}
	}
	
	static class ForwardSnocerator<T> implements Iterator<T> {
		public ForwardSnocerator(SnocList<T> end) {
			this.end = end;
			this.steps = end.sizeCache - 1;
		}
		
		final SnocList<T> end;
		int steps;
		
		@Override
		public boolean hasNext() {
			return steps > -1;
		}
		
		@Override
		public T next() {
			SnocList<T> s = end;
			for(int i = 0; i < steps; i++) s = s.head;
			steps--;
			return s.tail;
		}
	}
}
