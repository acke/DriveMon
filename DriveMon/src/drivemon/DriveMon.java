package drivemon;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DriveMon extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "DiscMon";

	public static final String RUN_IMG = PLUGIN_ID + ".run";

	public static final String STOP_IMG = PLUGIN_ID + ".stop";

	public static final String ICON_IMG = PLUGIN_ID + ".icon";

	private boolean pluginIsPartOfRCP;

	// The shared instance
	private static DriveMon plugin;

	/**
	 * The constructor
	 */
	public DriveMon() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginIsPartOfRCP = false;
		IPreferenceStore prefStore = getDefault().getPreferenceStore();
		prefStore.setDefault(DriveMonConstants.MONITOR_PREF, "");
		prefStore.setDefault(DriveMonConstants.ONTOP_PREF, false);
		prefStore.setDefault(DriveMonConstants.TRAY_PREF, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DriveMon getDefault() {
		return plugin;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(RUN_IMG, ImageDescriptor.createFromFile(getClass(),
				"/icons/run.gif"));
		reg.put(STOP_IMG, ImageDescriptor.createFromFile(getClass(),
				"/icons/stop.gif"));
		reg.put(ICON_IMG, ImageDescriptor.createFromFile(getClass(),
				"/icons/saveall_edit.gif"));
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public void useAsAnRCP(boolean hupp) {
		pluginIsPartOfRCP = hupp;
	}

	public boolean isRCP() {
		return pluginIsPartOfRCP;
	}

}
