package com.arewold.apps.speedtagr;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.Size;
import com.aetrion.flickr.tags.Tag;
import com.aetrion.flickr.tags.TagsInterface;
import com.aetrion.flickr.util.IOUtilities;

@SuppressWarnings("serial")
public class Speedtagr extends JFrame {
	private static final String NEXT_ENTER = "Next <Enter>";
	private static final int Y_LOCATION = 200;
	private static final String HOST = "api.flickr.com";
	private static final int TEXTFIELD_SIZE = 30;
	private static final int MINIMUM_TAG_LENGTH = 2;
	private static final String TAG_AUTHOR = "Speedtagr";
	private static final String APP_NAME = "Speedtagr";
	private static final int X_LOCATION = 300;
	private static final String NO_UNTAGGED_PHOTOS = "There are no untagged photos";
	private static final String PHOTO_TITLE = "Photo title: ";
	private static final int PHOTO_HEIGHT = 500;
	private static final int PHOTO_WIDTH = 500;
	Properties properties = null;
	static String apiKey;
	static String sharedSecret;
	PhotoList untaggedPhotos;
	PhotosInterface photosInterface;
	TagsInterface tagsInterface;
	Flickr flickr;
	REST rest;
	Photo currentPhoto = null;
	Photo nextPhoto = null;
	ImageIcon bufferedNextPhoto = null;
	String bufferedNextPhotoId = null;
	private int photoPointer = 0;
	JButton nextButton = null;
	JButton fetchPhotosButton = null;
	JLabel photoLabel = null;
	JLabel statusLabel = null;
	JTextField tagsTextField = null;

	public Speedtagr() {
		setSize(300, 200);
		setTitle(APP_NAME);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(X_LOCATION, Y_LOCATION);
		pack();
	}

