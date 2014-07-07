import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;


public class BeatBox {
	
	JPanel mainPanel;
	ArrayList <JCheckBox> checkboxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cow Bell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open High Conga"};
	
	int [] instruments= {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
	
	public static void main(String[] args) {
		new BeatBox().buildGUI();	
	}
	
	public void buildGUI(){
		
		
		// Basic layout of the GUI interface in buildGUI class
		/*****************************************************
		*           theFrame
		*               |
		*        backgroundPanel___________________               
		*         /          \                    |
		* buttonBox        nameBox             mainPanel		*     |               |                   | 
		* 4 x JButton    instrumentNames[]   checkBoxList[]
		*                     |                   |
		*                   Labels              JCheckBoxes
		*******************************************************/
		
		
		
		
		// System.out.println("new BeatBox"); // only for test
		theFrame= new JFrame ("Cyber BeatBox");                                      //creating 'theFrame'(JFrame)
		theFrame.setDefaultCloseOperation(1);
		BorderLayout layout= new BorderLayout();
		JPanel background= new JPanel (layout);                                      //creating 'background' (JPanel)
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));
		
		checkboxList = new ArrayList<JCheckBox>();
		Box buttonBox= new Box (BoxLayout.Y_AXIS);                                      //creating buttonBox (BoxObject)
		
		
		/*******************************
		 * Adding buttons to buttonbox *
		 *******************************/
		
		JButton start = new JButton ("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton ("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton ("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);

		JButton downTempo = new JButton ("Tempo Down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton serializeButton = new JButton ("Save Pattern");
		serializeButton.addActionListener(new MySendListener());
		buttonBox.add(serializeButton);
		
		JButton readButton = new JButton ("Restore Pattern");
		readButton.addActionListener(new MyReadInListener());
		buttonBox.add(readButton);
		
		//**************************************************************
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);                                      //creating nameBox (Box Object)
		for (int i=0; i< 16; i++){
			nameBox.add(new Label(instrumentNames[i]));
			
		}

		//background.add(buttonBox); //prueba, no es el que debe
		background.add(BorderLayout.EAST, buttonBox);                         //adding 'buttonBox' to 'background'JPannel 
		background.add(BorderLayout.WEST, nameBox);                         //adding 'nameBox' to 'background'JPannel 
		
		theFrame.getContentPane().add(background);                         //adding 'background' to 'theFrame' 
		
		GridLayout grid = new GridLayout (16, 16);                         //setting up 'grid' gridLayout
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);                         //new JPannel'mainPanel' with a grid layout 
		background.add(BorderLayout.CENTER, mainPanel);       //adding 'mainPanel' to 'background'
		
		for (int i=0; i<256; i++) {                           //adding 256 JCheck to 'background'
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}
		
		
		
		setUpMidi();
		theFrame.setBounds(50, 50, 300, 300);      //final setup to theFrame
		theFrame.pack();
		theFrame.setVisible(true);
		
		}// end buildGUI method
	
	public void setUpMidi(){
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//close setUpMidi method
	
	public void buildTrackAndStart(){
		int[] trackList = null;
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for (int i= 0; i< 16; i++){                               // we're creating 16 arrays trackList. Each array will contain a sequence of 16 ints.
		                                                          // those ints will be 0 value (rest) or the number of the instrument (play)
		                                                         // the array 'track' will be used as argument to build a track pattern in the makeTrack method.
			trackList = new int[16];
			int key = instruments [i];                            // the array "instruments" is a instance variable of the beatBox class
			for (int j= 0; j< 16; j++) {
				JCheckBox jc= (JCheckBox) checkboxList.get(j + (16*i));           //this creates a new JCheckBox, a copy of the checkbox in the GUi
				                                                                  //the expression j + (16*i) selects each one of the checkBoxes in a 16x16 grid
				if (jc.isSelected()) {
					trackList[j]= key;
				} else {
					trackList[j]=0;
				}
			} //close inner for loop
			makeTracks(trackList);
			track.add(makeEvent(176, 1, 127, 0, 16));
		} //close outer for loop
		track.add(makeEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} //close buldTrackAndStart
	
	
	
	/*********************
	 *     LISTENERS     *
	 *********************/
	
	
	public class MyStartListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			buildTrackAndStart();
		}
	} //close inner class
	
	public class MyStopListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			sequencer.stop();
		}
	} //close inner class

	public class MyUpTempoListener implements ActionListener{                
		public void actionPerformed(ActionEvent a){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * 1.03));
		}
	} //close inner class
	
	public class MyDownTempoListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * .97));
           // System.out.println(sequencer.getTempoFactor());
			//sequencer.setTempoFactor(sequencer.getTempoFactor() - 1);
		}
	} //close inner class

	public class MySendListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			//System.out.println("Into the Send Listener");
			boolean [] checkboxState = new boolean [256];
			
			for (int i = 0; i < 256; i++) {
				JCheckBox check= (JCheckBox) checkboxList.get(i);
				if (check.isSelected()) {
					checkboxState[i]= true;
				}
			}
			
			try {
				//File file= new File ("CheckBox.ser");
				FileOutputStream fileStream = new FileOutputStream (new File("Checkbox.ser"));
				ObjectOutputStream os = new ObjectOutputStream(fileStream);
				os.writeObject(checkboxState);
				//os.close();
				System.out.println("object serialized!");//for testing only
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Can't save!");//for testing only
			}
		}//end method
	}//end class
	
	public class MyReadInListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			boolean[] checkboxState = null;
			try {
				FileInputStream fileIn = new FileInputStream (new File("Checkbox.ser"));
				ObjectInputStream is = new ObjectInputStream (fileIn);
				checkboxState = (boolean[])is.readObject();
				is.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}


			for (int i = 0; i < 256; i++) {
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if (checkboxState[i]){
					check.setSelected(true);
				}else {
					check.setSelected(false);
				}
			}
			
			sequencer.stop();
			buildTrackAndStart();
		}	
	}
	
	
	
	public void makeTracks(int[] list){
		for (int i= 0; i< 16; i++) {
			//System.out.println(i); //test only
			int key = list [i];
			if (key != 0) {
				track.add(makeEvent(144, 9, key, 100, i));
				track.add(makeEvent(128, 9, key, 100, i +1));
			}
		}
	} //close method
	
	public MidiEvent makeEvent (int comd, int chan, int one, int two, int tick){
		MidiEvent event= null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent (a, tick);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}
	
} // end class BeatBox