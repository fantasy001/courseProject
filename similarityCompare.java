import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;



public class similarityCompare {
	static int targetWidth = 480;
	static int targetHeight = 270;
	static byte []targetImg = new byte[targetWidth * targetHeight * 3];
	
	public static double  computeSimilarity(byte xR, byte xG, byte xB, byte yR, byte yG, byte yB)
	{
		double result = 0;
		
		double xXYZ[] = {0,0,0};
		double yXYZ[] = {0,0,0};
		xXYZ = convertRGBtoXYZ(xR, xG, xB);
		yXYZ = convertRGBtoXYZ(yR, yG, yB);
		
		double xLAB[] = {0,0,0};
		double yLAB[] = {0,0,0};
		
		xLAB = convertXYZtoLAB(xXYZ[0], xXYZ[1], xXYZ[2]);
	    yLAB = convertXYZtoLAB(yXYZ[0], yXYZ[1], yXYZ[2]);		
		
	    result = diffLAB(xLAB[0], xLAB[1], xLAB[2], yLAB[0], yLAB[1], yLAB[2]);
		
		return result;
	}
    
	private static double[] convertRGBtoXYZ(byte xR, byte xG, byte xB)
	{
		
		double var_R = (xR & 0xFF)/255.0;
		double var_G = (xG & 0xFF)/255.0;        
		double var_B = (xB & 0xFF)/255.0;        
        double result[] = {0,0,0};
		
		if ( var_R > 0.04045 )
		{
			var_R = Math.pow(( var_R + 0.055 ) / 1.055,  2.4);
		}
		else
		{
			var_R = var_R / 12.92;
		}
		
		if ( var_G > 0.04045 )
		{
			var_G = Math.pow(( var_G + 0.055 ) / 1.055,  2.4);
		}
		else
		{
			var_G = var_G / 12.92;
		}
		
		if ( var_B > 0.04045 )
		{
			var_B = Math.pow(( var_B + 0.055 ) / 1.055,  2.4);	
		}
		else
		{
			var_B = var_B / 12.92;
		}
		var_R = var_R * 100;
		var_G = var_G * 100;
		var_B = var_B * 100;

		//Observer. = 2°, Illuminant = D65
		result[0] = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
		result[1] = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
		result[2] = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
		
		return result;
	}
	
	
	private static double[] convertXYZtoLAB(double X, double Y, double Z)
	{
		double var_X = X / 95.047;         //ref_X =  95.047   Observer= 2°, Illuminant= D65
		double var_Y = Y / 100.000;        //ref_Y = 100.000
		double var_Z = Z / 108.883;        //ref_Z = 108.883
        double CIE[] = {0,0,0};
		
		
	    if ( var_X > 0.008856 ) 
	    {
	    	var_X = Math.pow(var_X, 1/3);
	    }
	    else
	    {
			var_X = ( 7.787 * var_X ) + 16.0/116;
	    }
	    
		if ( var_Y > 0.008856 )
		{
			var_Y = Math.pow(var_Y, 1/3);
		}
		else
		{
			var_Y = ( 7.787 * var_Y ) + 16.0/116;
		}
		
		if ( var_Z > 0.008856 )
		{
			var_Z = Math.pow(var_Z, 1/3);
		}
		else
		{
			var_Z = ( 7.787 * var_Z ) + 16.0/116;
		}
		CIE[0] = (116 * var_Y ) - 16;
		CIE[1] = 500 * ( var_X - var_Y);
		CIE[2] = 200 * ( var_Y - var_Z );
        
		return CIE;

	
	}
	//DeltaE94 computation
	private static double diffLAB(double L1, double a1, double b1, double L2, double a2, double b2)
	{
		double xC1 = Math.sqrt( Math.pow(a1, 2) + Math.pow(b1, 2) );
		double xC2 = Math.sqrt( Math.pow(a2, 2) + Math.pow(b2, 2) );
		double xDL = L2 - L1;
		double xDC = xC2 - xC1;
		double xDE = Math.sqrt( 
				    ((L1 - L2) * (L1 - L2))
		          + ((a1 - a2) * (a1 - a2))
		          + ((b1 - b2) * (b1 - b2)));
		
		//The code has been changed
		
		double xDH = 0;
		if ( 
				Math.sqrt( xDE ) > ( 
						Math.sqrt(Math.abs(xDL)) + Math.sqrt(Math.abs(xDC)) 
						           ) 
		   ) 
		{
		   xDH = Math.sqrt( ( xDE * xDE ) - ( xDL * xDL ) - ( xDC * xDC ));
		}
		else {
		   xDH = 0;
		}
		
		
		
		double xSC = 1 + ( 0.045 * xC1 );
		double xSH = 1 + ( 0.015 * xC1 );
		xDL /= 1;
		xDC /= xSC;
		xDH /= xSH;
		
		return Math.sqrt( Math.pow(xDL, 2) + Math.pow(xDC, 2) + Math.pow(xDH,2));
	}
	//This is used to test show the single image
	public static void showSingleImage(byte[] imgData, String title){
		JFrame frame = new JFrame();
		JLabel lbIm1;

		BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);;
		
		 int ind = 0;
         for(int y = 0; y < targetHeight; y++){
             for(int x = 0; x < targetWidth; x++){

                 byte r = imgData[ind];
                 byte g = imgData[ind + targetWidth * targetHeight];
                 byte b = imgData[ind + targetWidth * targetHeight *2]; 

                 int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

                 //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                 img.setRGB(x,y,pix);
                 ind++;

             }

         }
		
		frame = new JFrame();

        GridBagLayout gLayout = new GridBagLayout();

        frame.getContentPane().setLayout(gLayout);

        
        JLabel lbText1 = new JLabel(title);

