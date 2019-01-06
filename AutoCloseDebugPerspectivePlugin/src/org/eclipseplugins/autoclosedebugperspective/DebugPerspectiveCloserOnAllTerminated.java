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

	private ILaunchManager lm;
	private ILog log;
	private IPerspectiveUtil perspectiveUtil;
	private Optional<IPerspectiveDescriptor> perspectiveOnFirstLaunch = Optional.empty();

	public DebugPerspectiveCloserOnAllTerminated(ILaunchManager lm, IPerspectiveUtil perspectiveUtil, ILog log) {
		super();
		this.lm = lm;
		this.perspectiveUtil = perspectiveUtil;
		this.log = log;
	}

	@Override
	public void launchesAdded(ILaunch[] launchesAdded) {
		ILaunch[] launches = lm.getLaunches();
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
	}

	@Override
	public void launchesTerminated(ILaunch[] launchesTerminated) {
		ILaunch[] launches = lm.getLaunches();
		if (Arrays.stream(launches).allMatch(l -> l.isTerminated())) {
			perspectiveOnFirstLaunch.ifPresent(p -> {
				if (!perspectiveUtil.isDebugPerspective(p)) {
					log.log(new Status(IStatus.OK, Activator.PLUGIN_ID, "setting perspective " + p));
					perspectiveUtil.setPerspective(p);
				}
				perspectiveOnFirstLaunch = Optional.empty();
			});
		}
	}

	@Override
	public void launchesChanged(ILaunch[] arg0) {
	}

	@Override
	public void launchesRemoved(ILaunch[] arg0) {
	}

}
