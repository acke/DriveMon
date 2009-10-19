package drivemon.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import drivemon.DriveMon;
import drivemon.DriveMonConstants;

public class DriveMonView extends ViewPart {
	private Composite dmComp;
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private ArrayList<File> fileArray;
	protected String[] s = new String[0];
	private RepetitiveTrivialJob job;
	private Image runImg;
	private Image stopImg;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class DriveMonContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return s;
		}
	}

	class NameSorter extends ViewerSorter {
	}

	IPropertyChangeListener driveListener = new IPropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(DriveMonConstants.MONITOR_PREF)) {
				fileArray = setDrives();
				updateFileInfo();
				viewer.refresh();
			}
		}
	};

	/**
	 * The constructor.
	 */
	public DriveMonView() {
		fileArray = setDrives();
		IPreferenceStore preferenceStore = DriveMon.getDefault()
				.getPreferenceStore();
		preferenceStore.addPropertyChangeListener(driveListener);
		job = new RepetitiveTrivialJob();
		job.setPriority(Job.DECORATE);

		updateFileInfo();
		runImg = DriveMon.getDefault().getImageRegistry().get(DriveMon.RUN_IMG);
		stopImg = DriveMon.getDefault().getImageRegistry().get(
				DriveMon.STOP_IMG);
	}

	private ArrayList<File> setDrives() {
		ArrayList<File> newDrives = new ArrayList<File>();
		IPreferenceStore preferenceStore = DriveMon.getDefault()
				.getPreferenceStore();
		String driveString = preferenceStore
				.getString(DriveMonConstants.MONITOR_PREF);
		String[] drives = driveString.split(";");
		for (int i = 0; i < drives.length; i++) {
			File testFile = new File(drives[i]);
			if (testFile.isDirectory()) {
				newDrives.add(testFile);
			}
		}
		return newDrives;
	}

	@Override
	public void dispose() {
		IPreferenceStore pStore = DriveMon.getDefault().getPreferenceStore();
		pStore.removePropertyChangeListener(driveListener);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		dmComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		dmComp.setLayout(layout);
		viewer = new TableViewer(dmComp, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new DriveMonContentProvider());
		viewer.setLabelProvider(new DriveMonLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		Table table = viewer.getTable();

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				"DiscMon.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		if (DriveMon.getDefault().isRCP()) {
			setVisibleMenuItems(false);
		}
	}

	/**
	 * Used to hide the Menu Items when running DriveMon as a RCP application
	 * 
	 * @param visible
	 */
	public void setVisibleMenuItems(boolean visible) {
		IContributionItem[] items = getViewSite().getActionBars()
				.getToolBarManager().getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].setVisible(false);
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DriveMonView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				action1.setEnabled(false);
				action2.setEnabled(true);
				viewer.refresh();
				job.schedule();
			}
		};
		action1.setText("Start");
		action1.setToolTipText("Start monitoring");

		action1.setImageDescriptor(ImageDescriptor.createFromImage(runImg));

		action2 = new Action() {
			public void run() {
				job.cancel();
				action1.setEnabled(true);
				action2.setEnabled(false);
			}
		};
		action2.setText("Stop");
		action2.setToolTipText("Stop monitoring");
		action2.setImageDescriptor(ImageDescriptor.createFromImage(stopImg));
		action2.setEnabled(false);
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"DiskMonView", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private String calcFreeSpace(File f2) {
		double l1 = f2.getFreeSpace();
		double l2 = roundTwoDecimals(l1);
		double dblTotalSpace2 = roundTwoDecimals(f2.getTotalSpace());
		String s2 = "Freespace on  " + f2.getAbsolutePath() + ": "
				+ Double.toString(l2) + " GB" + " from a total of: "
				+ dblTotalSpace2 + " GB";
		return s2;
	}

	double roundTwoDecimals(double dbl) {
		dbl = dbl / Math.pow(1024, 3);
		int ix = (int) (dbl * 100.0); // scale it
		double dbl2 = ((double) ix) / 100.0;

		return dbl2;
	}

	private void updateFileInfo() {

		ArrayList<String> al = new ArrayList<String>();
		al.add(addTimeString());
		for (Iterator<File> iterator = fileArray.iterator(); iterator.hasNext();) {
			al.add(calcFreeSpace((File) iterator.next()));
		}
		s = (String[]) al.toArray(new String[al.size()]);
	}

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());

	}

	private String addTimeString() {
		String time;
		time = "Last update: " + now("H:mm:ss");
		return time;
	}

	class RepetitiveTrivialJob extends Job {
		public RepetitiveTrivialJob() {
			super("Check drive size");
		}

		private void refreshViewer() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
		}

		public IStatus run(IProgressMonitor monitor) {
			updateFileInfo();
			refreshViewer();
			if (action2.isEnabled()) {
				schedule(10000);
			}
			return Status.OK_STATUS;
		}
	}

}
