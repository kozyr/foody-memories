package kozyr.github.com.foody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.util.Log;

public class DbManager {

	private static final String TAG = "DbManager";
	
	public static final String TABLE_PLACES = "places";
	public static final String TABLE_THINGS = "things";
	public static final String TABLE_PHOTOS = "photos";

	/**
	 * Database creation sql statement
	 */
	private static final String PLACES_CREATE =
		"create table " + TABLE_PLACES + " (_id integer primary key autoincrement, "
		+ "name text not null, review text, rating integer, lastModified date, createdOn date);";
	
	private static final String THINGS_CREATE = 
		"create table " + TABLE_THINGS + " (_id integer primary key autoincrement," +
		"name text not null, review text, rating integer, lastModified date, createdOn date," +
		"placeId integer not null);";
	
	private static final String PHOTOS_CREATE = 
		"create table " + TABLE_PHOTOS + " (_id integer primary key autoincrement," + 
		"location integer not null, thingId not null, miniId integer not null, microId integer not null);";
	
	private static final String THINGS_INSERT_TRIGGER = 
	"CREATE TRIGGER things_add_created_on AFTER INSERT ON " +  TABLE_THINGS +
	 " BEGIN " +
	    " UPDATE " + TABLE_THINGS + " SET createdOn = datetime('now','localtime') WHERE _id = new._id; " +
	 " END; ";
	
	private static final String THINGS_UPDATE_TRIGGER = 
		"CREATE TRIGGER things_update_last_modified AFTER UPDATE ON " + TABLE_THINGS + 
		 " BEGIN " +
		    " UPDATE " + TABLE_THINGS + " SET lastModified = datetime('now','localtime') WHERE _id = new._id; " +
		 " END; ";


	private static final String DATABASE_NAME = "memorizer";
	private static final int DATABASE_VERSION = 10;

	private DatabaseHelper _dbHelper;
	private SQLiteDatabase _db;
	private final Context _context;
	
	private static DbManager _instance;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(PLACES_CREATE);
			db.execSQL(THINGS_CREATE);
			db.execSQL(PHOTOS_CREATE);
			db.execSQL(THINGS_INSERT_TRIGGER);
			db.execSQL(THINGS_UPDATE_TRIGGER);
			Log.w(TAG, "Recreated DB");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which might destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_THINGS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
			onCreate(db);
		}
		
		
	}

	private DbManager(Context ctx) {
		this._context = ctx;
	}
	
	private static void copyFileToFile(final File src, final File dest) throws IOException
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);
			final byte[] buffer = new byte[1024];
			int n;
			while ((n = in.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}
		}
		finally
		{
			try {
				if (in != null) {
					in.close();
				} 
			} catch (Exception e) {

			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {

			}
		}
	}
	
	private File getDumpFile() {
		return new File(Environment.getExternalStorageDirectory(), DATABASE_NAME + ".db");
	}
	
	public void dumpDB() throws IOException {
		File dbPath = _context.getDatabasePath(DATABASE_NAME);
		File dst = getDumpFile();
		copyFileToFile(dbPath, dst);
		Log.i(TAG, dbPath.getAbsolutePath() + " dumped to " + dst);
	}
	
	public boolean loadDB() throws IOException {
		File dbPath = _context.getDatabasePath(DATABASE_NAME);
		// _dbHelper.close();
		File src = getDumpFile();
		if (src.exists()) {
			copyFileToFile(src, dbPath);
			// open();
			return true;
		}
		
		return false;
	}

	private DbManager open() throws SQLException {
		_dbHelper = new DatabaseHelper(_context);
		_db = _dbHelper.getWritableDatabase();
		return this;
	}
	
	private void reopen() {
		_db = _dbHelper.getWritableDatabase();
	}
	
	public static DbManager getInstance(Context ctx) {
		if (_instance == null) {
			_instance = new DbManager(ctx);
			_instance.open();
		}
		
		_instance.reopen();
		return _instance;
	}

	public void close() {
		_dbHelper.close();
		_instance = null;
	}

	public long insertData(String table, ContentValues values) {
		return _db.insert(table, null, values);
	}
	
	public boolean deleteData(String table, String matcher) {
		return _db.delete(table, matcher, null) > 0;
	}

	public Cursor fetchAll(String table, String [] columns) {
		return _db.query(table, columns, null, null, null, null, null);
	}
	
	public Cursor fetchData(String table, String [] columns, String matcher, String orderBy) throws SQLException {
		return _db.query(table, columns, matcher, null, null, null, orderBy);
	}

	public Cursor fetchData(String table, String [] columns, String matcher) throws SQLException {
		Cursor mCursor = _db.query(true, table, columns, matcher, null, null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor joinedFetch(String tables, String [] columns, String matcher, String orderBy) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(tables);
		return builder.query(_db, columns, matcher, null, null, null, orderBy);
	}
	
	public boolean updateData(String table, ContentValues values, String matcher) {
		return _db.update(table, values, matcher, null) > 0;
	}
	
	public void beginTransaction() {
		_db.beginTransaction();
	}
	
	public void commitTransaction() {
		try {
			_db.setTransactionSuccessful();
		} finally {
			_db.endTransaction();
		}
	}
	
	public void rollbackTransaction() {
		_db.endTransaction();
	}
	
	public void rollbackIfInTransaction() {
		if (_db.inTransaction()) {
			_db.endTransaction();
		}
	}

	public static void destroy() {
		if (_instance != null) {
			_instance.close();
		}
	}
}
