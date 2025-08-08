package agency.highlysuspect.crowmap.craftless;

import agency.highlysuspect.quatlib.craftless.config.ReadableConfig;
import org.jetbrains.annotations.NotNull;

public class TooltipStateMachine {
	private int state;
	private long byeMillis;
	
	//displaying the "hold shift" teaser tooltip
	private static final int HOLD_SHIFT_PROMPT = 0;
	
	//holding shift, displaying "map updates when not being held..."
	private static final int INFO = 1;
	
	//released shift, "this tooltip will now self destruct!"
	private static final int SELF_DESTRUCT = 2;
	
	//after self-destruct, waiting to press shift again
	private static final int HIDDEN = 3;
	
	public enum Action {
		SHOW_HOLD_SHIFT_PROMPT,
		SHOW_INFO,
		SHOW_SELF_DESTRUCT_MESSAGE,
		NOTHING
		;
	}
	
	public @NotNull Action tick(boolean shifting) {
		ReadableConfig config = CrowmapBase.inst().config;
		
		for(int i = 0; i < 3; i++) {
			switch(state) {
				case HOLD_SHIFT_PROMPT:
					//displaying "hold shift" and shift is pressed: transition to INFO
					if(shifting) {
						state = INFO;
						continue;
					}
					return Action.SHOW_HOLD_SHIFT_PROMPT;
				
				case INFO:
					//just released shift: if self-destruct is turned on, transition
					//to the self-destruct message, otherwise go back to the "hold
					//shift" prompt
					if(!shifting) {
						if(config.get(CrowmapBaseOpts.TOOLTIP_SELF_DESTRUCT)) {
							state = SELF_DESTRUCT;
							byeMillis = System.currentTimeMillis();
						} else state = HOLD_SHIFT_PROMPT;
						continue;
					}
					return Action.SHOW_INFO;
				
				case SELF_DESTRUCT:
					if(shifting) {
						//re-show message
						state = INFO;
						continue;
					} else if (System.currentTimeMillis() - byeMillis > 5000) {
						//showed the self-destruct message for 5s
						state = HIDDEN;
						continue;
					} else {
						return Action.SHOW_SELF_DESTRUCT_MESSAGE;
					}
				
				case HIDDEN:
					//re-show tooltip if self-destruct option is turned back off
					if(!config.get(CrowmapBaseOpts.TOOLTIP_SELF_DESTRUCT)) {
						state = HOLD_SHIFT_PROMPT;
						continue;
					}
					//just show the info on shift
					return shifting ? Action.SHOW_INFO : Action.NOTHING;
			}
		}
		return Action.NOTHING; //unreachable
	}
}