        lbText1.setHorizontalAlignment(SwingConstants.CENTER);

        lbIm1 = new JLabel(new ImageIcon(img));

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;

        c.anchor = GridBagConstraints.CENTER;

        c.weightx = 0.5;

        c.gridx = 0;

        c.gridy = 0;

        frame.getContentPane().add(lbText1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

        frame.pack();
        frame.setVisible(true);		
	}
	
	
	
	
    //args[0] = "Alin_Day1_002.rgb"
	//args[1] = "12651.rgb"
	public ArrayList compareStreamWithSingleImage(String[] args, int subsample, int topK, ArrayList <String> myListWords){
		JFrame frame;
		JLabel lbIm1;
		JLabel lbIm2;
	   
        //This is the original size of the picture
		int width = 1280;
		int height = 720;
		byte []comparingImg = new byte[targetWidth * targetHeight * 3];
		ArrayList <byte []> myList = new ArrayList<byte[]> ();
		

		try {

            //read image two and compress it to 480 * 270
			File file = new File(args[1]);
			InputStream is = new FileInputStream(file);
			long len = width * height * 3;
			byte[] bytes = new byte[(int)len];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			int ind = 0;
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					//Save the comparing image only save to 480 * 270 format
					if (
							( x % 8 == 0 || x % 8 == 3 || x % 8 == 6) &&
							( y % 8 == 0 || y % 8 == 3 || y % 8 == 6)
					   )
					{
						int newX = x / 8 * 3 + x % 8 / 3;
						int newY = y / 8 * 3 + y % 8 / 3;
						comparingImg[newX + newY * targetWidth] = r;
						comparingImg[newX + newY * targetWidth + targetHeight * targetWidth] = g;
						comparingImg[newX + newY * targetWidth + targetHeight * targetWidth * 2] = b;
						
					}
					ind++;
				}
			}
			System.out.println("compress to 480 * 270 successful!");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		myList.add(comparingImg);
		myListWords.add("Original pic");
		
		//Read the image stream and compare it with the comparing image
		double diffResult = 0;
		double minResult = Double.MAX_VALUE;
		
		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			long len = targetWidth * targetHeight * 3;
			long file_len = file.length();
			byte[] bytes = new byte[(int)len];
			long frame_num = file_len / len;
			long frameRead = 0;

			while(frameRead < frame_num){
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				frameRead++;
	
				int ind = 0;
				int quickerCompute = 0;
				diffResult = 0;
				byte[] tmpBytes = new byte[targetHeight * targetWidth * 3];
				
				for(int y = 0; y < targetHeight; y++){
					for(int x = 0; x < targetWidth; x++){
	
						byte r = bytes[ind];
						byte g = bytes[ind + targetHeight * targetWidth];
						byte b = bytes[ind + targetHeight * targetWidth * 2]; 
						//Save the image in a tmp array
						
						
						tmpBytes[ind] = r;
						tmpBytes[ind + targetHeight * targetWidth] = g;
						tmpBytes[ind + targetHeight * targetWidth * 2] = b; 
								
						
						//This is for image 2
						byte r2 = comparingImg[ind];
						byte g2 = comparingImg[ind + targetHeight * targetWidth];
						byte b2 = comparingImg[ind + targetHeight * targetWidth * 2]; 
	
						if (ind % subsample == 0)
						{	
							
							if (diffResult > minResult)
							{
								x = targetWidth;
								y = targetHeight;
								break;
							}
							diffResult += Math.pow(computeSimilarity(r,g,b, r2, g2, b2), 2);
						}
						
						
						
						ind++;
					}
					
					
					
				}
				
				
				
			    if (diffResult < minResult)
			    {
			    	
			    	minResult = diffResult;
			    	//showSingleImage(tmpBytes, "image Number = " + frameRead + " LAB Diff = " + diffResult);
			    	
			    	System.out.println("image Number = " + frameRead + " LAB Diff = " + diffResult);
			    	if (myList.size() == topK + 1)
			    	{	
			    		myListWords.remove(1);
			    		myList.remove(1);
			    		
			    		
			    	}
			    	myListWords.add("image Number = " + frameRead + " LAB Diff = " + diffResult);
		    		myList.add(tmpBytes);
			    }
			    
			    }
				 
			}catch (IOException e) {
			e.printStackTrace();
			} 
		
		
		
		
		
		return myList;
	}
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		/*
		double xyz[] = {0,0,0};
		double lab[] = {0,0,0};
		byte r = 5;
		byte g = 6;
		byte b = 9;
		
		byte r2 = 6;
		byte g2 = 7;
		byte b2 = 8;
		xyz = convertRGBtoXYZ(r, g, b);
		lab = convertXYZtoLAB(xyz[0], xyz[1], xyz[2]);
		//x
		System.out.println("x = " + xyz[0] + "\n" + "y = " + xyz[1] + "\n" + "z = " + xyz[2]);
		System.out.println("L = " + lab[0] + "\n" + "A = " + lab[1] + "\n" + "B = " + lab[2]);
		System.out.println("Delta E94 value should be = " + computeSimilarity(r,g,b, r2,g2,b2));

		*/
		similarityCompare simComp = new similarityCompare();
		ArrayList <byte[]> myList = new ArrayList<byte[]>();
        String []filenames ={"Alin_Day1_002.rgb", "16192.rgb"};
        ArrayList <String> myListWords = new ArrayList<String>();
		myList = simComp.compareStreamWithSingleImage(filenames, 4, 10, myListWords);
		
		for (int i = 0; i < myList.size(); i++)
		{
			showSingleImage(myList.get(i), "Top" + (myList.size() - i)+ " Match " + myListWords.get(i));
		}
	
		
	}
}
