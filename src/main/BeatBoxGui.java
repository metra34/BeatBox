package main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.sound.midi.*;
import java.util.*;

public class BeatBoxGui {
    
    private JFrame frame;
    private JPanel mainPanel;
    private ArrayList<JCheckBox> checkboxList;
    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private Sequence mySequence = null;

    // adding Client components for chat
    private JList<String> incomingList;
    private JTextField userMessage;
    private JTextField userNameField;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int nextNum;
    
    private String userName = "anonymous";
    private String ipAddress = "127.0.0.1";
    private int portNum = 5000;
    
    // store recieved checkBox settings
    private Vector<String> listVector;
    private HashMap<String, boolean[]> otherSeqsMap;

    // Names of the instruments to build JLabels with
    private String[] instrumentNames = { "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
	    "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "CowBell", "Vibraslap",
	    "Low-mid Tom", "High Agogo", "Open Hi Conga" };
    // array of all drum keys
    private int[] instruments = { 35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63 };
    
    public static void main(String[] args){
	new BeatBoxGui();
    }
    
    public BeatBoxGui(){
	 // use default settings
	connect(); // connect to server
	initialize(); // build GUI and MIDI
    }
    
    public BeatBoxGui(String userName, String ip, int num) {
	 // use custom client server settings
	this.userName = userName;
	this.ipAddress = ip;
	this.portNum = num;
	connect(); // connect to server
	initialize(); // build GUI and MIDI
    }
    
    private void connect(){
	// open connection to the server
	try{
	    Socket sock = new Socket(ipAddress, portNum);
	    out = new ObjectOutputStream(sock.getOutputStream());
	    in = new ObjectInputStream(sock.getInputStream());
	    Thread remote = new Thread(new RemoteReader());
	    remote.start();
	    
	}catch(Exception e){ 
	    System.out.println("Could not connect, offline mode only");
	}
    }

