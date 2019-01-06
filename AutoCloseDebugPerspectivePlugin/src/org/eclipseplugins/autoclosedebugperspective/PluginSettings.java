package org.eclipseplugins.autoclosedebugperspective;

enum PerspectiveSwitchTrigger {
	OnAllLaunchesTerminated, OnAnyLaunchTerminated
}

class PluginSettings {

	private PerspectiveSwitchTrigger perspectiveSwitchTrigger = PerspectiveSwitchTrigger.OnAllLaunchesTerminated;

	public PerspectiveSwitchTrigger getPerspectiveSwitchTrigger() {
		return perspectiveSwitchTrigger;
	}

	public void setPerspectiveSwitchTrigger(PerspectiveSwitchTrigger perspectiveSwitchMode) {
		this.perspectiveSwitchTrigger = perspectiveSwitchMode;
	}
	
	@Override
	public String toString() {
		return "PluginSettings [perspectiveSwitchTrigger=" + perspectiveSwitchTrigger + "]";
	}

}