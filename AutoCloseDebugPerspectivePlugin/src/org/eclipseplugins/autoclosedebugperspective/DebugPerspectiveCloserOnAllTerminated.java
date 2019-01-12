package org.eclipseplugins.autoclosedebugperspective;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipseplugins.autoclosedebugperspective.util.IPerspectiveUtil;
import org.eclipseplugins.autoclosedebugperspective.util.LaunchUtil;

public class DebugPerspectiveCloserOnAllTerminated implements ILaunchesListener2 {

	private ILaunchManager launchManager;
	private ILog log;
	private IPerspectiveUtil perspectiveUtil;
	private Optional<IPerspectiveDescriptor> perspectiveOnFirstLaunch = Optional.empty();

	public DebugPerspectiveCloserOnAllTerminated(ILaunchManager launchManager, IPerspectiveUtil perspectiveUtil, ILog log) {
		super();
		this.launchManager = launchManager;
		this.perspectiveUtil = perspectiveUtil;
		this.log = log;
	}

	@Override
	public void launchesAdded(ILaunch[] launchesAdded) {
		try {
			long addedActiveDebugLaunchesCount = 
					Arrays.stream(launchesAdded)
					.filter(l -> isNonTerminatedDebugLaunch(l))
					.count();

			if (addedActiveDebugLaunchesCount <= 0)
				return;
			
			long totalActiveDebugLaunchesCount = 
					Arrays.stream(launchManager.getLaunches())
					.filter(l -> isNonTerminatedDebugLaunch(l))
					.count();
			
			// launchesAdded have been already added and thus are returned in launchManager.getLaunches().
			// Therefore for the first launch the count of added launches is equal to the total count returned by getLaunches() (filtered by debug and not terminated).
			if (totalActiveDebugLaunchesCount == addedActiveDebugLaunchesCount) {
				IPerspectiveDescriptor p = perspectiveUtil.getCurrentPerspective();
				perspectiveOnFirstLaunch = Optional.of(p);
				log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "first launch from perspective" + p));
			} else {
				log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
						"not first launch: totalActiveDebugLaunchesCount:" + totalActiveDebugLaunchesCount
								+ ", addedActiveDebugLaunchesCount: " + addedActiveDebugLaunchesCount));
			}
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}

	@Override
	public void launchesTerminated(ILaunch[] launchesTerminated) {
		try {
			boolean hasAnyDebugLaunchTerminated = Arrays.stream(launchesTerminated).anyMatch(LaunchUtil::isDebugLaunch);
			if (!hasAnyDebugLaunchTerminated)
				return;
			
			ILaunch[] launches = launchManager.getLaunches();
			boolean haveAllDebugLaunchesTerminated = 
					Arrays.stream(launches)
					.filter(LaunchUtil::isDebugLaunch)
					.allMatch(ILaunch::isTerminated);
			
			if (haveAllDebugLaunchesTerminated) {
				perspectiveOnFirstLaunch.ifPresent(p -> {
					if (!perspectiveUtil.isDebugPerspective(p)) {
						log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "setting perspective " + p));
						perspectiveUtil.setPerspective(p);
					}
					perspectiveOnFirstLaunch = Optional.empty();
				});
			} else {
				log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "some debug launch(es) have terminated, but not all"));
			}
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}
	
	private boolean isNonTerminatedDebugLaunch(ILaunch l) {
		return LaunchUtil.isDebugLaunch(l) && !l.isTerminated();
	}

	@Override
	public void launchesChanged(ILaunch[] arg0) {
	}

	@Override
	public void launchesRemoved(ILaunch[] arg0) {
	}

}