	public static void main(String[] args) {
		Speedtagr speedtagr = new Speedtagr();
		speedtagr.setVisible(true);

		try {
			speedtagr.initPropertiesAndUI();
			speedtagr.fetchNewUntaggedPhotos();
			speedtagr.showUIAndFirstImage();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This method returns an Image object from a buffered image
	public static Image toImage(BufferedImage bufferedImage) {
		return Toolkit.getDefaultToolkit().createImage(
				bufferedImage.getSource());
	}

	private void showUIAndFirstImage() throws Exception {
		GridBagLayout gridBagLayout = new GridBagLayout();

		GridBagConstraints statusConstraints = new GridBagConstraints();
		GridBagConstraints photoConstraints = new GridBagConstraints();
		GridBagConstraints nextButtonConstraints = new GridBagConstraints();
		GridBagConstraints fetchButtonConstraints = new GridBagConstraints();
		GridBagConstraints tagsConstraints = new GridBagConstraints();

		statusConstraints.gridx = 0;
		statusConstraints.gridy = 1;
		statusConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		photoConstraints.anchor = GridBagConstraints.CENTER;
		photoConstraints.gridx = 0;
		photoConstraints.gridy = 2;
		photoConstraints.gridwidth = GridBagConstraints.REMAINDER;
//		photoConstraints.ipady = PHOTO_HEIGHT;
//		photoConstraints.ipadx = PHOTO_WIDTH;
		
			
		tagsConstraints.gridx = 0;
		tagsConstraints.gridy = 3;
		tagsConstraints.gridwidth = GridBagConstraints.REMAINDER;

		nextButtonConstraints.gridx = 3;
		nextButtonConstraints.gridy = 4;
		nextButtonConstraints.anchor = GridBagConstraints.CENTER;
		nextButtonConstraints.fill = GridBagConstraints.HORIZONTAL;

		fetchButtonConstraints.gridx = 4;
		fetchButtonConstraints.gridy = 4;
		fetchButtonConstraints.anchor = GridBagConstraints.CENTER;
		fetchButtonConstraints.fill = GridBagConstraints.HORIZONTAL;

		this.setLayout(gridBagLayout);

		fetchPhotosButton.addActionListener(new FetchUntaggedPhotos(this));
		nextButton.addActionListener(new NextListener(this));
		tagsTextField.addKeyListener(new NextListener(this));

		add(statusLabel, statusConstraints);
		add(photoLabel, photoConstraints);
		add(nextButton, nextButtonConstraints);
		add(fetchPhotosButton, fetchButtonConstraints);
		add(tagsTextField, tagsConstraints);

		pullAndShowPhotoWithTags();

		pack();

		tagsTextField.grabFocus();
	}

	private ImageIcon fetchImageIconOfFlickrImage(Photo photoToGet) throws IOException,
			FlickrException {
		BufferedImage bufferedImage = photosInterface.getImage(photoToGet,
				Size.MEDIUM);
		Image image = toImage(bufferedImage);
		ImageIcon imageIcon = new javax.swing.ImageIcon();
		imageIcon.setImage(image);
		return imageIcon;
	}

	protected void saveTags() {
		String tagCandidates = tagsTextField.getText();
		String[] tagStrings = tagCandidates.split("\\s+");
		// String[] tagStrings = {tagCandidates};

		if (tagStrings.length < 1) {
			return;
		}

		try {
			authenticateAndConnect();
			photosInterface.setTags(currentPhoto.getId(), tagStrings);
		} catch (IOException e) {
			showErrorMessage(e);
		} catch (SAXException e) {
			showErrorMessage(e);
		} catch (FlickrException e) {
			showErrorMessage(e);
		}

		System.out.println(tagCandidates + " could become tags!");
	}

	private void showErrorMessage(Exception e) {
		e.printStackTrace();
	}

	protected void fetchNewUntaggedPhotos() throws FlickrException,
			SAXException, IOException {
		authenticateAndConnect();
		photoPointer = 0;

		untaggedPhotos = photosInterface.getUntagged(10, 0);
		// untaggedPhotos = photosInterface.getNotInSet(10, 0);

		for (int j = 0; j < untaggedPhotos.size(); j++) {
			System.out.println(((Photo) untaggedPhotos.get(j)).getTitle());
		}
	}

	public void fetchAndShowNextImage() throws IOException, FlickrException,
			SAXException {
		System.out.println("Getting next image");
		safelyIncrementAndWraparoundPhotoPointer();
		pullAndShowPhotoWithTags();
	}

	protected void pullAndShowPhotoWithTags() throws IOException,
			FlickrException, SAXException {
		if (untaggedPhotos.size() > 0) {
			
			tagsTextField.setEnabled(true);
			tagsTextField.grabFocus();
			currentPhoto = (Photo) untaggedPhotos.get(photoPointer);
			ImageIcon imageIcon;
			
			if(currentPhoto.getId().equals(bufferedNextPhotoId)){
				bufferedNextPhotoId = "";
				imageIcon = bufferedNextPhoto;
			} else {
				imageIcon = fetchImageIconOfFlickrImage(currentPhoto);
			}
			
			displayMessage(PHOTO_TITLE + currentPhoto.getTitle() + buildCounterMessage(photoPointer, untaggedPhotos.size()));
			photoLabel.setIcon(imageIcon);
			currentPhoto = photosInterface.getInfo(currentPhoto.getId(),
					sharedSecret);
			tagsTextField.setText(convertTagsToString(currentPhoto.getTags()));
			SwingUtilities.invokeLater(prefetchPhoto);
			
		} else {
			tagsTextField.setEnabled(false);
			fetchPhotosButton.grabFocus();
			displayMessage(NO_UNTAGGED_PHOTOS);
		}
	}

	Runnable prefetchPhoto = new Runnable() {
	    public void run() { 
	    	int nextPhotoPointer = safelyGetNextPointerNumber();
	    	nextPhoto = (Photo) untaggedPhotos.get(nextPhotoPointer);
	    	try {
	    		System.out.println("Buffering " + nextPhoto.getTitle());
				bufferedNextPhoto = fetchImageIconOfFlickrImage(nextPhoto);
				bufferedNextPhotoId = nextPhoto.getId();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FlickrException e) {
				e.printStackTrace();
			}
	    }
	};
	
	
	private static String buildCounterMessage(int counter,
			int total) {
		return " (" + counter + " of " + total + ")";		
	}

	private void displayMessage(String message) {
		statusLabel.setText(message);
	}

	@SuppressWarnings("unchecked")
	private String convertTagsToString(Collection tags) {
		String tagString = "";
		java.util.Iterator<Tag> tagIterator = tags.iterator();
		while (tagIterator.hasNext()) {
			tagString = tagString + " " + tagIterator.next().getValue();
		}
		return tagString;
	}

	private void safelyIncrementAndWraparoundPhotoPointer() {
		photoPointer = safelyGetNextPointerNumber();
		System.out.println("Pointer is " + photoPointer);
	}

	private int safelyGetNextPointerNumber() {
		int localPointer = photoPointer; 
		
		if (photoPointer < (untaggedPhotos.size() - 1)) {
			return ++localPointer;
		} else {
			return 0;
		}
	}

	private void initPropertiesAndUI() throws ParserConfigurationException,
			IOException {

		FileInputStream propertiesFileStream = null;
		File file = new File("/setup.properties");
		rest = new REST(HOST);

		try {
			propertiesFileStream = new FileInputStream(file);
			properties = new java.util.Properties();
			properties.load(propertiesFileStream);
		} finally {
			IOUtilities.close(propertiesFileStream);
		}

		nextButton = new JButton(NEXT_ENTER);
		fetchPhotosButton = new JButton("Get new batch <F10>");
		tagsTextField = new JTextField(TEXTFIELD_SIZE);
		statusLabel = new JLabel();
		photoLabel = new javax.swing.JLabel();

		photoLabel.setSize(new Dimension(PHOTO_WIDTH, PHOTO_HEIGHT));
		photoLabel.setMaximumSize(new Dimension(PHOTO_WIDTH, PHOTO_HEIGHT));
		photoLabel.setMinimumSize(new Dimension(PHOTO_WIDTH, PHOTO_HEIGHT));
		photoLabel.setBorder(BorderFactory.createEtchedBorder());
		photoLabel.setHorizontalTextPosition(JLabel.CENTER);
		
//		photoLabel.setText("Test Test Test Test Test ");
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	}

	private void authenticateAndConnect() {
		RequestContext requestContext;
		flickr = new Flickr(properties.getProperty("apiKey"), properties
				.getProperty("secret"), rest);
		requestContext = RequestContext.getRequestContext();
		Auth auth = new Auth();
		auth.setPermission(Permission.READ);
		auth.setToken(properties.getProperty("token"));
		requestContext.setAuth(auth);
		Flickr.debugRequest = false;
		Flickr.debugStream = false;
		photosInterface = flickr.getPhotosInterface();
		tagsInterface = flickr.getTagsInterface();
	}

}