    private void initialize() {
	frame = new JFrame("BeatBox");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	BorderLayout layout = new BorderLayout();
	JPanel background = new JPanel(layout);
	background.setBackground(new Color(153, 204, 204));
	// empty border to create margin between the edges of the panel and
	// where the components are placed
	background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	frame.getContentPane().add(background);

	checkboxList = new ArrayList<JCheckBox>();
	// hold the buttons in a box with vertical layout, add empty border to
	// add default spacing of 2 pixels between buttonBox components and mainPanel
	Box buttonBox = new Box(BoxLayout.Y_AXIS);
	buttonBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

	JButton start = new JButton("Start");
	start.addActionListener(new StartListener());
	buttonBox.add(start);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5))); // create space between buttons using rigid area

	JButton stop = new JButton("Stop");
	stop.addActionListener(new StopListener());
	buttonBox.add(stop);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));

	JButton upTempo = new JButton("Tempo Up");
	upTempo.addActionListener(new UpTempoListener());
	buttonBox.add(upTempo);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));

	JButton downTempo = new JButton("Tempo Down");
	downTempo.addActionListener(new DownTempoListener());
	buttonBox.add(downTempo);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));

	JButton resetTempo = new JButton("Tempo Reset");
	resetTempo.addActionListener(new ResetTempoListener());
	buttonBox.add(resetTempo);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));

	JButton clearSelected = new JButton("Clear Selection");
	clearSelected.addActionListener(new ClearSelectedListener());
	buttonBox.add(clearSelected);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));

	// adding serialize buttons
	JButton serializeBtn = new JButton("saveSet");
	serializeBtn.addActionListener(new SerializeListener());
	buttonBox.add(serializeBtn);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));

	JButton restoreBtn = new JButton("Restore");
	restoreBtn.addActionListener(new LoadListener());
	buttonBox.add(restoreBtn);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 15)));
	
	// adding client chat components
	buttonBox.add(new JLabel("User Name"));
	userNameField = new JTextField(userName);
	userNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, userNameField.getPreferredSize().height));
	buttonBox.add(userNameField);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));
	
	buttonBox.add(new JLabel("Sequence Name"));
	userMessage = new JTextField();
	userMessage.setMaximumSize(new Dimension(Integer.MAX_VALUE, userMessage.getPreferredSize().height));
	buttonBox.add(userMessage);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));
	
	JButton sendChatBtn = new JButton("sendIt");
	sendChatBtn.addActionListener(new SendButtonListener());
	buttonBox.add(sendChatBtn);
	buttonBox.add(Box.createRigidArea(new Dimension(0, 5)));
	
	buttonBox.add(new JLabel("Sequence List"));
	listVector = new Vector<String>();
	otherSeqsMap = new HashMap<String, boolean[]>();
	incomingList = new JList<String>();
	incomingList.addListSelectionListener(new MyListSelectionListener());
	incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	JScrollPane theList = new JScrollPane(incomingList);
	buttonBox.add(theList);
	incomingList.setListData(listVector); // initially empty
	buttonBox.add(Box.createRigidArea(new Dimension(0, 2)));
	//buttonBox.add(Box.createGlue());

	// box to hold all the names of instruments, use for loop to fill the
	// box with Labels (should be 16)
	Box nameBox = new Box(BoxLayout.Y_AXIS);
	nameBox.setBorder(null);
	for (int i = 0; i < 16; i++) {
	    nameBox.add(new Label(instrumentNames[i]));
	}

	// add buttons to right pane, instrument name labels to left
	background.add(BorderLayout.EAST, buttonBox);
	background.add(BorderLayout.WEST, nameBox);

	// Gridlayout to hold 16 checkboxes for 16 different instruments
	// Gridlayout(rows, cols) -> 0,0 allows to create as many as necessary,
	// we will use the length of instrumentNames for rows, 16 for columns
	GridLayout grid = new GridLayout(16, 16);
	grid.setVgap(1);
	grid.setHgap(2);
	mainPanel = new JPanel(grid);
	background.add(BorderLayout.CENTER, mainPanel);

	// create 16 checkboxes for each instrument name
	for (int i = 0; i < 16 * 16; i++) {
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
	 * private Sequencer sequencer; private Sequence sequence; private Track
	 * track;
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

    public void buildTrackAndStart() {
	// a 16 element array to hold the values for one instrument, if an
	// instrument is supposed to play on that beat, the value at that
	// element will be the key, if not then 0
	int[] trackList = null;

	// delete old track, make a new one
	sequence.deleteTrack(null);
	track = sequence.createTrack();

	for (int i = 0; i < 16; i++) {
	    trackList = new int[16];

	    // int value of drum key that corresponds to instrument
	    int key = instruments[i];

	    for (int j = 0; j < 16; j++) {
		JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
		if (jc.isSelected()) {
		    trackList[j] = key;
		} else {
		    trackList[j] = 0;
		}
	    }

	    makeTracks(trackList);
	    track.add(makeEvent(176, 1, 127, 0, 16));
	}

	track.add(makeEvent(192, 9, 1, 0, 15));

	try {
	    sequencer.setSequence(sequence);
	    sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
	    sequencer.start();
	    sequencer.setTempoInBPM(120);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void deleteTracks(){
	Track[] allTracks = sequence.getTracks();
	for (Track t : allTracks){
	    sequence.deleteTrack(t);
	}
    }

    private void makeTracks(int[] list) {
	// make events for one instrument at a time for all 16 beats, value will
	// be either 0 or value of instrument key, for the latter make an event
	// and add it to the track
	for (int i = 0; i < 16; i++) {
	    int key = list[i];

	    if (key != 0) {
		// to play the key
		track.add(makeEvent(144, 9, key, 100, i));
		// to stop playing the key
		track.add(makeEvent(128, 9, key, 100, i + 1));
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
    
    private void changeSequence(boolean[] checkboxState) {
	for (int i=0; i<256; i++){
	    JCheckBox check = (JCheckBox) checkboxList.get(i);
	    if (checkboxState[i]){
		check.setSelected(true);
	    }else{
		check.setSelected(false);
	    }
	}
    }

    private class StartListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    buildTrackAndStart();
	}

    }

    private class StopListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    sequencer.stop();
	}

    }

    private class UpTempoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    // adjust the tempo +3%
	    float tempoFactor = sequencer.getTempoFactor();
	    sequencer.setTempoFactor((float) (tempoFactor * 1.03));
	}
    }

    private class DownTempoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    // adjust the tempo -3%
	    float tempoFactor = sequencer.getTempoFactor();
	    sequencer.setTempoFactor((float) (tempoFactor * .97));
	}

    }

    private class ResetTempoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    // reset TempoFactor to 1
	    sequencer.setTempoFactor(1f);
	}
    }

    private class ClearSelectedListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    // reset all checkboxes to selected = false, stop sequencer;
	    Iterator<JCheckBox> allBoxes = checkboxList.iterator();
	    while (allBoxes.hasNext()) {
		allBoxes.next().setSelected(false);
	    }
	    
	    changeSequence(new boolean[256]);
	    sequencer.stop();
	}
    }
    
    private boolean[] getCheckboxState() {
	// make an arrayList of just the state of the checkboxes
	boolean[] checkboxes = new boolean[256];
	for (int i = 0; i < checkboxList.size(); i++) {
	    if (checkboxList.get(i).isSelected()) {
		checkboxes[i] = true;
	    }
	}

	return checkboxes;
    }
    
    private void serializeToFile() {
	// create a boolean area of size 256, loop through checkboxList and
	// set boolean[i] to true if checkbox at that position is checked
	// prompt the user to choose a file
	// write the boolean array to given file

	boolean[] checkboxes = getCheckboxState();

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

    private class SerializeListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    serializeToFile();
	}
    }

    private class LoadListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent event) {
	    // prompt user for a file, load the boolean array from file, assign
	    // the values to the checkboxList ArrayList

	    try {
		JFileChooser openFile = new JFileChooser();
		openFile.showOpenDialog(frame);
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(openFile.getSelectedFile()));
		boolean[] checkBoxes = (boolean[]) is.readObject();
		is.close();

		for (int i = 0; i < checkBoxes.length; i++) {
		    JCheckBox box = checkboxList.get(i);
		    box.setSelected(checkBoxes[i]);
		    checkboxList.set(i, box);
		}

		sequencer.stop();
		buildTrackAndStart();
	    } catch (IOException | ClassNotFoundException e) {
		e.printStackTrace();
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}
    }
    
    private class SendButtonListener implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent event) {
	    
	    boolean[] checkboxState = getCheckboxState();
	    userName = userNameField.getText();
	    try{
		out.writeObject(userName + nextNum++ + ": "+userMessage.getText());
		out.writeObject(checkboxState);
		userMessage.setText("");
	    } catch (Exception e){
		System.out.println("Could not send to server");
	    }
	}
    }
    
    private class MyListSelectionListener implements ListSelectionListener {
	// if the user makes a selection, immideatly load the patter into the
	// checkboxes
	@Override
	public void valueChanged(ListSelectionEvent le) {
	    if (!le.getValueIsAdjusting()) {
		String selected = (String) incomingList.getSelectedValue();
		if (selected != null) {
		    // ask user to save current sequence
		    int response = JOptionPane.showConfirmDialog(frame, "Do you want to save the current sequence?",
			    "Save Sequence", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    if (response != JOptionPane.CLOSED_OPTION) {
			if (response == JOptionPane.YES_OPTION) {
			    serializeToFile();
			}
			// go to the Hashmap for the sequence, and change the
			// local sequence
			boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
			changeSequence(selectedState);
			sequencer.stop();
			buildTrackAndStart();
		    }

		}
	    }
	}
    }
    
    private class RemoteReader implements Runnable{
	boolean[] checkBoxState = null;
	String nameToShow = null;
	Object obj = null;
	
	@Override
	public void run() {
	    try{
		while ((obj =in.readObject()) != null){
		    System.out.println("got an object from the server");
		    System.out.println(obj.getClass());
		    nameToShow = (String) obj;
		    checkBoxState = (boolean[]) in.readObject();
		    otherSeqsMap.put(nameToShow, checkBoxState);
		    listVector.add(nameToShow);
		    incomingList.setListData(listVector);
		}
	    } catch (Exception e) { e.printStackTrace(); }
	}
    }
    
    private class PlayMineListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent event) {
	    if (mySequence != null){
		sequence = mySequence;
	    }
	}
    }

}