package courseProject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class AVPlayer {
	//@SuppressWarnings("deprecation")
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException {
		if (args.length < 2) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
		    return;
		}
		int mark = 0;
		Num i = new Num(0);
		File file = new File(args[0]);
		i.frame_num = file.length()/(480*270*3);
		playS threadS = new playS(args[1],i);
		playV threadV = new playV(args,i);
		threadS.start();
		threadV.start();
		while(true){
			if(i.i == 1 && mark == 0) {threadS.suspend();mark=1;}
			if(i.i == 0 && mark == 1) {threadS.resume();mark=0;}
			if(i.i == 0 && mark == 2) {threadS = new playS(args[1],i);threadS.start();mark=0;}
			if(i.i == -1) {threadS.stop();mark = 2;}
		}
	}
}
class playS extends Thread{
	String filename;
	Num SoundPause;
	public playS(String arg,Num i){
		filename = new String(arg);
		this.SoundPause = i;
		//System.out.println(SoundPause.frame_num);
	}
	
	
	public void run(){
		// opens the inputStream
		FileInputStream inputStream;
		try {
			File file = new File(filename);
			SoundPause.audio_segment = (int) (file.length()/SoundPause.frame_num);
			inputStream = new FileInputStream(file);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// initializes the playSound Object
		PlaySound playSound = new PlaySound(inputStream,SoundPause);
	
		// plays the sound
		try {
				playSound.play();
		} catch (PlayWaveException e) {
			e.printStackTrace();
			return;
		} 
		
		
	}
}

class playV extends Thread{
	String args[];
	//volatile int VideoPause = -1;
	Num VideoPause;
	public playV(String arg[],Num i){
		args = arg;
		this.VideoPause = i;
	}
	public void run(){
		JFrame frame;
		JLabel lbIm1;
		JLabel lbIm2;
		BufferedImage img;
		Button btn1 = new Button("Play");
		Button btn2 = new Button("Pause");
		Button btn3 = new Button("Stop");
		int width = 480;
		int height = 270;

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		
		// Use labels to display the images
				frame = new JFrame();
				GridBagLayout gLayout = new GridBagLayout();
				frame.getContentPane().setLayout(gLayout);

				JLabel lbText1 = new JLabel("Video: " + args[0]);
				lbText1.setHorizontalAlignment(SwingConstants.LEFT);
				JLabel lbText2 = new JLabel("Audio: " + args[1]);
				lbText2.setHorizontalAlignment(SwingConstants.LEFT);
				
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.anchor = GridBagConstraints.CENTER;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 0;
				//frame.getContentPane().add(lbText1, c);
				frame.add(lbText1,c);

				c.fill = GridBagConstraints.HORIZONTAL;
				c.anchor = GridBagConstraints.CENTER;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 1;
				//frame.getContentPane().add(lbText2, c);
				frame.add(lbText2,c);
				
				
				c.fill = GridBagConstraints.NONE;
				c.anchor = GridBagConstraints.WEST;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 3;
				frame.add(btn1, c);
				
				c.fill = GridBagConstraints.NONE;
				c.anchor = GridBagConstraints.CENTER;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 3;
				frame.add(btn2, c);
				
				c.fill = GridBagConstraints.NONE;
				c.anchor = GridBagConstraints.EAST;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 3;
				frame.add(btn3, c);
				
				c.fill = GridBagConstraints.HORIZONTAL;
				c.anchor = GridBagConstraints.CENTER;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 2;
				
				btn1.setActionCommand("play");
				btn1.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						VideoPause.i = 0;
					}
				});
				btn2.setActionCommand("pause");
				btn2.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						VideoPause.i = 1;
					}
				});
				btn3.setActionCommand("stop");
				btn3.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						VideoPause.i = -1;
					}
				});
				
				frame.addWindowListener(new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						System.exit(0);
					}
				});

		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			//long len = file.length();
			long len = width*height*3;
			long file_len = file.length();
			byte[] bytes = new byte[(int)len];
			long frame_num = file_len / len;
			VideoPause.frame_num = frame_num;
			long frameRead = 0;

			while(frameRead < frame_num){
				//System.out.print(frameRead);
				//System.out.println(frameRead + ", " + VideoPause);
				while(VideoPause.i == 1);
				if(VideoPause.i == -1){
					is.close();
					is = new FileInputStream(file);
					VideoPause.i = 1;
				}
				//if(VideoPause == 0){
				
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				frameRead++;
	
				int ind = 0;
				for(int y = 0; y < height; y++){
	
					for(int x = 0; x < width; x++){
	
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
	
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
				lbIm1 = new JLabel(new ImageIcon(img));
				//frame.getContentPane().add(lbIm1, c);
				frame.add(lbIm1, c);

				frame.pack();
				frame.setVisible(true);
				
				
				try   
				{   
				Thread.currentThread();
				Thread.sleep(60);//毫秒   
				}   
				catch(Exception e){}  
				 
			}

			//}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}

class Num{
	volatile int i;
	volatile long frame_num;
	volatile int audio_segment;
	
	public Num(int n){
		this.i = n;
		this.frame_num = 0;
		this.audio_segment = 0;
	}
}
