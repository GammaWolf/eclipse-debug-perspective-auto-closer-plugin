package org.eclipseplugins.autoclosedebugperspective;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;

interface IPerspectiveUtil {
	IPerspectiveDescriptor getCurrentPerspective();
	boolean isDebugPerspective(IPerspectiveDescriptor p);
	void setPerspective(IPerspectiveDescriptor p);
}