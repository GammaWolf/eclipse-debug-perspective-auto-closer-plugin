package org.eclipseplugins.autoclosedebugperspective;

import org.eclipse.ui.IPerspectiveDescriptor;

interface IPerspectiveUtil {
	IPerspectiveDescriptor getCurrentPerspective();
	boolean isDebugPerspective(IPerspectiveDescriptor p);
	void setPerspective(IPerspectiveDescriptor p);
}