package org.eclipseplugins.autoclosedebugperspective;

import java.util.Optional;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipseplugins.autoclosedebugperspective.preferences.PreferenceConstants;

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

			ILaunchesListener2 launchesListener = null;
			PluginSettings settings = loadPluginSettings();
			switch (settings.getPerspectiveSwitchTrigger()) {
			case OnAnyLaunchTerminated:
				launchesListener = new DebugPerspectiveCloserOnAnyTermination(perspectiveUtil, log);
				break;
			case OnAllLaunchesTerminated:
				// fall through
			default:
				launchesListener = new DebugPerspectiveCloserOnAllTerminated(launchmanager, perspectiveUtil, log);
				break;
			}
			launchmanager.addLaunchListener(launchesListener);
		} catch (Exception e) {
			if (log != null)
				log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "error at initialization", e));
			else
				System.out.println("error initializing plugin, log is null, ex: " + e.toString());
		}
	}

	private PluginSettings loadPluginSettings() {
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		PluginSettings s = new PluginSettings();
		s.setPerspectiveSwitchTrigger(determinePerspectiveSwitchTrigger(prefStore));
		return s;
	}

	private PerspectiveSwitchTrigger determinePerspectiveSwitchTrigger(IPreferenceStore prefStore) {
		int i = prefStore.getInt(PreferenceConstants.PERSPECTIVE_SWITCH_TRIGGER_CHOICE);
		return intToOptionalEnum(PerspectiveSwitchTrigger.class, i).orElseGet(() -> {
			return intToOptionalEnum(PerspectiveSwitchTrigger.class,
					prefStore.getDefaultInt(PreferenceConstants.PERSPECTIVE_SWITCH_TRIGGER_CHOICE))
							.orElse(PerspectiveSwitchTrigger.OnAllLaunchesTerminated);
		});
	}

	private <T extends Enum<T>> Optional<T> intToOptionalEnum(Class<T> enumClass, int i) {
		if (i < 0)
			return Optional.empty();
		T[] enumConstants = enumClass.getEnumConstants();
		if (i > enumConstants.length - 1)
			return Optional.empty();
		else
			return Optional.of(enumConstants[i]);
	}

}
