package org.eclipseplugins.autoclosedebugperspective;

import java.util.Optional;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Tracks the currently active perspective and the previously active perspective
 */
public class PerspectiveTracker implements IPerspectiveListener {

	private final ILog log;
	private IPerspectiveDescriptor currentPerspective;
	private Optional<IPerspectiveDescriptor> previousPerspective = Optional.empty();

	public PerspectiveTracker(ILog log, IPerspectiveDescriptor currentPerspective) {
		super();
		this.log = log;
		this.currentPerspective = currentPerspective;
	}

	public IPerspectiveDescriptor getCurrentPerspective() {
		return currentPerspective;
	}

	public Optional<IPerspectiveDescriptor> getPreviousPerspective() {
		return previousPerspective;
	}

	@Override
	public void perspectiveActivated(IWorkbenchPage wbp, IPerspectiveDescriptor activatedPerspective) {
		previousPerspective = Optional.of(currentPerspective);
		currentPerspective = activatedPerspective;

		logPerspectiveActivated(activatedPerspective);
	}

	private void logPerspectiveActivated(IPerspectiveDescriptor activatedPerspective) {
		try {
			String activatedPerspectiveId = activatedPerspective != null ? activatedPerspective.getId() : "null";
			String previousPerspectiveId = previousPerspective.map(x->x.getId()).orElse("null");
			String msg = "perspective activated: '" + activatedPerspectiveId + "' from previous perspective: '"
					+ previousPerspectiveId + "'";
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID, msg));
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage arg0, IPerspectiveDescriptor arg1, String arg2) {
	}
}