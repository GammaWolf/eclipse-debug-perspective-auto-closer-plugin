package org.eclipseplugins.autoclosedebugperspective;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;

final class DebugEventHandler implements ActionListener {
	private final ILog log;
	private final PerspectiveTracker perspectiveTracker;
	private final Consumer<IPerspectiveDescriptor> perspectiveSetter;
	private final PluginSettings settings;
	private final IPerspectiveRegistry perspectiveRegistry;

	DebugEventHandler(ILog log, PerspectiveTracker perspectiveTracker,
			Consumer<IPerspectiveDescriptor> perspectiveSetter, IPerspectiveRegistry perspectiveRegistry,
			PluginSettings settings) {
		this.log = log;
		this.perspectiveTracker = perspectiveTracker;
		this.perspectiveSetter = perspectiveSetter;
		this.perspectiveRegistry = perspectiveRegistry;
		this.settings = settings;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e != null && Objects.equals(e.getActionCommand(), DebugEventListener.DEBUGGING_TERMINATED)) {
				onDebuggingTerminated();
			}
		} catch (Exception e1) {
			log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "", e1));
		}
	}

	private void onDebuggingTerminated() {
		Optional<IPerspectiveDescriptor> previousPerspective = perspectiveTracker.getPreviousPerspective();
		IPerspectiveDescriptor currentPerspective = perspectiveTracker.getCurrentPerspective();

		if (!isDebugPerspective(currentPerspective)) {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
					"not setting perspective: the current perspective is not the the debug perspective"));
			return;
		}

		if (isDebugPerspective(previousPerspective.orElse(null))) {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
					"not setting perspective: the previous perspective is the debug perspective"));
			return;
		}

		String msg = "handling event: " + DebugEventListener.DEBUGGING_TERMINATED;
		log.log(new Status(Status.INFO, Activator.PLUGIN_ID, msg));

		IPerspectiveDescriptor targetPerspective = determineTargetPerspective(previousPerspective);

		if (targetPerspective != null) {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
					"setting perspective to " + targetPerspective.getId()));
			perspectiveSetter.accept(targetPerspective);
		} else {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
					"not setting perspective, targetPerspective: null, current perspective: " + currentPerspective
							+ ", previous perspective: " + previousPerspective));
		}
	}

	private IPerspectiveDescriptor determineTargetPerspective(Optional<IPerspectiveDescriptor> previousPerspective) {
		IPerspectiveDescriptor targetPerspective = null;
		PerspectiveSwitchMode switchMode = settings.getPerspectiveSwitchMode();
		switch (switchMode) {
		case Previous:
			targetPerspective = previousPerspective.orElse(null);
			break;
		case PreviousWithFallback:
			targetPerspective = previousPerspective.orElse(null);
			if (targetPerspective == null) {
				targetPerspective = getFallbackPerspective();
			}
			break;
		case Fallback:
			targetPerspective = getFallbackPerspective();
			break;
		default:
			log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "unexpected perspective switch mode: " + switchMode));
			break;
		}
		return targetPerspective;
	}

	private IPerspectiveDescriptor getFallbackPerspective() {
		return perspectiveRegistry.findPerspectiveWithId(settings.getFallbackPerspectiveId());
	}

	private boolean isDebugPerspective(IPerspectiveDescriptor p) {
		final String idOfDebugPerspective = "org.eclipse.debug.ui.DebugPerspective";
		return p != null && idOfDebugPerspective.equalsIgnoreCase(p.getId());
	}
}