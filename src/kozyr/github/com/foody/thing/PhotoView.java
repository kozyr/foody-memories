package kozyr.github.com.foody.thing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import kozyr.github.com.foody.R;
import kozyr.github.com.foody.util.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

public class PhotoView extends Activity {

	private static final String TAG = "PhotoView";
	public static final String PHOTO_ID = "PHOTO_ID";
	// Need handler for callbacks to the UI thread
    private final Handler mHandler = new Handler();
    private Bitmap _photoBitmap;
    private ProgressDialog _progressDialog;
	
    // Create runnable for posting
    private final Runnable _updateImageView = new Runnable() {
        public void run() {
        	if (_photoBitmap != null) {
        		ImageView photoView = (ImageView) findViewById(R.id.big_photo);
        		photoView.setImageBitmap(_photoBitmap);
        	}
        	if (_progressDialog != null) {
    			_progressDialog.dismiss();
    			_progressDialog = null;
    		}
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_photo);
		
		Uri photoUri = (Uri) getIntent().getExtras().get(PHOTO_ID);
		
		_progressDialog = ProgressDialog.show(this, "Hold on a sec", "Loading photo...", true);
		loadImage(photoUri);
	}

	private Bitmap scaleImage(Uri photoUri) {
		InputStream is = null;
		
		try {
			is = getContentResolver().openInputStream(photoUri);
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = calculateSampleSize(photoUri);
			return BitmapFactory.decodeStream(is, null, options);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Full-size image not found", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		
		return null;
	}

    private int calculateSampleSize(Uri photoUri) {
    	int sampleSize = 1;
    	
    	String[] projection = {
				MediaStore.Images.ImageColumns._ID,  // The columns we want
				MediaStore.Images.ImageColumns.SIZE
				};
		String selection = MediaStore.Images.Media._ID + " = " + Util.getPhotoId(photoUri.toString());
		
		Cursor c = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
		
		if (c != null && c.moveToFirst()) {
			long size = c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
			Log.d(TAG, "Photo size: " + size);
			if (size > 500000) {
				sampleSize = 8;
			} else if (size > 250000) {
				sampleSize = 4;
			} else if (size > 125000) {
				size = 2;
			}
		}
		
		return sampleSize;		
	}

	protected void loadImage(final Uri photoUri) {

        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	if (_photoBitmap != null) {
            		_photoBitmap.recycle();
            	}
                _photoBitmap = scaleImage(photoUri);
                mHandler.post(_updateImageView);
            }
        };
        t.start();
    }
}
