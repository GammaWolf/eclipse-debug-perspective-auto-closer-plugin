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
			ILaunch[] launches = launchManager.getLaunches();
			long nonTerminatedLaunchesCount = Arrays.stream(launches).filter(l -> !l.isTerminated()).count();
			long nonTerminatedAddedLaunchesCount = Arrays.stream(launchesAdded).filter(l -> !l.isTerminated()).count();
			if (nonTerminatedLaunchesCount == nonTerminatedAddedLaunchesCount) {
				IPerspectiveDescriptor p = perspectiveUtil.getCurrentPerspective();
				perspectiveOnFirstLaunch = Optional.of(p);
				log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "first launch from perspective" + p));
			} else {
				log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
						"not first launch: nonTerminatedLaunchesCount:" + nonTerminatedLaunchesCount
								+ ", nonTerminatedAddedLaunchesCount: " + nonTerminatedAddedLaunchesCount));
			}
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}

	@Override
	public void launchesTerminated(ILaunch[] launchesTerminated) {
		try {
			ILaunch[] launches = launchManager.getLaunches();
			if (Arrays.stream(launches).allMatch(l -> l.isTerminated())) {
				perspectiveOnFirstLaunch.ifPresent(p -> {
					if (!perspectiveUtil.isDebugPerspective(p)) {
						log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "setting perspective " + p));
						perspectiveUtil.setPerspective(p);
					}
					perspectiveOnFirstLaunch = Optional.empty();
				});
			}
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}

	@Override
	public void launchesChanged(ILaunch[] arg0) {
	}

	@Override
	public void launchesRemoved(ILaunch[] arg0) {
	}

}
