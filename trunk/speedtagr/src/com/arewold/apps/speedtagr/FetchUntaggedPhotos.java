package com.arewold.apps.speedtagr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;



class FetchUntaggedPhotos implements ActionListener, KeyListener {
	Speedtagr speedtagr;
	
	public FetchUntaggedPhotos(Speedtagr speedtagr){
		super();
		this.speedtagr = speedtagr;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			speedtagr.fetchNewUntaggedPhotos();
			speedtagr.pullAndShowPhotoWithTags();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (FlickrException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_F10) {

			try {
				speedtagr.fetchNewUntaggedPhotos();
				speedtagr.pullAndShowPhotoWithTags();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FlickrException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	
	}
}
