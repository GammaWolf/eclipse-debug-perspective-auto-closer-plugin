package org.eclipseplugins.autoclosedebugperspective.util;

import org.eclipse.ui.IPerspectiveDescriptor;

public interface IPerspectiveUtil {
	IPerspectiveDescriptor getCurrentPerspective();
	boolean isDebugPerspective(IPerspectiveDescriptor p);
	void setPerspective(IPerspectiveDescriptor p);
}