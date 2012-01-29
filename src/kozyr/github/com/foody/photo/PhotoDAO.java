package kozyr.github.com.foody.photo;

import kozyr.github.com.foody.DbManager;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PhotoDAO {
	/**
	 * Photo types. IMPORTANT: Enum that's stored in the database -- only add things to the bottom of the list
	 * 
	 */
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_MINI_ID = "miniId";
	public static final String KEY_MICRO_ID = "microId";
	public static final String KEY_PHOTO_THING_ID = "thingId";
	
	public static final String[] ALL_COLUMNS = new String[] {
		KEY_ROWID,
		KEY_LOCATION,
		KEY_PHOTO_THING_ID,
		KEY_MINI_ID,
		KEY_MICRO_ID
	};
	
	private Context _ctx;
	
	public PhotoDAO(Context ctx) {
		_ctx = ctx;
	}
	
	public long addPhoto(long imageId, long thingId, long miniId, long microId) {
		ContentValues values = new ContentValues();
		values.put(KEY_LOCATION, imageId);
		values.put(KEY_PHOTO_THING_ID, thingId);
		values.put(KEY_MINI_ID, miniId);
		values.put(KEY_MICRO_ID, microId);
		return DbManager.getInstance(_ctx).insertData(DbManager.TABLE_PHOTOS, values);
	}
	
	public void deletePhoto(long photoId) {
		DbManager.getInstance(_ctx).deleteData(DbManager.TABLE_PHOTOS, KEY_ROWID + "=" + photoId);
	}

	public void deletePhotos(long thingId) {
		DbManager.getInstance(_ctx).deleteData(DbManager.TABLE_PHOTOS, KEY_PHOTO_THING_ID + "=" + thingId);
	}
	
	public Cursor getPhotoFieldByThing(Activity parent, long thingId) {
		 Cursor cursor = DbManager.getInstance(_ctx).fetchData(DbManager.TABLE_PHOTOS, ALL_COLUMNS, KEY_PHOTO_THING_ID + "=" + thingId);
		 parent.startManagingCursor(cursor);
		 
		 return cursor;
	}
	
	public Cursor getPhotoFieldByPhoto(Activity parent, long mainPhotoId) {
		 Cursor cursor = DbManager.getInstance(_ctx).fetchData(DbManager.TABLE_PHOTOS, ALL_COLUMNS, KEY_LOCATION + "=" + mainPhotoId);
		 parent.startManagingCursor(cursor);
		 
		 return cursor;
	}
	
	public long getMiniIdByThing(Activity parent, long thingId) {
		Cursor cursor = getPhotoFieldByThing(parent, thingId);
		long miniId = -1;
		if (cursor != null && cursor.getCount() > 0) {
			miniId = cursor.getLong(cursor.getColumnIndex(KEY_MINI_ID));
		}
		
		return miniId;
	}
	
	public long getMiniIdByPhoto(Activity parent, long photoId) {
		Cursor cursor = getPhotoFieldByPhoto(parent, photoId);
		long miniId = -1;
		if (cursor != null && cursor.getCount() > 0) {
			miniId = cursor.getLong(cursor.getColumnIndex(KEY_MINI_ID));
		}
		
		return miniId;
	}

	public long getMicroIdByPhoto(Activity parent, long photoId) {
		Cursor cursor = getPhotoFieldByPhoto(parent, photoId);
		long microId = -1;
		if (cursor != null && cursor.getCount() > 0) {
			microId = cursor.getLong(cursor.getColumnIndex(KEY_MICRO_ID));
		}
		
		return microId;
	}
	
	public long getPhotoIdByThing(Activity parent, long thingId) {
		Cursor cursor = getPhotoFieldByThing(parent, thingId);
		long photoId = -1;
		if (cursor != null && cursor.getCount() > 0) {
			photoId = cursor.getLong(cursor.getColumnIndex(KEY_LOCATION));
		}
		return photoId;
	}
}
