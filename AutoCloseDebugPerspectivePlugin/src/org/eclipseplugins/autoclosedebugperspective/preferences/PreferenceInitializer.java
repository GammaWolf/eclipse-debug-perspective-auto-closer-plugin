package org.eclipseplugins.autoclosedebugperspective.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipseplugins.autoclosedebugperspective.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
//		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		store.setDefault(PreferenceConstants.PERSPECTIVE_SWITCH_TRIGGER_CHOICE, "0");
//		store.setDefault(PreferenceConstants.P_STRING,
//				"Default value");
	}

}
