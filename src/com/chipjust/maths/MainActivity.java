package com.chipjust.maths;

import java.util.HashSet;
import java.util.Set;

import com.chipjust.maths.Quiz.QuestionFragment;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	private static final String USER_LIST = "user_list";
	private static final String NEW_USER = "New User";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		//SharedPreferences.Editor editor = sharedPref.edit();

		if (savedInstanceState == null) {
			FragmentTransaction t = getFragmentManager().beginTransaction();
			Fragment fragment = new UserSelectionFragment();
			//Bundle args = new Bundle();
			//fragment.setArguments(args);
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
	
	public static void addUser() {
		
	}

	public static class UserButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Button b = (Button)v;
		    String buttonText = b.getText().toString();
			Log.v(TAG, String.format("onClick:%s", buttonText));
			if (buttonText == NEW_USER) {
				addUser();
			}
		}
		
	}

	public static class UserSelectionFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);
			LinearLayout l = (LinearLayout) rootView.findViewById(R.id.linearlayout);

			//Bundle args = getArguments();
			SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
			Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
			// Add the New User psuedo-user to the set. We can always add more users.
			user_list.add(NEW_USER);
			
			// Create a button for each user.
			for (Object user: user_list) {
				Button button = new Button(getActivity());
				button.setText((CharSequence) user);
				button.setOnClickListener(new UserButtonListener());
				l.addView(button);
			}

			return rootView;
		}
	}
}
