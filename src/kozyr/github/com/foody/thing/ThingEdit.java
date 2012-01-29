package kozyr.github.com.foody.thing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import kozyr.github.com.foody.Constants;
import kozyr.github.com.foody.R;
import kozyr.github.com.foody.photo.PhotoDAO;
import kozyr.github.com.foody.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;

public class ThingEdit extends Activity  {

	private static final int ACTIVITY_TAKE_PHOTO = 1;
	private static final int ACTIVITY_BROWSE_PHOTO = 2;
	
	private static final String TAG = "ThingEdit"; 
	private static final String MEMORIZER_TMP_FILE = "memorizer_tmp";
	
	private EditText _nameText;
    private EditText _reviewText;
    private RatingBar _ratingBar;
    private ImageButton _imageButton;
    
    private long _thingId;
    private long _placeId; 
    
    private ThingDAO _thingDAO;
    private PhotoDAO _photoDAO;
    
    boolean _isImageFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.thing_edit);
        Util.appendToTitle(this, "Food");
        initDB();
        initFields();
        // sizeUpImageButton();
        restoreState(savedInstanceState);
        restoreNonConfigurationInstance();
        
		populateFields();
        createId();
        
        Log.d(TAG, "ThingID: " + _thingId);
    }

	private void restoreNonConfigurationInstance() {
    	final Object data = getLastNonConfigurationInstance();
    	if (data != null) {
    		Long thingId = (Long) data;
    		_thingId = thingId.longValue();
    	}
	}

	private void initDB() {
        _thingDAO = new ThingDAO(this);
        _photoDAO = new PhotoDAO(this);
    }
    
    private void initFields() {
        _nameText = (EditText) findViewById(R.id.thing_name);
        _reviewText = (EditText) findViewById(R.id.thing_review);
        _ratingBar = (RatingBar) findViewById(R.id.thing_rating);
        _imageButton = (ImageButton) findViewById(R.id.thing_image_view);
        
        _nameText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				Button confirmButton = (Button) findViewById(R.id.thing_confirm);
				confirmButton.setEnabled(s != null && s.length() > 0);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}
        	
        });
      
        Button confirmButton = (Button) findViewById(R.id.thing_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        	    setResult(RESULT_OK);
        	    finish();
        	}
        });
        
        Button shareButton = (Button) findViewById(R.id.thing_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				shareThing();
			}
		});
        
        _imageButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        	    if (!_isImageFound) {
        	    	addPhoto();
        	    } else {
        	    	viewPhoto();
        	    }
        	}
        });
        registerForContextMenu(_imageButton);
    }
    
    @Override
    // retain thingId
    public Object onRetainNonConfigurationInstance() {
    	return _thingId;
    }
    
    private void shareThing() {
    	long photoId = _photoDAO.getPhotoIdByThing(this, _thingId);
    	if (Util.isValidId(photoId)) {
	    	Intent shareIntent = new Intent(Intent.ACTION_SEND); 
	        shareIntent.putExtra(Intent.EXTRA_SUBJECT, _nameText.getText().toString());
	        shareIntent.putExtra(Intent.EXTRA_TEXT, _nameText.getText() + ":\n" + _reviewText.getText());
	        shareIntent.putExtra(Intent.EXTRA_STREAM, Util.getPhotoUri(photoId));
	        shareIntent.setType("image/jpg");
	        
	        startActivity(Intent.createChooser(shareIntent, "Share"));
    	} else {
    		Util.showToast(this, "No photo - nothing to share...");
    	}
	}

	private void viewPhoto() {
    	long photoId = _photoDAO.getPhotoIdByThing(this, _thingId);
    	if (Util.isValidId(photoId)) {
    		Uri photoURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(photoId));
    		// startDefaultPhotoView(photoURI);
    		startMyPhotoView(photoURI);
    	}
    }
	
	private void onBrowsePhoto() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		startActivityForResult(Intent.createChooser(intent, "Choose a Viewer"), ACTIVITY_BROWSE_PHOTO); 
	}
    
    private void startDefaultPhotoView(Uri photoURI) {
    	Intent intent = new Intent();
    	intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(photoURI, "image/*");
		startActivity(intent);
    }
    
    private void startMyPhotoView(Uri photoURI) {
    	Intent intent = new Intent(this, PhotoView.class);
    	intent.putExtra(PhotoView.PHOTO_ID, photoURI);
    	startActivity(intent);
    }
    
    private void onDeletePhoto() {
    	confirmDeletion();
    	deletePhotoFromStorage();
    	_photoDAO.deletePhotos(_thingId);
    	showPhoto();
	}

	private void deletePhotoFromStorage() {
		long photoId = _photoDAO.getPhotoIdByThing(this, _thingId);
		Util.deletePhoto(getContentResolver(), photoId);
	}

	private void confirmDeletion() {
		
	}

	@Override
    protected void onPause() {
    	Log.w(TAG, "onPause()");
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ThingDAO.KEY_ROWID, _thingId);
    }
    
    private void saveState() {
    	if (Util.isValidId(_thingId)) {
    		Log.w(TAG, "Saving state for: " + _thingId);
	        String name = _nameText.getText().toString();
	        String review = _reviewText.getText().toString();
	        int rating = (int) (_ratingBar.getRating()*Constants.RATING_MULTIPLIER);
	
	        _thingDAO.updateThing(_thingId, name, review, rating);
    	}
    }

	private void restoreState(Bundle savedInstanceState) {
        _thingId = savedInstanceState != null ? savedInstanceState.getLong(ThingDAO.KEY_ROWID, -1) : -1;
		if (!Util.isValidId(_thingId)) {
			Bundle extras = getIntent().getExtras();    
			_placeId = extras != null ? extras.getLong(ThingDAO.KEY_PLACE, -1) : -1;
			_thingId = extras != null ? extras.getLong(ThingDAO.KEY_ROWID, -1) : -1;
		}
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy()");
    	super.onDestroy();
    }
    
    @Override
    protected void onStop() {
    	Log.d(TAG, "onStop()");
    	super.onStop();
    }
    
    
    
    private void populateFields() {
        if (Util.isValidId(_thingId)) {
	    	Thing thing = _thingDAO.getThingById(_thingId, this);
	    	if (thing != null) {
		    	_nameText.setText(thing.getName());
		    	_reviewText.setText(thing.getReview());
		    	_ratingBar.setRating(thing.getRating()/Constants.RATING_MULTIPLIER);
		    	_placeId = thing.getPlaceId();
	    	}
	    	showPhoto();
        }
    }
    
    private void showPhoto() {
    	long miniThumbId = _photoDAO.getMiniIdByThing(this, _thingId);
    	
    	_isImageFound = miniThumbId > 0;
    	if (_isImageFound) {
    		Uri thumbUri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(miniThumbId));
    		// _imageButton.setImageURI(thumbUri);
    		InputStream is = null;
    		try {
    			is = getContentResolver().openInputStream(thumbUri);
    			Bitmap thumb = BitmapFactory.decodeStream(is);
    			_imageButton.setImageBitmap(thumb);
    			
                // View imageBorder = findViewById(R.id.photo_border_layout);
                // imageBorder.setBackgroundResource(R.drawable.photo_border);
    		} catch (FileNotFoundException e) {
    			Log.d(TAG, "Image " + miniThumbId + " not found, loading default image...");
    			// get rid of photos for this image, they probably were deleted elsewhere
    			_photoDAO.deletePhotos(_thingId);
    			_isImageFound = false;
    			loadDefaultImage();
    		} finally {
    			if (is != null) {
    				try {
    					is.close();
    				} catch (IOException e) {
    					Log.e(TAG, e.getMessage());
    				}
    			}
    		}
    	} else {
    		loadDefaultImage();
    	}
		updateButtonState();
	}

	private void loadDefaultImage() {
    	_imageButton.setImageDrawable(getResources().getDrawable(R.drawable.capture_large));
    	// View imageBorder = findViewById(R.id.photo_border_layout);
        // imageBorder.setBackgroundResource(0);
        
		_imageButton.refreshDrawableState();
	}

	private void updateButtonState() {
		 Button shareButton = (Button) findViewById(R.id.thing_share);
		 shareButton.setEnabled(_isImageFound);
	}
    
    private boolean createId() {
    	Log.d(TAG, "createId()");
    	boolean created = false;
    	String name = _nameText.getText().toString();
        String review = _reviewText.getText().toString();
        int rating = (int) (_ratingBar.getRating()*Constants.RATING_MULTIPLIER);
        
        if (!Util.isValidId(_thingId)) {
        	long id = _thingDAO.createThing(_placeId, name, review, rating);
            if (id > 0) {
                _thingId = id;
                created = true;
            }
        }
        
        return created;
    }
    
    private void addPhoto() {
    	Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);	
    	// Work around http://code.google.com/p/android/issues/detail?id=1480
    	// We save the image to tmp file because camera API is buggy
    	i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTmpPhotoFile())); 
		startActivityForResult(i, ACTIVITY_TAKE_PHOTO);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d(TAG, "Result code = " + resultCode);

		if (resultCode == RESULT_CANCELED) {
			Util.showToast(this, "Photo shoot cancelled...");
			return;
		}
		switch (requestCode) {

			case ACTIVITY_TAKE_PHOTO:
				saveImageAndThumbnails();
				showPhoto();
				break;
				
			case ACTIVITY_BROWSE_PHOTO:	
				Bundle extras = intent.getExtras();
				addPhotoToDb(intent.getData());
				showPhoto();
				break;
		}
	}
	
	private void addPhotoToDb(Uri photoUri) {
		Log.i(TAG, "Photo Uri: " + photoUri);
		saveThumbnails(photoUri.toString());
	}

	private File getTmpPhotoFile() {
		return new File(Environment.getExternalStorageDirectory(), MEMORIZER_TMP_FILE);
	}
	
	private void saveImageAndThumbnails() {
		// Save the image.  This also saves a micro and mini thumbnail
		// if (bm != null) showToast(this, "Width: "  + bm.getWidth() + ", Height: " + bm.getHeight());
		File tmpPhoto = getTmpPhotoFile();
		String sUri = null;
		try {
			sUri = MediaStore.Images.Media.insertImage(getContentResolver(), tmpPhoto.getAbsolutePath(), _nameText.getText().toString(), _nameText.getText().toString());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Didn't find the photo file: " + e);
//			if (bm != null) {
//				sUri = MediaStore.Images.Media.insertImage(getContentResolver(), bm, _nameText.getText().toString(), _nameText.getText().toString());
//			} else {
				Util.showToast(this, "Acquiring image failed...");
				return;
			//}
		}
		
		Log.d(TAG, "Image URI: " + sUri);
		
		saveThumbnails(sUri);
	}

	private void saveThumbnails(String sUri) {
		// Now update the mini Thumbnail record with the image's ID
		long imgId = Util.getPhotoId(sUri);// Strip off the Id num
		Uri thumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;      // Thumbnail provider
		
		// Query for the mini thumbnail for the image just saved. There should be just 1.
		// See the comments on showGallery() for an explanation of managedQuery()
		String[] projection = {
				MediaStore.Images.ImageColumns._ID,  // The columns we want
				MediaStore.Images.Thumbnails.IMAGE_ID,  
				MediaStore.Images.Thumbnails.KIND,
				};
		String selection = 
			MediaStore.Images.Thumbnails.KIND + " IN ("  +  
			MediaStore.Images.Thumbnails.MINI_KIND + ", " + MediaStore.Images.Thumbnails.MICRO_KIND + ")"  
			+ " AND " + MediaStore.Images.Thumbnails.IMAGE_ID + " = " + imgId;
		
		Cursor c = this.managedQuery(thumbUri, projection, selection, null, null);
		int miniId = 0;
		int microId = 0;
		
		if (c.moveToFirst()) {
			do {
				int thumbKind = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.KIND));		
				
				int thumbId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
				if (thumbKind == MediaStore.Images.Thumbnails.MICRO_KIND) {
					microId = thumbId;
				} else {
					miniId = thumbId;
				}		
			} while (c.moveToNext());
		}
		
		// Now, save the image location in PhotoDAO
		removeOldPhoto();
		_photoDAO.addPhoto(imgId, _thingId, miniId, microId);
	}

	private void removeOldPhoto() {
		deletePhotoFromStorage();
		_photoDAO.deletePhotos(_thingId);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.thing_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        
        boolean itookit = onMenuClick(item);
        return itookit ? itookit : super.onMenuItemSelected(featureId, item);
    }
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
        if (_isImageFound) {
	    	super.onCreateContextMenu(menu, v, menuInfo);
	
	        menu.setHeaderTitle("Manage Photo");
	        getMenuInflater().inflate(R.menu.thing_edit, menu);
        }
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	boolean itookit = onMenuClick(item);
    	return itookit ? itookit : super.onContextItemSelected(item);
	}

	private boolean onMenuClick(MenuItem item) {
		switch(item.getItemId()) {
	    	case R.id.context_menu_thing_update_photo:
	    		addPhoto();
		        return true;
    	    case R.id.context_menu_thing_delete_photo:
    	    	onDeletePhoto();
    	    	return true;
    	    case R.id.context_menu_thing_select_gallery:
    	    	onBrowsePhoto();
    	    	return true;
    	}
		
		return false;
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
           creationCancelled();
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
        	addPhoto();
        	return true;
        }

        return super.onKeyDown(keyCode, event);
    }

	private void creationCancelled() {
		String name = _nameText.getText().toString();
           if (Util.isEmpty(name)) {
        	   // check the name in the db, if it's not empty, delete
        	   Thing thing = _thingDAO.getThingById(_thingId, this);
        	   String currentName = null;
        	   if (thing != null) {
        		   currentName = thing.getName();
        	   }
        	   if (Util.isEmpty(currentName)) {
	        	   deletePhotoFromStorage();
	        	   _thingDAO.deleteThing(_thingId);
	        	   _thingId = -1;
        	   } else {
        		   _nameText.setText(currentName);
        	   }
           }
	}

}
