package org.eclipseplugins.autoclosedebugperspective;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipseplugins.autoclosedebugperspective.util.IPerspectiveUtil;
import org.eclipseplugins.autoclosedebugperspective.util.LaunchUtil;

class DebugPerspectiveCloserOnAnyTermination implements ILaunchesListener2 {

	private HashMap<ILaunch, IPerspectiveDescriptor> perspectivesAtLaunch = new HashMap<ILaunch, IPerspectiveDescriptor>();
	private IPerspectiveUtil perspectiveUtil;
	private ILog log;

	public DebugPerspectiveCloserOnAnyTermination(IPerspectiveUtil perspectiveUtil, ILog log) {
		super();
		this.perspectiveUtil = perspectiveUtil;
		this.log = log;
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
		IPerspectiveDescriptor currentPerspective = perspectiveUtil.getCurrentPerspective();
		Arrays.stream(launches).filter(LaunchUtil::isDebugLaunch).forEach(launch -> {
			log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "launched " + launch));
			perspectivesAtLaunch.put(launch, currentPerspective);
		});
	}

	@Override
	public void launchesChanged(ILaunch[] arg0) {
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
		try {
			removeDebugLaunchesFromStore(launches);
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}

	private void removeDebugLaunchesFromStore(ILaunch[] launches) {
		Arrays.stream(launches).filter(LaunchUtil::isDebugLaunch).forEach(l -> perspectivesAtLaunch.remove(l));
	}

	@Override
	public void launchesTerminated(ILaunch[] launches) {
		try {
			log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "launchesTerminated"));

			// change the perspective if the current perspective is the Debug perspective
			IPerspectiveDescriptor currentPerspective = perspectiveUtil.getCurrentPerspective();
			if (!perspectiveUtil.isDebugPerspective(currentPerspective))
				return;

			for (int i = launches.length - 1; i >= 0; i--) {
				ILaunch launch = launches[i];

				if (LaunchUtil.isDebugLaunch(launch)) {
					IPerspectiveDescriptor perspectiveOnLaunchStart = perspectivesAtLaunch.getOrDefault(launch, null);
					if (!perspectiveUtil.isDebugPerspective(perspectiveOnLaunchStart)) {
						perspectiveUtil.setPerspective(perspectiveOnLaunchStart);
						log.log(new Status(IStatus.OK, Activator.PLUGIN_ID,
								"switched to perspective " + perspectiveOnLaunchStart));
						break;
					}
				}
			}

			removeDebugLaunchesFromStore(launches);
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}

}