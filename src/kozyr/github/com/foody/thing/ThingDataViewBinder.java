package kozyr.github.com.foody.thing;

import kozyr.github.com.foody.Constants;
import android.database.Cursor;
import android.view.View;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * setViewValue on the viewBinder will be called for every view passed to the cursor
 * We can have custom view setup there
 * @author sergei
 *
 */
public class ThingDataViewBinder implements ViewBinder {

	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		int ratingIndex = cursor.getColumnIndex(ThingDAO.KEY_RATING);
		
		if (columnIndex == ratingIndex) {
			return setRatingView(view, cursor, columnIndex);
		} 
		
		return false;
	}

	private boolean setRatingView(View view, Cursor cursor, int columnIndex) {
		
		RatingBar ratingBar = (RatingBar) view;
		int rating = cursor.getInt(columnIndex);
		ratingBar.setRating(rating/Constants.RATING_MULTIPLIER);
		return true;
	}

}
