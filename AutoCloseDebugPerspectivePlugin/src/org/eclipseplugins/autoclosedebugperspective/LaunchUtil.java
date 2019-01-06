package org.eclipseplugins.autoclosedebugperspective;

import java.util.Objects;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;

class LaunchUtil {
	public static boolean isDebugLaunch(ILaunch launch) {
		return launch != null && Objects.equals(launch.getLaunchMode(), ILaunchManager.DEBUG_MODE);
	}
}