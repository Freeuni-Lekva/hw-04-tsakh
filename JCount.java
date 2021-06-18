// JCount.java

/*
 Basic GUI/Threading exercise.
*/

import javax.swing.*;
import java.awt.event.*;

public class JCount extends JPanel {
	JTextField textField;
	JLabel label;
	JButton startButton;
	JButton stopButton;
	CounterThread counter;
	boolean started;
	class CounterThread extends Thread{
		int input;
		public CounterThread(int n){
			input = n;
		}
		@Override
		public void run(){
			int currNum = 0;
			while (currNum <= input){
				if (isInterrupted()) break;
				if (currNum % 10000 == 0){
					int newCount = currNum; // for swingUtilities
					try {
						sleep(100);
					} catch (InterruptedException e) {
						break;
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							label.setText(newCount + "");
						}
					});
				}
				currNum++;
			}
		}
	}
	public JCount() {
		// Set the JCount to use Box layout
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		counter = null;
		started = false;
		textField = new JTextField();
		add(textField);
		label = new JLabel("0");
		add(label);
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startButtonClicked();
			}
		});
		add(startButton);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopButtonClicked();
			}
		});
		add(stopButton);
	}
	private void stopButtonClicked(){
		if(started) counter.interrupt();
	}

	private void startButtonClicked(){
		if (started) counter.interrupt();
		started = true;
		int n = Integer.parseInt(textField.getText());
		counter = new CounterThread(n);
		counter.start();
	}
	
	static public void main(String[] args)  {
		// Creates a frame with 4 JCounts in it.
		// (provided)
		JFrame frame = new JFrame("The Count");
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		frame.add(new JCount());
		frame.add(new JCount());
		frame.add(new JCount());
		frame.add(new JCount());

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

