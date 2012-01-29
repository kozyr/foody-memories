package kozyr.github.com.foody.place;

import kozyr.github.com.foody.AboutActivity;
import kozyr.github.com.foody.DbManager;
import kozyr.github.com.foody.R;
import kozyr.github.com.foody.thing.ThingActivity;
import kozyr.github.com.foody.thing.ThingDAO;
import kozyr.github.com.foody.util.MyExceptionHandler;
import kozyr.github.com.foody.util.Util;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PlaceList extends ListActivity  {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_BROWSE=2;

    private PlaceDAO _placesDAO;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
        setContentView(R.layout.place_list);
        Util.appendToTitle(this, "Places");
        
        initDB();
        fillData();
        
        registerForContextMenu(getListView());
    }
    
    private void initDB() {
        _placesDAO = new PlaceDAO(this);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	DbManager.destroy();
    	 // Debug.stopMethodTracing();
    }

    
    private void fillData() {
        SimpleCursorAdapter places = _placesDAO.fetchAllPlaces(this);
        setListAdapter(places);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.place_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_place_add:
            createPlace();
            return true;
        case R.id.menu_place_about:
        	showAboutScreen();
        	return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
    private void showAboutScreen() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        LinearLayout layout = (LinearLayout) info.targetView;
        TextView name = (TextView) layout.getChildAt(0);
        menu.setHeaderTitle( name.getText());

        getMenuInflater().inflate(R.menu.place, menu);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case R.id.context_menu_place_delete:
		        _placesDAO.deletePlace(info.id);
		        fillData();
		        return true;
			case R.id.context_menu_place_edit:
				editPlace(info.id);
				return true;
			case R.id.context_menu_place_view:
				viewPlace(info.id);
				return true;
		}
		return super.onContextItemSelected(item);
	}
	
    private void editPlace(long id) {
    	Intent i = new Intent(this, PlaceEdit.class);
        i.putExtra(PlaceDAO.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
	}

	private void createPlace() {
        Intent i = new Intent(this, PlaceEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
	
	public void viewPlace(long id) {
		Intent i = new Intent(this, ThingActivity.class);
        i.putExtra(ThingDAO.KEY_PLACE, id);
        startActivityForResult(i, ACTIVITY_BROWSE);
	}
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        viewPlace(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

	
}