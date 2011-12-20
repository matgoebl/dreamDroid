/* © 2010 Stephan Reichholf <stephan at reichholf dot net>
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package net.reichholf.dreamdroid.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.reichholf.dreamdroid.DreamDroid;
import net.reichholf.dreamdroid.R;
import net.reichholf.dreamdroid.helpers.ExtendedHashMap;
import net.reichholf.dreamdroid.helpers.enigma2.CheckProfile;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class TabbedNavigationActivity extends TabActivity {
	private CheckProfileTask mCheckProfileTask;
	private TextView mActiveProfile;
	private TextView mConnectionState;

	private class CheckProfileTask extends AsyncTask<Void, String, ExtendedHashMap> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected ExtendedHashMap doInBackground(Void... params) {
			publishProgress(getText(R.string.checking).toString());
			return CheckProfile.checkProfile(DreamDroid.PROFILE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(String... progress) {
			setConnectionState(progress[0]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(ExtendedHashMap result) {
			Log.i(DreamDroid.LOG_TAG, result.toString());
			if ((Boolean) result.get(CheckProfile.KEY_HAS_ERROR)) {
				String error = getText((Integer) result.get(CheckProfile.KEY_ERROR_TEXT)).toString();
				setConnectionState(error);
			} else {
				setConnectionState(getText(R.string.ok).toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabhost);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, MainActivity.class);
		spec = tabHost.newTabSpec("menu")
				.setIndicator(getText(R.string.main_menu), res.getDrawable(android.R.drawable.ic_menu_view))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ExtrasActivity.class);
		spec = tabHost.newTabSpec("extras")
				.setIndicator(getText(R.string.extras), res.getDrawable(android.R.drawable.ic_menu_zoom))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ProfileListActivity.class);
		spec = tabHost.newTabSpec("profiles")
				.setIndicator(getText(R.string.profiles), res.getDrawable(R.drawable.ic_tab_link))
				.setContent(intent);
		tabHost.addTab(spec);
		
		if (Build.VERSION.SDK_INT >= 11) {
			final Activity activity = this;
			tabHost.setOnTabChangedListener(new OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					try {
						Method invalidateOptionsMenu = activity.getClass().getMethod("invalidateOptionsMenu");
						invalidateOptionsMenu.invoke(activity);
					} catch (NoSuchMethodException e) {
						Log.e(DreamDroid.LOG_TAG, e.toString());
					} catch (IllegalArgumentException e) {
						Log.e(DreamDroid.LOG_TAG, e.toString());
					} catch (IllegalAccessException e) {
						Log.e(DreamDroid.LOG_TAG, e.toString());
					} catch (InvocationTargetException e) {
						Log.e(DreamDroid.LOG_TAG, e.toString());
					}
				}
			});
		}
		tabHost.setCurrentTab(0);
		
		mActiveProfile = (TextView) findViewById(R.id.TextViewProfile);
		mConnectionState = (TextView) findViewById(R.id.TextViewConnectionState);

		onProfileChanged();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if(Build.VERSION.SDK_INT >= 11){
			if(getTabHost().getCurrentTab() == 2){
				menu.add(0, ProfileListActivity.ITEM_ADD_PROFILE, 1, getText(R.string.profile_add)).setIcon(android.R.drawable.ic_menu_add);
			} else {
				menu.add(0, MainActivity.ITEM_SETTINGS, 0, getText(R.string.settings)).setIcon(android.R.drawable.ic_menu_edit);
			}
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(Build.VERSION.SDK_INT >= 11){
			return getCurrentActivity().onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 
	 */
	public void setProfileName() {
		mActiveProfile.setText(DreamDroid.PROFILE.getProfile());
	}

	/**
	 * @param state
	 */
	private void setConnectionState(String state) {
		mConnectionState.setText(state);
		setAvailableFeatures();
	}

	/**
	 * 
	 */
	public void checkActiveProfile() {		
		if (mCheckProfileTask != null) {
			mCheckProfileTask.cancel(true);
		}

		mCheckProfileTask = new CheckProfileTask();
		mCheckProfileTask.execute();
	}
	
	public void onProfileChanged(){
		setProfileName();
		checkActiveProfile();
		
	}

	/**
	 * 
	 */
	private void setAvailableFeatures() {
		Activity currentActivity = getCurrentActivity();
		if (currentActivity.getClass().equals(ExtrasActivity.class)) {
			((ExtrasActivity) currentActivity).setAvailableFeatures();
		}
	}

}
