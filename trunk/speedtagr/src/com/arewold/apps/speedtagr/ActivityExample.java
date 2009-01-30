package com.arewold.apps.speedtagr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.activity.ActivityInterface;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.util.IOUtilities;

/**
 * Demonstration of howto use the ActivityInterface.
 *
 * @author mago
 * @version $Id: ActivityExample.java,v 1.3 2008/07/05 22:19:48 x-mago Exp $
 */
public class ActivityExample {
    static String apiKey;
    static String sharedSecret;
    Flickr flickr;
    REST rest;
    RequestContext requestContext;
    Properties properties = null;

    public ActivityExample()
      throws ParserConfigurationException, IOException {

		FileInputStream f1 = null;
		File file = new File("/setup.properties");
		try {
			f1 = new FileInputStream(file);
			properties = new java.util.Properties();
			properties.load(f1);
		} finally {
			IOUtilities.close(f1);
		}
    	
    	flickr = new Flickr(
            properties.getProperty("apiKey"),
            properties.getProperty("secret"),
            new REST()
        );
        requestContext = RequestContext.getRequestContext();
        Auth auth = new Auth();
        auth.setPermission(Permission.READ);
        auth.setToken(properties.getProperty("token"));
        requestContext.setAuth(auth);
        Flickr.debugRequest = false;
        Flickr.debugStream = false;
    }

    public void showActivity() throws FlickrException, IOException, SAXException {     
        PhotosInterface photosInterface = flickr.getPhotosInterface();
        PhotoList untaggedPhotos = photosInterface.getUntagged(10, 0);
        
        for (int j = 0; j < untaggedPhotos.size(); j++) {
        	System.out.println( ((Photo)untaggedPhotos.get(j)).getTitle());
        }
    }

    public static void main(String[] args) {
        try {
            ActivityExample t = new ActivityExample();
            t.showActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

}
