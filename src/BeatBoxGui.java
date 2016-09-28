import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
	private JTextArea chatBox;
	private JTextArea userInputBox;
	
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
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBackground(new Color(153, 204, 204));
		// empty border to create margin between the edges of the panel and where the components are placed
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		frame.getContentPane().add(background);
		
		checkboxList = new ArrayList<JCheckBox>();
		// hold the buttons in a box with vertical layout, add empty border to add default spacing of 2 pixels between buttonBox components and mainPanel
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		buttonBox.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		
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
		
		// adding serialize buttons
		JButton serializeBtn = new JButton("Serialize It");
		serializeBtn.addActionListener(new SerializeListener());
		buttonBox.add(serializeBtn);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		JButton restoreBtn = new JButton("Restore");
		restoreBtn.addActionListener(new LoadListener());
		buttonBox.add(restoreBtn);
		buttonBox.add(Box.createRigidArea(new Dimension(0,10)));
		
		//adding client chat components
		chatBox = new JTextArea("No new messages", 4, 10);
		chatBox.setEditable(false);
		chatBox.setLineWrap(true);
		chatBox.setWrapStyleWord(true);
		buttonBox.add(chatBox);
		buttonBox.add(Box.createRigidArea(new Dimension(0,2)));
		
		userInputBox = new JTextArea(4,10);
		userInputBox.setLineWrap(true);
		userInputBox.setWrapStyleWord(true);
		buttonBox.add(userInputBox);
		buttonBox.add(Box.createRigidArea(new Dimension(0,5)));
		
		// TODO implement send button listener
		JButton sendChatBtn = new JButton("SendIt");
		buttonBox.add(sendChatBtn);
		
		
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
		
		// set size to 75% of screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (screenSize.getWidth() * 0.75);
		int height = (int) (screenSize.getHeight() * 0.75);
		frame.setSize(width, height);
		
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

	private class StartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			buildTrackAndStart();
		}
		
	}
	
	private class StopListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			sequencer.stop();
		}
		
	}
	
	private class UpTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// adjust the tempo +3%
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 1.03)); 
		}
	}
	
	private class DownTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// adjust the tempo -3%
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * .97)); 
		}
		
	}
	
	private class ResetTempoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			// reset TempoFactor to 1
			sequencer.setTempoFactor(1f);
		}
	}
	
	private class ClearSelectedListener implements ActionListener{
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
	
	private class SerializeListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent event) {
		// create a boolean area of size 256, loop through checkboxList and set boolean[i] to true if checkbox at that position is checked
		// prompt the user to choose a file
		// write the boolean array to given file
		
		boolean[] checkboxes = new boolean[256];
		for (int i = 0; i<checkboxList.size(); i++){
		    if (checkboxList.get(i).isSelected()){
			checkboxes[i] = true;
		    }
		}
		
		JFileChooser fileSave = new JFileChooser();
		fileSave.showSaveDialog(frame);
		
		try {
		    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileSave.getSelectedFile()));
		    os.writeObject(checkboxes);
		    os.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
	
	private class LoadListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent event) {
		// prompt user for a file, load the boolean array from file, assign the values to the checkboxList ArrayList
		
		try{
		    JFileChooser openFile = new JFileChooser();
		    openFile.showOpenDialog(frame);
		    ObjectInputStream is = new ObjectInputStream(new FileInputStream(openFile.getSelectedFile()));
		    boolean[] checkBoxes = (boolean[]) is.readObject();
		    is.close();
		    
		    for (int i=0; i<checkBoxes.length; i++){
			JCheckBox box = checkboxList.get(i);
			box.setSelected(checkBoxes[i]);
			checkboxList.set(i, box);
		    }
		    
		    sequencer.stop();
		    buildTrackAndStart();
		}catch(IOException | ClassNotFoundException e){
		    e.printStackTrace();
		}catch (Exception e){
		    e.printStackTrace();
		}
		
	    }
	    
	}

}
