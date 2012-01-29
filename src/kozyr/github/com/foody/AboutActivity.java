package kozyr.github.com.foody;

import java.io.IOException;

import kozyr.github.com.foody.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity extends Activity {
	
	private static final String TAG = "AboutActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		Util.appendToTitle(this, "About");
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.context_menu_about_load_db:
            showAcceptDialog();
            return true;
        case R.id.context_menu_about_dump_db:
        	dumpDB();
        	return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }

	private void dumpDB() {
		try {
			DbManager.getInstance(this).dumpDB();
			Util.showToast(this, "DB saved on SD card.");
		} catch (IOException e) {
			Log.e(TAG, "Saving of DB failed...", e);
			Util.showToast(this, "Could not save DB...");
		}
		
	}

	private void loadDB() {
		try {
			if (DbManager.getInstance(this).loadDB()) {
				Util.showToast(this, "DB loaded from SD card.");
			} else {
				Util.showToast(this, "Could not load DB -- memorizer.db is not on SD card?");
			}
		} catch (IOException e) {
			Log.e(TAG, "Loading of DB failed...", e);
			Util.showToast(this, "Could not load DB...");
		}
	}
	
	protected void showAcceptDialog()
	{
		Dialog d = new AlertDialog.Builder(this)
		.setTitle("Confirm")
		.setMessage("This will completely replace all your data!")
		.setPositiveButton("Load data", new
				DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int
					whichButton) {
				loadDB();
			}
		})
		.setNegativeButton("Cancel", new
				DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int
					whichButton) {
				
			}
		})
		.create();
		
		d.show();
	} 
}
