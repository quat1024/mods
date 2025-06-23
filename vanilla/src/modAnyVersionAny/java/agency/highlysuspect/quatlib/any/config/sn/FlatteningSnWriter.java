package agency.highlysuspect.quatlib.any.config.sn;

import agency.highlysuspect.quatlib.any.util.SnocList;

import java.util.function.BiConsumer;

//For debugging
public class FlatteningSnWriter {
	public void accept(Sn<?> sn, BiConsumer<SnocList<String>, String> out) {
		accept(sn, SnocList.empty(), out);
	}
	
	public void accept(Sn<?> sn, SnocList<String> stack, BiConsumer<SnocList<String>, String> out) {
		switch(sn) {
			case SnList snList -> acceptList(snList, stack, out);
			case SnMap snMap -> acceptMap(snMap, stack, out);
			case SnStr snStr -> acceptStr(snStr, stack, out);
		}
	}
	
	public void acceptList(SnList list, SnocList<String> stack, BiConsumer<SnocList<String>, String> out) {
		for(int i = 0; i < list.size(); i++)
			accept(list.get(i), stack.snoc("[" + i + "]"), out);
	}
	
	public void acceptMap(SnMap map, SnocList<String> stack, BiConsumer<SnocList<String>, String> out) {
		map.forEach((k, v) -> accept(v, stack.snoc(k), out));
	}
	
	public void acceptStr(SnStr str, SnocList<String> stack, BiConsumer<SnocList<String>, String> out) {
		out.accept(stack, str.value());
	}
}
