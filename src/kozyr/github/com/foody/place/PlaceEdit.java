package kozyr.github.com.foody.place;

import kozyr.github.com.foody.R;
import kozyr.github.com.foody.util.Util;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PlaceEdit extends Activity {
	private EditText _nameText;
    private EditText _reviewText;
    private long _placeId;
    private PlaceDAO _placesDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_edit);
        Util.appendToTitle(this, "Places");
        
        initDB();
        
        _nameText = (EditText) findViewById(R.id.place_name);
        _reviewText = (EditText) findViewById(R.id.place_review);
      
        Button confirmButton = (Button) findViewById(R.id.place_confirm);
       
        _placeId = savedInstanceState != null ? savedInstanceState.getLong(PlaceDAO.KEY_ROWID, -1) : -1;
		if (!Util.isValidId(_placeId)) {
			Bundle extras = getIntent().getExtras();            
			_placeId = extras != null ? extras.getLong(PlaceDAO.KEY_ROWID, -1) : -1;
		}
		
        confirmButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
        	    setResult(RESULT_OK);
        	    finish();
        	}
          
        });
        
        _nameText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				Button confirmButton = (Button) findViewById(R.id.place_confirm);
				confirmButton.setEnabled(s != null && s.length() > 0);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}
        	
        });
        
        populateFields();
        
        if (Util.isEmpty(_nameText.getText().toString())) {
        	confirmButton.setEnabled(false);
        }
    }
    
    private void initDB() {
        _placesDAO = new PlaceDAO(this);
    }
    
    private void populateFields() {
    	if (Util.isValidId(_placeId)) {
	        Place place = _placesDAO.getPlaceById(_placeId, this);
	        if (place != null) {
		        _nameText.setText(place.getName());
		        _reviewText.setText(place.getReview());
	        }
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(PlaceDAO.KEY_ROWID, _placeId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void saveState() {
        String name = _nameText.getText().toString();
        String review = _reviewText.getText().toString();
        int rating = 0;

        if (!Util.isEmpty(name)) {
	        if (!Util.isValidId(_placeId)) {
	            long id = _placesDAO.createPlace(name, review);
	            if (id > 0) {
	                _placeId = id;
	            }
	        } else {
	            _placesDAO.updatePlace(_placeId, name, review, rating);
	        }
        }
    }
}
