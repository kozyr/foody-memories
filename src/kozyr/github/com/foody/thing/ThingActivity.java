package kozyr.github.com.foody.thing;

import kozyr.github.com.foody.R;
import kozyr.github.com.foody.photo.PhotoDAO;
import kozyr.github.com.foody.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ThingActivity extends Activity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final String TAG = ThingActivity.class.getSimpleName();
    private static final String SHARE_AS = "Share...";
    
    private Long _placeId; 

    private ThingDAO _thingsDAO;
    private PhotoDAO _photoDAO;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        loadPlace();
        
        setContentView(R.layout.thing_grid);
        Util.appendToTitle(this, "Menu");
       
        _thingsDAO = new ThingDAO(this);
        _photoDAO = new PhotoDAO(this);
        
        fillData();
        
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
        {
            public void onItemClick(AdapterView parent, View v, int position, long id) {      
            	editThing(id);
            }
        }); 

        View emptyView = LayoutInflater.from(this).inflate(R.layout.thing_empty_grid, null);
        addContentView(emptyView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        gridView.setEmptyView(emptyView);

        registerForContextMenu(gridView);
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume()");
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause()");
    	super.onPause();
    }
    
    @Override
    protected void onStart() {
    	Log.d(TAG, "onStart()");
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	Log.d(TAG, "onStop()");
    	super.onStop();
    }
    
    
    @Override
    protected void onDestroy() {
    	GridView gridView = (GridView) findViewById(R.id.gridview);
    	if (gridView != null) {
    		gridView.setOnItemClickListener(null);
    		gridView.setAdapter(null);
    	}
    	super.onDestroy();
    	Log.d(TAG, "onDestroy()");
    }
    
    private void loadPlace() {
    	Bundle extras = getIntent().getExtras();    
		_placeId = extras != null ? extras.getLong(ThingDAO.KEY_PLACE) : null;
    }
    
    private void fillData() {
    	if (_placeId != null) {
    		GridView gridview = (GridView) findViewById(R.id.gridview);
	        gridview.setAdapter(_thingsDAO.fetchAllThings(this, _placeId));
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.thing_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
	        case R.id.menu_thing_add:
	            createThing();
	            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Need to find right view here...
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(((TextView)info.targetView.findViewById(R.id.thing_list_name)).getText());

        getMenuInflater().inflate(R.menu.thing, menu);
        
        long photoId = _photoDAO.getPhotoIdByThing(this, ((AdapterContextMenuInfo) menuInfo).id);
        if (Util.isValidId(photoId)) {
        	menu.add(SHARE_AS);
        }
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch(item.getItemId()) {
	    	case R.id.context_menu_thing_delete:
	    		deleteThing(info.id);
		        return true;
    	    case R.id.context_menu_thing_edit:
    	    	editThing(info.id);
    	    	return true;
    	}
    	if (SHARE_AS.equals(item.getTitle())) {
    		shareThing(info.id);
    	}
		return super.onContextItemSelected(item);
	}
    
    private void shareThing(long id) {
    	long photoId = _photoDAO.getPhotoIdByThing(this, id);
    	if (Util.isValidId(photoId)) {
    		Thing thing = _thingsDAO.getThingById(id, this);
	    	Intent shareIntent = new Intent(Intent.ACTION_SEND); 
	        shareIntent.putExtra(Intent.EXTRA_SUBJECT, thing.getName());
	        shareIntent.putExtra(Intent.EXTRA_TEXT, thing.getName() + ":\n" + thing.getReview());
	        shareIntent.putExtra(Intent.EXTRA_STREAM, Util.getPhotoUri(photoId));
	        shareIntent.setType("image/jpg");
	        
	        startActivity(Intent.createChooser(shareIntent, "Share"));
    	} else {
    		Util.showToast(this, "No photo - nothing to share...");
    	}
	}

	private void deleteThing(long id) {
    	long photoId = _photoDAO.getPhotoIdByThing(this, id);
    	Util.deletePhoto(getContentResolver(), photoId);
    	_photoDAO.deletePhotos(id);
    	_thingsDAO.deleteThing(id);
        fillData();
    }
	
    private void createThing() {
        Intent i = new Intent(this, ThingEdit.class);
        i.putExtra(ThingDAO.KEY_PLACE, _placeId);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    private void editThing(long id) {
    	 Intent i = new Intent(this, ThingEdit.class);
         i.putExtra(ThingDAO.KEY_ROWID, id);
         startActivityForResult(i, ACTIVITY_EDIT);
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.requestLayout();
    }*/
}
