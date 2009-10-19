package drivemon.ui;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import drivemon.DriveMon;
import drivemon.DriveMonConstants;

public class DriveMonPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Composite page;
	private Table driveTable;
	private ArrayList<String> drives;
	private final int DRIVE_COLUMN = 0;
	private final int BUTTON_WIDTH = 70;
	private Button removeButton;
	private Button ontopButton;
	private Button trayButton;

	@Override
	protected Control createContents(Composite parent) {
		page = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		page.setLayout(layout);
		Label label = new Label(page, SWT.NONE);
		label.setText("Drives:");
		GridData labelGD = new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 1);
		label.setLayoutData(labelGD);
		label.setVisible(true);

		driveTable = new Table(page, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		GridData tableGD = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3);
		driveTable.setLayoutData(tableGD);
		driveTable.setHeaderVisible(false);
		driveTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!removeButton.isEnabled()) {
					removeButton.setEnabled(true);
				}
			}
		});

		TableColumn column = new TableColumn(driveTable, SWT.CENTER);
		column.setText("Drives");
		column.setResizable(true);

		drives = getDrives();
		populateTable();

		Button addButton = new Button(page, SWT.PUSH);
		addButton.setText("Add...");
		GridData buttonGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		buttonGD.widthHint = BUTTON_WIDTH;
		addButton.setLayoutData(buttonGD);
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(page.getShell(),
						SWT.None);
				String driveCandidate = dd.open();
				if (driveCandidate != null) {
					if (!driveCandidate.endsWith(Character
							.toString(File.separatorChar))) {
						driveCandidate += File.separatorChar;
					}
					File testFile = new File(driveCandidate);
					if (testFile.isDirectory()) {
						drives.add(driveCandidate);
						populateTable();
					}
				}
			}
		});

		GridData removeButtonGD = new GridData(SWT.RIGHT, SWT.TOP, false,
				false, 1, 2);
		removeButtonGD.widthHint = BUTTON_WIDTH;
		removeButton = new Button(page, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setLayoutData(removeButtonGD);
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = driveTable.getSelectionIndex();
				if (selectionIndex != -1) {
					drives.remove(selectionIndex);
					populateTable();
				}
			}
		});

		Label ontopLabel = new Label(page, SWT.NONE);
		ontopLabel
				.setText("Set if DriveMon should always be on top (Requires a restart of DriveMon)");
		ontopLabel.setVisible(true);
		GridData ontopLabelGD = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1);
		ontopLabel.setLayoutData(ontopLabelGD);

		ontopButton = new Button(page, SWT.CHECK);
		ontopButton.setText("Always on top");
		GridData ontopButtonGD = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1);
		ontopButton.setLayoutData(ontopButtonGD);
		ontopButton.setSelection(getPreferenceStore().getBoolean(
				DriveMonConstants.ONTOP_PREF));

		trayButton = new Button(page, SWT.CHECK);
		trayButton.setText("Enable minimize to tray");
		GridData trayButtonGD = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1);
		trayButton.setLayoutData(trayButtonGD);
		trayButton.setSelection(getPreferenceStore().getBoolean(
				DriveMonConstants.TRAY_PREF));

		return page;
	}

	@Override
	protected void performDefaults() {
		getPreferenceStore().setToDefault(DriveMonConstants.MONITOR_PREF);
		getPreferenceStore().setToDefault(DriveMonConstants.ONTOP_PREF);
		getPreferenceStore().setToDefault(DriveMonConstants.TRAY_PREF);

		ontopButton.setSelection(getPreferenceStore().getBoolean(
				DriveMonConstants.ONTOP_PREF));
		trayButton.setSelection(getPreferenceStore().getBoolean(
				DriveMonConstants.TRAY_PREF));
		drives = getDrives();
		populateTable();
	}

	@Override
	protected void performApply() {
		saveChanges();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		saveChanges();
		return super.performOk();
	}

	private void saveChanges() {
		StringBuilder driveBuilder = new StringBuilder();
		for (int i = 0; i < drives.size(); i++) {
			driveBuilder.append(drives.get(i));
			if (i != drives.size() - 1) {
				driveBuilder.append(";");
			}
		}
		getPreferenceStore().setValue(DriveMonConstants.MONITOR_PREF,
				driveBuilder.toString());
		getPreferenceStore().setValue(DriveMonConstants.ONTOP_PREF,
				ontopButton.getSelection());
		getPreferenceStore().setValue(DriveMonConstants.TRAY_PREF,
				trayButton.getSelection());
	}

	private void populateTable() {
		driveTable.removeAll();
		for (int i = 0; i < drives.size(); i++) {
			TableItem drive = new TableItem(driveTable, SWT.FILL);
			drive.setText(drives.get(i));
		}
		driveTable.getColumn(DRIVE_COLUMN).pack();

	}

	private ArrayList<String> getDrives() {
		String savedDrives = getPreferenceStore().getString(
				DriveMonConstants.MONITOR_PREF);
		String[] arrayDrives = savedDrives.split(";");
		ArrayList<String> newDrives = new ArrayList<String>();
		for (int i = 0; i < arrayDrives.length; i++) {
			if (!arrayDrives[i].equals("")) {
				newDrives.add(arrayDrives[i]);
			}
		}
		return newDrives;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(DriveMon.getDefault().getPreferenceStore());
	}

}
