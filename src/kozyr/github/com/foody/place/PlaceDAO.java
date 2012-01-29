package kozyr.github.com.foody.place;

import kozyr.github.com.foody.DbManager;
import kozyr.github.com.foody.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class PlaceDAO {
	
	public static final String KEY_NAME = "name";
	public static final String KEY_REVIEW = "review";
	public static final String KEY_RATING = "rating";
	public static final String KEY_ROWID = "_id";
	
	public static final String [] ALL_COLUMNS = new String[] {
		KEY_ROWID,
		KEY_NAME,
		KEY_REVIEW,
		KEY_RATING
	};
	
	private Context _ctx;
	
	public PlaceDAO(Context ctx) {
		_ctx = ctx;
	}
	
	public long createPlace(String name, String review) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_REVIEW, review);
		
		return DbManager.getInstance(_ctx).insertData(DbManager.TABLE_PLACES, initialValues);
	}
	
	public boolean deletePlace(long rowId) {
		return DbManager.getInstance(_ctx).deleteData(DbManager.TABLE_PLACES, KEY_ROWID + "=" + rowId);
	}
	
	private Cursor fetchAllPlaces() {
		return DbManager.getInstance(_ctx).fetchAll(DbManager.TABLE_PLACES, ALL_COLUMNS);
	}
	
	public SimpleCursorAdapter fetchAllPlaces(Activity parent) {
		Cursor placesCursor = fetchAllPlaces();
        parent.startManagingCursor(placesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[] { KEY_NAME };
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] {R.id.place_item_view};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter places = new SimpleCursorAdapter(parent, R.layout.place_list_item, placesCursor, from, to);
        return places;
	}
	
	public boolean updatePlace(long rowId, String name, String review, int rating) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_REVIEW, review);
		args.put(KEY_RATING, rating);
		
		return DbManager.getInstance(_ctx).updateData(DbManager.TABLE_PLACES, args, KEY_ROWID + "=" + rowId);
	}
	
	public Place getPlaceById(long rowId, Activity parent) {
		Place place = null;
		Cursor placeCursor = DbManager.getInstance(_ctx).fetchData(
				DbManager.TABLE_PLACES, 
				ALL_COLUMNS,
				KEY_ROWID + "=" + rowId);
		 parent.startManagingCursor(placeCursor);
		 
		if (placeCursor != null && placeCursor.getCount() > 0) {
	        
	        place = new Place(
	        		placeCursor.getLong(placeCursor.getColumnIndexOrThrow(KEY_ROWID)),
	        		placeCursor.getString(placeCursor.getColumnIndexOrThrow(KEY_NAME)),
	        		placeCursor.getString(placeCursor.getColumnIndexOrThrow(KEY_REVIEW)),
	        		placeCursor.getInt(placeCursor.getColumnIndexOrThrow(KEY_RATING))
	        );
		}
        
        return place;
	}
	
 }
