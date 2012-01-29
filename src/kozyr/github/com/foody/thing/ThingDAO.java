package kozyr.github.com.foody.thing;

import kozyr.github.com.foody.DbManager;
import kozyr.github.com.foody.R;
import kozyr.github.com.foody.photo.PhotoDAO;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ThingDAO {
	
	private static final String TAG = "ThingDAO";
	
	public static final String KEY_NAME = "name";
	public static final String KEY_REVIEW = "review";
	public static final String KEY_RATING = "rating";
	public static final String KEY_PLACE = "placeId";
	public static final String KEY_LAST_MODIFIED = "lastModified";
	public static final String KEY_CREATED_ON = "createdOn";
	public static final String KEY_ROWID = "_id"; 
	
	public static final String [] ALL_COLUMNS = new String[] {
		KEY_ROWID,
		KEY_NAME,
		KEY_REVIEW,
		KEY_RATING,
		KEY_LAST_MODIFIED,
		KEY_CREATED_ON,
		KEY_PLACE
	};
	
	public static final String [] LIST_COLUMNS = new String[] {
		DbManager.TABLE_THINGS + "." + KEY_ROWID,
		KEY_NAME,
		KEY_RATING,
		KEY_LAST_MODIFIED,
		PhotoDAO.KEY_PHOTO_THING_ID,
		PhotoDAO.KEY_MINI_ID,
	};
	
	Context _ctx;
	
	public ThingDAO(Context parent) {
		_ctx = parent;
	}
	
	public long createThing(long placeId, String name, String review, int rating) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_REVIEW, review);
		initialValues.put(KEY_RATING, rating);
		initialValues.put(KEY_PLACE, placeId);
		
		return DbManager.getInstance(_ctx).insertData(DbManager.TABLE_THINGS, initialValues);
	}
	
	public boolean deleteThing(long rowId) {
		return DbManager.getInstance(_ctx).deleteData(DbManager.TABLE_THINGS, KEY_ROWID + "=" + rowId);
	}
	
	private Cursor fetchAllThings(long placeId) {
		return DbManager.getInstance(_ctx).joinedFetch(
				DbManager.TABLE_THINGS + " LEFT OUTER JOIN " + DbManager.TABLE_PHOTOS + " ON (" + DbManager.TABLE_THINGS + "." + KEY_ROWID + " = " + PhotoDAO.KEY_PHOTO_THING_ID + ")", 
				LIST_COLUMNS,
				KEY_PLACE + "=" + placeId,
				KEY_NAME + " ASC");
	}
 	
	public ThingCursorAdapter fetchAllThings(Activity parent, long placeId) {
		Cursor thingsCursor = fetchAllThings(placeId);
        parent.startManagingCursor(thingsCursor);
        
        Log.i(TAG, "number of results: " + thingsCursor.getCount());
        // Create an array to specify the fields we want to display in the list 
        String[] from = new String[] { KEY_NAME, KEY_RATING, PhotoDAO.KEY_MINI_ID };
        
        // and an array of the fields we want to bind those fields to 
        int[] to = new int[] { R.id.thing_list_name, R.id.thing_list_rating, R.id.thing_list_photo };
        
        // Now create a simple cursor adapter and set it to display
        ThingCursorAdapter things = new ThingCursorAdapter(parent, R.layout.thing_grid_item, thingsCursor, from, to);
        return things;
	}
	
	public boolean updateThing(long rowId, String name, String review, int rating) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_REVIEW, review);
		args.put(KEY_RATING, rating);
		
		return DbManager.getInstance(_ctx).updateData(DbManager.TABLE_THINGS, args, KEY_ROWID + "=" + rowId);
	}
	
	public Thing getThingById(long rowId, Activity parent) {
		Thing thing = null;
		
		Cursor thingCursor = DbManager.getInstance(_ctx).fetchData(
				DbManager.TABLE_THINGS, 
				ALL_COLUMNS,
				KEY_ROWID + "=" + rowId);
		parent.startManagingCursor(thingCursor);
		
		if (thingCursor != null && thingCursor.getCount() > 0) {
	        
	        thing = new Thing(
	        		thingCursor.getLong(thingCursor.getColumnIndexOrThrow(KEY_ROWID)),
	        		thingCursor.getString(thingCursor.getColumnIndexOrThrow(KEY_NAME)),
	        		thingCursor.getString(thingCursor.getColumnIndexOrThrow(KEY_REVIEW)),
	        		thingCursor.getInt(thingCursor.getColumnIndexOrThrow(KEY_RATING)),
	        		thingCursor.getString(thingCursor.getColumnIndexOrThrow(KEY_LAST_MODIFIED)),
	        		thingCursor.getString(thingCursor.getColumnIndexOrThrow(KEY_CREATED_ON)),
	        		thingCursor.getLong(thingCursor.getColumnIndex(KEY_PLACE))
	        );
		}
        
        return thing;
	}
}
