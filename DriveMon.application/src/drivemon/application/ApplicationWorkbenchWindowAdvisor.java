package drivemon.application;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import drivemon.DriveMon;
import drivemon.DriveMonConstants;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	@Override
	public boolean preWindowShellClose() {
		if (DriveMon.getDefault().getPreferenceStore().getBoolean(
				DriveMonConstants.TRAY_PREF)) {
			final TrayItem trayItem = new TrayItem(Display.getCurrent()
					.getSystemTray(), SWT.NONE);
			Image trayImage = DriveMon.getDefault().getImageRegistry().get(
					DriveMon.ICON_IMG);
			trayItem.setToolTipText("DriveMon");
			trayItem.setImage(trayImage);
			final Shell driveMonShell = getWindowConfigurer().getWindow()
					.getShell();
			driveMonShell.setVisible(false);

			final Menu trayMenu = new Menu(driveMonShell, SWT.POP_UP);
			final MenuItem exit = new MenuItem(trayMenu, SWT.PUSH);

			trayItem.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// Show a status menu here?
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					driveMonShell.setVisible(true);
					driveMonShell.setActive();
					driveMonShell.setFocus();
					driveMonShell.setMinimized(false);

					trayItem.dispose();
				}
			});

			exit.setText("Exit");
			exit.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					getWindowConfigurer().getWorkbenchConfigurer()
							.getWorkbench().close();
				}

			});
			trayItem.addMenuDetectListener(new MenuDetectListener() {

				@Override
				public void menuDetected(MenuDetectEvent e) {
					trayMenu.setVisible(true);
				}
			});
			return false;
		}
		return super.preWindowShellClose();
	}

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(500, 200));
		configurer.setShowCoolBar(false);
		configurer.setShowMenuBar(true);
		int style = SWT.SHELL_TRIM;
		if (DriveMon.getDefault().getPreferenceStore().getBoolean(
				DriveMonConstants.ONTOP_PREF)) {
			style |= (SWT.ON_TOP | SWT.TOOL);
		}
		DriveMon.getDefault().useAsAnRCP(true);
		configurer.setShellStyle(style);
		configurer.setShowStatusLine(false);
		configurer.setTitle("DriveMon");

	}
}
