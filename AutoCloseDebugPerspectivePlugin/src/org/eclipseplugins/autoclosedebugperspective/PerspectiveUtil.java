package org.eclipseplugins.autoclosedebugperspective;

import java.util.concurrent.FutureTask;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;

class PerspectiveUtil implements IPerspectiveUtil {
	private static final String idOfDebugPerspective = "org.eclipse.debug.ui.DebugPerspective";
	private IWorkbench workbench;
	private ILog log;

	public PerspectiveUtil(IWorkbench workbench, ILog log) {
		super();
		this.workbench = workbench;
		this.log = log;
	}

	@Override
	public boolean isDebugPerspective(IPerspectiveDescriptor p) {
		return p != null && idOfDebugPerspective.equalsIgnoreCase(p.getId());
	}

	@Override
	public IPerspectiveDescriptor getCurrentPerspective() {
		FutureTask<IPerspectiveDescriptor> f = new FutureTask<IPerspectiveDescriptor>(() -> {
			return workbench.getActiveWorkbenchWindow().getActivePage().getPerspective();
		});
		workbench.getDisplay().syncExec(f);
		try {
			return f.get();
		} catch (Exception e) {
			log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "error getting current perspective", e));
			return null;
		}
	}

	@Override
	public void setPerspective(IPerspectiveDescriptor p) {
		workbench.getDisplay().asyncExec(() -> {
			try {
				workbench.getActiveWorkbenchWindow().getActivePage().setPerspective(p);
			} catch (Exception e) {
				log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "error setting perspective", e));
			}
		});
	}
}