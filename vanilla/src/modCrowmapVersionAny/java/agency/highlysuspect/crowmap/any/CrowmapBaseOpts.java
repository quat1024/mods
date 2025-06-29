package agency.highlysuspect.crowmap.any;

import agency.highlysuspect.quatlib.any.config.ConfigOpt;
import agency.highlysuspect.quatlib.any.config.ConfigSection;

public class CrowmapBaseOpts {
	public static ConfigOpt<Boolean> SHOW_TOOLTIP = new ConfigOpt.BoolOpt(
		"Show Tooltip", true,
		"Show a tooltip explaining Crowmap is installed?"
	);
	
	public static ConfigOpt<Boolean> TOOLTIP_SELF_DESTRUCT = new ConfigOpt.BoolOpt(
		"Tooltip Self Destructs", true,
		"If 'true', the 'Hold Shift' indication will disappear after",
		"you look at the tooltip once."
	);
	
	public static ConfigSection visit(ConfigSection in) {
		in.subsection("Tooltip").add(SHOW_TOOLTIP, TOOLTIP_SELF_DESTRUCT);
		return in;
	}
}
