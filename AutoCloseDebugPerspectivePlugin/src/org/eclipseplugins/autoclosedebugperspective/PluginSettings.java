package org.eclipseplugins.autoclosedebugperspective;

enum PerspectiveSwitchMode {
	Previous, PreviousWithFallback, Fallback
}

class PluginSettings {

	private String fallbackPerspectiveId = "";
	private PerspectiveSwitchMode perspectiveSwitchMode = PerspectiveSwitchMode.Previous;

	public PerspectiveSwitchMode getPerspectiveSwitchMode() {
		return perspectiveSwitchMode;
	}

	public void setPerspectiveSwitchMode(PerspectiveSwitchMode perspectiveSwitchMode) {
		this.perspectiveSwitchMode = perspectiveSwitchMode;
	}

	public String getFallbackPerspectiveId() {
		return fallbackPerspectiveId;
	}

	public void setFallbackPerspectiveId(String fallbackPerspectiveId) {
		this.fallbackPerspectiveId = fallbackPerspectiveId;
	}
}