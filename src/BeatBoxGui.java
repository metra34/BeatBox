import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;

public class BeatBoxGui {

	private JFrame frame;
	private JPanel mainPanel;
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
		new BeatBoxGui();
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
		
		JButton resetTempo = new JButton("Tempo Reset");
		resetTempo.addActionListener(new ResetTempoListener());
		buttonBox.add(resetTempo);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		JButton clearSelected = new JButton("Clear Selection");
		clearSelected.addActionListener(new ClearSelectedListener());
		buttonBox.add(clearSelected);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		// box to hold all the names of instruments, use for loop to fill the box with Labels (should be 16)
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		nameBox.setBorder(null);
		for (int i=0; i<16; i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		// add buttons to right pane, instrument name labels to left
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		// Gridlayout to hold 16 checkboxes for 16 different instruments
		// Gridlayout(rows, cols) -> 0,0 allows to create as many as necessary, we will use the length of instrumentNames for rows, 16 for columns 
		GridLayout grid = new GridLayout(16,16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel (grid);
		background.add(BorderLayout.CENTER, mainPanel);
		
		// create 16 checkboxes for each instrument name
		for (int i=0; i < 16 * 16; i++){
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}
		
		setUpMidi();
		
		frame.pack();
		frame.setVisible(true);
	}
	
	private void setUpMidi() {
		
		/*
		private Sequencer sequencer;
		private Sequence sequence;
		private Track track;
		*/
		
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
			
		} catch (MidiUnavailableException e) {
			// getSequencer failed
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			// new Sequence failed
			e.printStackTrace();
		}
	}
	
	public void buildTrackAndStart(){
		// a 16 element array to hold the values for one instrument, if an instrument is supposed to play on that beat, the value at that element will be the key, if not then 0
		int[] trackList = null;
		
		// delete old track, make a new one
		sequence.deleteTrack(null);
		track = sequence.createTrack();
		
		for (int i=0; i<16; i++){
			trackList = new int[16];
			
			// int value of drum key that corresponds to instrument
			int key = instruments[i];
			
			for (int j=0; j<16; j++){
				JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
				if (jc.isSelected()){
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			
			makeTracks(trackList);
			track.add(makeEvent(176,1,127,0,16));
		}
		
		track.add(makeEvent(192,9,1,0,15));
		
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void makeTracks(int[] list) {
		// make events for one instrument at a time for all 16 beats, value will be either 0 or value of instrument key, for the latter make an event and add it to the track
		for (int i=0; i<16; i++){
			int key = list[i];
			
			if (key!=0){
				// to play the key
				track.add(makeEvent(144,9,key,100,i));
				// to stop playing the key
				track.add(makeEvent(128,9,key,100,i+1));
			}
		}
	}
	
	private MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return event;
	}

	public class StartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			buildTrackAndStart();
		}
		
	}
	
	public class StopListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			sequencer.stop();
		}
		
	}
	
	public class UpTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// adjust the tempo +3%
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 1.03)); 
		}
	}
	
	public class DownTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// adjust the tempo -3%
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * .97)); 
		}
		
	}
	
	public class ResetTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// reset TempoFactor to 1
			sequencer.setTempoFactor(1f);
		}
	}
	
	public class ClearSelectedListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// reset all checkboxes to selected = false, stop sequencer;
			Iterator<JCheckBox> allBoxes = checkboxList.iterator();
			while (allBoxes.hasNext()){
				allBoxes.next().setSelected(false);
			}
			sequencer.stop();
		}
	}

}
