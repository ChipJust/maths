package com.chipjust.maths;

import com.chipjust.maths.Quiz.QuestionFragment;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	
	private static final String USER_NAME = "user_name";
	private String user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		//SharedPreferences.Editor editor = sharedPref.edit();
		user = sharedPref.getString(USER_NAME, "no-user");

		if (savedInstanceState == null) {
			FragmentTransaction t = getFragmentManager().beginTransaction();
			Fragment fragment = new UserFragment();
			Bundle args = new Bundle();
			args.putString(USER_NAME, user);
			fragment.setArguments(args);
			t.add(R.id.container, fragment);
			t.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.action_quiz:
			startActivity(new Intent(this, Quiz.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class UserFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user, container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(R.id.user)).setText(args.getString(USER_NAME, "No User"));
			return rootView;
		}
	}
}
