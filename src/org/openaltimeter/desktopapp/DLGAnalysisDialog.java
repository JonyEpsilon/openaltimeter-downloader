package org.openaltimeter.desktopapp;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DLGAnalysisDialog extends JDialog {
	
	private static final String PREFS_MARK_LAUNCH_HEIGHTS = "PREFS_MARK_LAUNCH_HEIGHTS";
	private static final String PREFS_MARK_MAX_HEIGHTS = "PREFS_MARK_MAX_HEIGHTS";
	private static final String PREFS_CORRECT_BASELINE = "PREFS_CORRECT_BASELINE";
	private static final String PREFS_SHOW_STATISTICS = "PREFS_SHOW_STATISTICS";

	private JCheckBox chckbxShowStatistics;
	private JCheckBox chckbxCorrectBaselineFor;
	private JCheckBox chckbxMarkMaximumHeights;
	private JCheckBox chckbxMarkLaunchHeights;
	private boolean success = false;
	Preferences prefs;

	public boolean shouldMarkLaunchHeights() {
		return chckbxMarkLaunchHeights.isSelected();
	}

	public void setMarkLaunchHeights(boolean markLaunchHeights) {
		this.chckbxMarkLaunchHeights.setSelected(markLaunchHeights);
	}

	public boolean shouldMarkMaxHeights() {
		return chckbxMarkMaximumHeights.isSelected();
	}

	public void setMarkMaxHeights(boolean markMaxHeights) {
		this.chckbxMarkMaximumHeights.setSelected(markMaxHeights);
	}

	public boolean shouldCorrectBaseline() {
		return chckbxCorrectBaselineFor.isSelected();
	}

	public void setCorrectBaseline(boolean correctBaseline) {
		this.chckbxCorrectBaselineFor.setSelected(correctBaseline);
	}

	public boolean shouldShowStatistics() {
		return chckbxShowStatistics.isSelected();
	}

	public void setShowStatistics(boolean showStatistics) {
		this.chckbxShowStatistics.setSelected(showStatistics);
	}

	public DLGAnalysisDialog(final Controller controller) {
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(DLGAnalysisDialog.class.getResource("/logo_short_64.png")));
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("DLG flight analysis");
		setBounds(100, 100, 282, 178);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(0, 358, 526, 10);
		getContentPane().add(panel);
		
		chckbxMarkLaunchHeights = new JCheckBox("Mark launch heights");
		chckbxMarkLaunchHeights.setSelected(true);
		chckbxMarkLaunchHeights.setBounds(6, 7, 180, 23);
		getContentPane().add(chckbxMarkLaunchHeights);
		
		chckbxMarkMaximumHeights = new JCheckBox("Mark maximum heights");
		chckbxMarkMaximumHeights.setSelected(true);
		chckbxMarkMaximumHeights.setBounds(6, 33, 250, 23);
		getContentPane().add(chckbxMarkMaximumHeights);
		
		chckbxCorrectBaselineFor = new JCheckBox("Correct baseline for weather shifts");
		chckbxCorrectBaselineFor.setSelected(true);
		chckbxCorrectBaselineFor.setBounds(6, 59, 250, 23);
		getContentPane().add(chckbxCorrectBaselineFor);
		
		chckbxShowStatistics = new JCheckBox("Show launch statistics window");
		chckbxShowStatistics.setSelected(true);
		chckbxShowStatistics.setBounds(6, 85, 250, 23);
		getContentPane().add(chckbxShowStatistics);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				success = true;
				prefs.putBoolean(PREFS_MARK_LAUNCH_HEIGHTS, shouldMarkLaunchHeights());			
				prefs.putBoolean(PREFS_MARK_MAX_HEIGHTS, shouldMarkMaxHeights());			
				prefs.putBoolean(PREFS_CORRECT_BASELINE, shouldCorrectBaseline());			
				prefs.putBoolean(PREFS_SHOW_STATISTICS, shouldShowStatistics());
				dispose();
			}
		});
		btnRun.setBounds(97, 115, 89, 23);
		getContentPane().add(btnRun);

		prefs = Preferences.userNodeForPackage(this.getClass());
		setMarkLaunchHeights(prefs.getBoolean(PREFS_MARK_LAUNCH_HEIGHTS, true));
		setMarkMaxHeights(prefs.getBoolean(PREFS_MARK_MAX_HEIGHTS, true));
		setCorrectBaseline(prefs.getBoolean(PREFS_CORRECT_BASELINE, true));
		setShowStatistics(prefs.getBoolean(PREFS_SHOW_STATISTICS, true));
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
	}

	public boolean isSuccessful() {
		return success;
	}

}
