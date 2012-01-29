package kozyr.github.com.foody.thing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import kozyr.github.com.foody.R;
import kozyr.github.com.foody.util.ImageUtilities;
import kozyr.github.com.foody.util.Util;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

/**
 * setViewValue on the viewBinder will be called for every view passed to the cursor
 * We can have custom view setup there
 * @author sergei
 *
 */
public class ThingCursorAdapter extends SimpleCursorAdapter {
	
	private static final String TAG = "ThingCursorAdapter";
	// Never make static Drawable -- it has a View as callback and will leak memory
	private static Bitmap _defaultImage = null;
	
	public ThingCursorAdapter(Activity parent, int layout, Cursor c,
			String[] from, int[] to) {
		super(parent, layout, c, from, to);
		setViewBinder(new ThingDataViewBinder()); 
		Log.d(TAG, "Created thing cursor adaptor...");
	}

	@Override
	public void setViewImage(ImageView v, String miniId) {
		Log.d(TAG, "in setViewImage: " + miniId);
		Log.d(TAG, v.toString());
		
		if (Util.isEmpty(miniId)) {
			loadDefaultImage(v);
		} else {
			loadThumbnail(v, miniId);
		}
	}

	private void loadDefaultImage(ImageView v) {
		Log.d(TAG, "Loading default image...");
		if (_defaultImage == null) {
			_defaultImage = ImageUtilities.rotateAndFrame(BitmapFactory.decodeResource(v.getResources(), R.drawable.capture_large));
			// _defaultImage = BitmapFactory.decodeResource(v.getResources(), R.drawable.capture_large);
		}
		v.setImageBitmap(_defaultImage);
	}

	private void loadThumbnail(ImageView v, String miniId) {
    	Log.i(TAG, "Loading thumbnail: " + miniId);
    	Uri thumbUri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, miniId);
    	InputStream is = null;
		try {
			is = v.getContext().getContentResolver().openInputStream(thumbUri);
			BitmapFactory.Options options=new BitmapFactory.Options();
			// options.outHeight = 145;
			options.inSampleSize = 3;
			Bitmap thumb = BitmapFactory.decodeStream(is, null, options);
			if (thumb != null) {
				v.setImageBitmap(ImageUtilities.rotateAndFrame(thumb));
				// v.setImageBitmap(thumb);
				thumb.recycle();
			} else {
				loadDefaultImage(v);
			}
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Image " + miniId + " not found, loading default image...");
			loadDefaultImage(v);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
    	
    	
	}
	
	
}
