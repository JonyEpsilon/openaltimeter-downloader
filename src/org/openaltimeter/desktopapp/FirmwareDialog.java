package org.openaltimeter.desktopapp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class FirmwareDialog extends JDialog {

	private JButton btnEraseAndFlash;
	private JButton btnCancel;
	private JPanel panel_1;
	private JButton btnViewFirmwareReadme;

	public FirmwareDialog(final Controller controller) {
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("openaltimeter firmware flash");
		setBounds(100, 100, 476, 328);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);

		btnEraseAndFlash = new JButton("Erase and flash firmware");
		btnEraseAndFlash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.doFirmwareFlash();
			}
		});
		panel.add(btnEraseAndFlash);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		panel.add(btnCancel);
		
		btnViewFirmwareReadme = new JButton("View firmware readme ...");
		btnViewFirmwareReadme.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.showFirmwareReadme();
			}});
		panel.add(btnViewFirmwareReadme);

		panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(null);

		JLabel label = new JLabel(
				"<html><p>This function allows you to flash a stable version of the firmware onto your openaltimeter. " +
				"You can use it to upgrade your firmware version, or to restore a working version of the software to an " +
				"openaltimeter that has been mis-configured.</p>" +
				"<p/>" +
				"<p>Note well that this function <b>will erase all of " +
				"the data on the altimeter and wipe any custom settings.</b> You should make sure that you've downloaded " +
				"any data you want to keep. The first thing to do after re-flashing the firmware is to check the settings " +
				"are appropriate for your configuration.</p>" +
				"<p/>" +
				"<p>You should disconnect the OA from your radio when performing the upgrade, as this can interfere with the " +
				"process.</p>" +
				"<p/>" +
				"<p>The full process takes a minute or two.</p>" +
				"<p/>" +
				"<p>You can view the firmware readme file, to see what's new in this version, by clicking the button below " +
				"(opens in new window).</p>" +
				"</html>"
		);
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setBounds(25, 11, 400, 233);
		panel_1.add(label);
	}

	public void enableButtons(final boolean enable) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				btnCancel.setEnabled(enable);
				btnEraseAndFlash.setEnabled(enable);
				if (enable)
					setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				else
					setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			}
		});
	}
}
