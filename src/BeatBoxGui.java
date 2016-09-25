import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import javax.swing.border.LineBorder;

public class BeatBoxGui {

	private JFrame frame;
	private ArrayList<JCheckBox> checkboxList;
	private Sequencer sequencer;
	private Sequence sequence;
	private Track track;
	
	// Names of the instruments to build JLabels with
	private String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom",
			"Hi Bongo", "Maracas", "Whistle", "Low Conga", "CowBell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
	// array of all drum keys
	private int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
	
	public static void main (String[] args){
		BeatBoxGui g = new BeatBoxGui();
	}

	public BeatBoxGui() {
		initialize();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame("BeatBox");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(50, 50, 300, 300);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBackground(new Color(153, 204, 204));
		// empty border to create margin between the edges of the panel and where the components are placed
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		frame.getContentPane().add(background);
		
		checkboxList = new ArrayList<JCheckBox>();
		// hold the buttons in a box with vertical layout
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("Start");
		start.addActionListener(new StartListener());
		buttonBox.add(start);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5))); // create space between buttons using empty components with RigidArea
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new StopListener());
		buttonBox.add(stop);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new UpTempoListener());
		buttonBox.add(upTempo);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new DownTempoListener());
		buttonBox.add(downTempo);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		// box to hold all the names of instruments, use for loop to fill the box with Labels (should be 16)
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		nameBox.setBorder(null);
		for (int i=0; i<instrumentNames.length; i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		// add buttons to right pane, instrument name labels to left
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public class StartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			
		}
		
	}
	
	public class StopListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			
		}
		
	}
	
	public class UpTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			
		}
		
	}
	
	public class DownTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			
		}
		
	}

}
