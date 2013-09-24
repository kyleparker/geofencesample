/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.bsu.android.apps.geofence;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import edu.bsu.android.apps.geofence.R;

import edu.bsu.android.apps.geofence.content.ContentProviderUtils;
import edu.bsu.android.apps.geofence.objects.SimpleGeofence;
import edu.bsu.android.apps.geofence.utils.Constants;

import java.util.ArrayList;

/**
 * UI handler for the Location Services Geofence sample app. Allow input of latitude, longitude, and radius for two geofences. When registering
 * geofences, check input and then send the geofences to Location Services. Also allow removing either one of or both of the geofences. The menu
 * allows you to clear the screen or delete the geofences stored in persistent memory.
 */
public class MainActivity extends ListActivity {
	
	private Activity mActivity;
	
	private ContentProviderUtils mProvider;
	private ArrayList<SimpleGeofence> mSimpleGeofence;
	private SimpleGeofenceAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence_list);

		mActivity = this;
		mProvider = ContentProviderUtils.Factory.get(mActivity);
		
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	
	@Override 
	protected void onResume() {
		super.onResume();

        loadList();
	}

	/*
	 * Inflate the app menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;

	}

	/*
	 * Respond to menu item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.map:
				startActivity(newIntent(this, MapActivity.class));
				return true;
			case R.id.add_geofence:
				startActivity(newIntent(this, GeofenceEditor.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        getListView().setItemChecked(position, true);

        SimpleGeofence item = (SimpleGeofence) getListView().getItemAtPosition(position);
        final Intent intent = new Intent(newIntent(mActivity, GeofenceEditor.class));
        intent.putExtra(Constants.EXTRA_ID, item.getId());
        
        mActivity.startActivity(intent);
    }

    private void loadList() {
    	mSimpleGeofence = new ArrayList<SimpleGeofence>();
    	mAdapter = new SimpleGeofenceAdapter(mActivity, mSimpleGeofence);
        setListAdapter(this.mAdapter);

        Runnable load = new Runnable(){
            public void run() {
            	try {   	
            		mSimpleGeofence = mProvider.getList();
            	} catch (Exception ex) {
            		ex.printStackTrace();
            	} finally {
            		mActivity.runOnUiThread(returnList);
            	}
            }
        };
        
        Thread thread = new Thread(null, load, "loadList");
        thread.start();
    }

    private Runnable returnList = new Runnable(){
        public void run() {
            if (mSimpleGeofence != null && mSimpleGeofence.size() > 0){
                for (int i=0; i < mSimpleGeofence.size(); i++) {
                	mAdapter.add(mSimpleGeofence.get(i));
                }
            } 

            mAdapter.notifyDataSetChanged();
        }
    };

	/**
	 * InvitationAdapter used to populate the listview for the invited travelers
	 */
	public static class SimpleGeofenceAdapter extends ArrayAdapter<SimpleGeofence> {
		private ArrayList<SimpleGeofence> items;
		private Activity activity;
		private int selectedPosition;

		public SimpleGeofenceAdapter(Activity activity, ArrayList<SimpleGeofence> items) {
			super(activity.getApplicationContext(), 0, items);
			this.items = items;
			this.activity = activity;
			this.selectedPosition = -1;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		public void setSelectedPosition(int selectedPosition) {
			this.selectedPosition = selectedPosition;
		}

		public SimpleGeofence getSelected() {
			return items.get(selectedPosition);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				view = LayoutInflater.from(activity).inflate(android.R.layout.simple_list_item_1, null);
			}

			SimpleGeofence item = items.get(position);

			if (item != null) {
				TextView text = (TextView)view.findViewById(android.R.id.text1);
				text.setText(item.getName());
			} 

			return view;
		}
	}

	/**
	 * Creates an intent with {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and {@link Intent#FLAG_ACTIVITY_NEW_TASK}.
	 * 
	 * @param context the context
	 * @param cls the class
	 */
	public static final Intent newIntent(Context context, Class<?> cls) {
		try {
			return new Intent(context, cls).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		} catch (Exception ex) {
			return new Intent();
		}
	}
}
