package org.eclipseplugins.autoclosedebugperspective;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class Startup implements IStartup {

	private ILog log;

	// Will be called in a separate thread after the workbench initializes.
	@Override
	public void earlyStartup() {
		try {
			log = Activator.getDefault().getLog();
			log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "initializing plugin: " + Activator.PLUGIN_ID));

			DebugPlugin debugPlugin = DebugPlugin.getDefault();
			final IWorkbench workbench = PlatformUI.getWorkbench();
			ILaunchManager launchmanager = debugPlugin.getLaunchManager();
			IPerspectiveUtil perspectiveUtil = new PerspectiveUtil(workbench, log);
			DebugPerspectiveCloserOnAnyTermination pm = new DebugPerspectiveCloserOnAnyTermination(perspectiveUtil, log);
			DebugPerspectiveCloserOnAllTerminated p2 = new DebugPerspectiveCloserOnAllTerminated(launchmanager, perspectiveUtil, log);
			launchmanager.addLaunchListener(pm);
		} catch (Exception e) {
			if (log != null)
				log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "error at initialization", e));
			else
				System.out.println("error initializing plugin, log is null, ex: " + e.toString());
		}
	}

	private PluginSettings loadPluginSettings() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		PluginSettings s = new PluginSettings();
		s.setFallbackPerspectiveId(prefs.get("fallbackPerspectiveId", ""));
		PerspectiveSwitchMode mode = (PerspectiveSwitchMode.values()[prefs.getInt("perspectiveSwitchMode", 1)]);
		s.setPerspectiveSwitchMode(mode);
		return s;
	}

}
