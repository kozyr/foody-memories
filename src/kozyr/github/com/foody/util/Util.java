package kozyr.github.com.foody.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

public class Util {
	
	public static boolean isEmpty(String s) {
		return (s == null || s.trim().equals(""));
	}

	public static boolean isValidId(long id) {
		return id > 0;
	}
	
	public static int deletePhoto(ContentResolver cr, long photoId) {
		int deleted = 0;
		if (isValidId(photoId)) {
			Uri photoURI = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(photoId));
			deleted = cr.delete(photoURI, null, null);
		}
		return deleted;
	}
	
	public static Uri getPhotoUri(long photoId) {
		return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(photoId));
	}
	
	public static long getPhotoId(String photoUri) {
		return Long.parseLong(photoUri.substring(photoUri.lastIndexOf("/")+1));
	}
	
	public static void showToast(Context mContext, String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}
	
	public static void appendToTitle(Activity a, String text) {
		// a.setTitle(a.getResources().getIdentifier("app_name", "string", a.getPackageName()) + ": " + text);
		a.setTitle("Foody Memories" + ": " + text);
	}
}
