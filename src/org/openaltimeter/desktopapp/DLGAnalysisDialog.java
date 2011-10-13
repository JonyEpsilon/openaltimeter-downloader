package org.openaltimeter.desktopapp;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class DLGAnalysisDialog extends JDialog {

	public DLGAnalysisDialog(final Controller controller) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(DLGAnalysisDialog.class.getResource("/logo_short_64.png")));
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("DLG flight analysis");
		setBounds(100, 100, 542, 406);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);

	}

	public void enableButtons(final boolean enable) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
			}
		});
	}
}
