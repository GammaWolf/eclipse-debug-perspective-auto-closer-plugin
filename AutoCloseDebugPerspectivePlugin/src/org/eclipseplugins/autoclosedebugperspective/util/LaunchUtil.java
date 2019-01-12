package org.eclipseplugins.autoclosedebugperspective.util;

import java.util.Objects;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;

public class LaunchUtil {
	public static boolean isDebugLaunch(ILaunch launch) {
		return launch != null && Objects.equals(launch.getLaunchMode(), ILaunchManager.DEBUG_MODE);
	}
}