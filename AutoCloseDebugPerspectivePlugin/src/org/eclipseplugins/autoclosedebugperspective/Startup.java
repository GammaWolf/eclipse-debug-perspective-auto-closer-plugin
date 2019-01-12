package org.eclipseplugins.autoclosedebugperspective;

import java.util.Objects;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipseplugins.autoclosedebugperspective.preferences.PreferenceConstants;
import org.eclipseplugins.autoclosedebugperspective.util.EnumUtil;
import org.eclipseplugins.autoclosedebugperspective.util.IPerspectiveUtil;
import org.eclipseplugins.autoclosedebugperspective.util.PerspectiveUtil;

public class Startup implements IStartup {

	private ILog log;
	ILaunchesListener2 launchesListener = null;

	// Will be called in a separate thread after the workbench initializes.
	@Override
	public void earlyStartup() {
		try {
			log = Activator.getDefault().getLog();
			log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "initializing plugin: " + Activator.PLUGIN_ID));

			DebugPlugin debugPlugin = DebugPlugin.getDefault();
			IWorkbench workbench = PlatformUI.getWorkbench();
			ILaunchManager launchmanager = debugPlugin.getLaunchManager();
			IPerspectiveUtil perspectiveUtil = new PerspectiveUtil(workbench, log);
			launchesListener = createLaunchesListener(launchmanager, perspectiveUtil);
			launchmanager.addLaunchListener(launchesListener);

			// listen and react to preference changes
			IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
			prefStore.addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent pce) {
					try {
						String changedProperty = pce.getProperty();
						log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "settings changed, property: " + changedProperty + ", to value: " + pce.getNewValue()));
						if (Objects.equals(changedProperty, PreferenceConstants.PERSPECTIVE_SWITCH_TRIGGER_CHOICE)) {
							if (launchesListener != null)
								launchmanager.removeLaunchListener(launchesListener);
							launchesListener = createLaunchesListener(launchmanager, perspectiveUtil);
							launchmanager.addLaunchListener(launchesListener);
						} else {
							log.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "setting property not known by settings change listener"));
						}
					} catch (Exception e) {
						log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
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

	private ILaunchesListener2 createLaunchesListener(ILaunchManager launchmanager, IPerspectiveUtil perspectiveUtil) {
		ILaunchesListener2 launchesListener = null;
		PluginSettings settings = loadPluginSettings();
		log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "loaded settings " + settings));
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
		return launchesListener;
	}

	private PluginSettings loadPluginSettings() {
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		PluginSettings s = new PluginSettings();
		s.setPerspectiveSwitchTrigger(determinePerspectiveSwitchTrigger(prefStore));
		return s;
	}

	private PerspectiveSwitchTrigger determinePerspectiveSwitchTrigger(IPreferenceStore prefStore) {
		int i = prefStore.getInt(PreferenceConstants.PERSPECTIVE_SWITCH_TRIGGER_CHOICE);
		return EnumUtil.intToOptionalEnum(PerspectiveSwitchTrigger.class, i).orElseGet(() -> {
			// fall back to default
			int defaultIndex = prefStore.getDefaultInt(PreferenceConstants.PERSPECTIVE_SWITCH_TRIGGER_CHOICE);
			return EnumUtil.intToOptionalEnum(PerspectiveSwitchTrigger.class, defaultIndex)
					.orElse(PerspectiveSwitchTrigger.OnAllLaunchesTerminated);
		});
	}

}
