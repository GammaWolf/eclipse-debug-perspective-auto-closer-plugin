package org.eclipseplugins.autoclosedebugperspective;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;

/*
 * Listens for debug events and raises DEBUGGING_TERMINATED event
 */
class DebugEventListener implements IDebugEventSetListener {

	public static final String DEBUGGING_TERMINATED = "DEBUGGING_TERMINATED";
	private final ILog log;
	private final List<java.awt.event.ActionListener> actionListeners = new ArrayList<java.awt.event.ActionListener>();

	public DebugEventListener(ILog log) {
		super();
		this.log = log;
	}

	public void addActionListener(java.awt.event.ActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	@Override
	public void handleDebugEvents(DebugEvent[] debugEvents) {
		try {
			if (debugEvents == null || debugEvents.length == 0 || debugEvents[0] == null)
				return;

			for (DebugEvent debugEvent : debugEvents) {
				if (debugEvent.getKind() == DebugEvent.TERMINATE) {
					log.log(new Status(Status.INFO, Activator.PLUGIN_ID, "debug event: " + debugEvents[0].toString()));

					// the TERMINATE event seems to be raised from sources
					// RuntimeProcess (once) and multiple times from source JDIThread.
					// To raise our event once the source is checked against type RuntimeProcess.
					if (debugEvent.getSource() instanceof org.eclipse.debug.core.model.RuntimeProcess) {
						log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
								"source of Terminate event was RuntimeProcess, raising DEBUGGING_TERMINATED event"));

						actionListeners.forEach(x -> x.actionPerformed(
								new ActionEvent(this, ActionEvent.ACTION_PERFORMED, DEBUGGING_TERMINATED)));

						break;
					}
				}
			}
		} catch (Exception e) {
			log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "", e));
		}
	}
}