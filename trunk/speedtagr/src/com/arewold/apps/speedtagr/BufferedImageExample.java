package com.arewold.apps.speedtagr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BufferedImageExample extends JPanel {
	Image img;
	private double length = 50;

	public void paint(Graphics g) {
		 super.paint(g);
	        int h = img.getHeight(null);
	        int w = img.getWidth(null);
	        Graphics2D g2 = (Graphics2D)g;
	        AffineTransform tx = new AffineTransform();
	        tx.translate(((getWidth()-w*length/100)/2), (getHeight()-h)/2);
	        tx.scale(length/100, 1.0);
	        g2.drawImage(img, tx, null);
	}

	public BufferedImageExample() {
		super();

	        BufferedImage bimg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
	        Graphics g = bimg.getGraphics();
	        g.clearRect(0, 0, bimg.getWidth(), bimg.getHeight());
	        g.setColor(Color.RED);
	        g.drawOval(20, 20, 10, 10);
	        img=bimg;
	}



	public static void main(String[] args) {
	       SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                JFrame frame = new JFrame("Test");
	                frame.setLayout(new BorderLayout());
	                final BufferedImageExample imagePanel;
	                imagePanel = new BufferedImageExample();
	                frame.add(imagePanel, BorderLayout.CENTER);
//	                final JSlider slider = new JSlider();
//	                slider.addChangeListener(new ChangeListener() {
//	                    public void stateChanged(ChangeEvent e) {
//	                        imagePanel.setLength(slider.getValue());
//	                    }
//	                });
//	                frame.add(slider, BorderLayout.SOUTH);
	                imagePanel.length = 100;
	                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                frame.pack();
	                frame.setBounds(100, 100, 150, 150);
	                frame.setVisible(true);
	            }
	        });
	}
}
