package org.eclipseplugins.autoclosedebugperspective;

import java.util.function.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.e4.ui.di.UISynchronize;

// TODOs 
// what if started in debug perspective? configurable default perspective?
// testing
// package
// eclipse marketplace

public class Startup implements IStartup {

	private ILog log;

	// Will be called in a separate thread after the workbench initializes.
	@Override
	public void earlyStartup() {
		try {
			log = Activator.getDefault().getLog();
			log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "initializing plugin: " + Activator.PLUGIN_ID));

			DebugPlugin debugPlugin = DebugPlugin.getDefault();
			DebugEventListener debugEventListener = new DebugEventListener(log);
			debugPlugin.addDebugEventListener(debugEventListener);

			final IWorkbench workbench = PlatformUI.getWorkbench();
			// workbench methods must be called in the UI thread since they may access SWT
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
//						savePluginSettings();
						PluginSettings settings = loadPluginSettings();
						IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
						if (window != null) {
							IWorkbenchPage activePage = window.getActivePage();
							IPerspectiveDescriptor currentPerspective = activePage.getPerspective();
							PerspectiveTracker perspectiveTracker = new PerspectiveTracker(log, currentPerspective);
							window.addPerspectiveListener(perspectiveTracker);
							
							UISynchronize ui = (UISynchronize) workbench.getService(UISynchronize.class);
							Consumer<IPerspectiveDescriptor> perspectiveSetter = (p) -> {
								// re-get active objects, because i don't know if they could have changed
								ui.asyncExec(() -> {
									try {
										workbench.getActiveWorkbenchWindow().getActivePage().setPerspective(p);
									} catch (Exception e) {
										log.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
												"error setting perspective", e));
									}
								});
							};
							IPerspectiveRegistry perspectiveRegistry = workbench.getPerspectiveRegistry();
							debugEventListener.addActionListener(new DebugEventHandler(log, perspectiveTracker,
									perspectiveSetter, perspectiveRegistry, settings));

							log.log(new Status(Status.INFO, Activator.PLUGIN_ID, "init finished successfully"));
						} else {
							log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "active workbench window is null"));
						}
					} catch (Exception e) {
						log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "error at initialization", e));
					}
				}
			});
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
