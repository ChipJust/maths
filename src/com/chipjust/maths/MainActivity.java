package com.chipjust.maths;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends MathsActivity {
	
	private static final String USER_LIST = "user_list";
	private static final String NEW_USER = "New User";
	private static final String CURRENT_USER = "Current User";
	private static final String COLOR_SELECTED_USER = "#D9E3B1";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			FragmentTransaction t = getFragmentManager().beginTransaction();
			Fragment fragment = new UserSelectionFragment();
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
			startActivity(new Intent(this, QuizActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public static class UserSelectionFragment extends Fragment {
		
		public class UserSelectionFragmentListener implements View.OnClickListener {

			@Override
			public void onClick(View v) {
				Button b = (Button) v;
			    String buttonText = b.getText().toString();
				Log.v(TAG, String.format("onClick:%s.", buttonText));
				if (buttonText.equals(NEW_USER)) {
					getFragmentManager().beginTransaction().replace(R.id.container, new NewUserFragment()).commit();
					return;
				}

				// Change the current user to this user.
				SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
				editor.putString(CURRENT_USER, buttonText);
				editor.commit();
				
				getFragmentManager().beginTransaction().replace(R.id.container, new UserFragment()).addToBackStack(null).commit();
				return;
			}
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);
			LinearLayout l = (LinearLayout) rootView.findViewById(R.id.linearlayout);

			SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
			Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
			
			// Add the New User psuedo-user to the set. We can always add more users.
			user_list.add(NEW_USER);
			//NEWREL: Convert to list and sort.
			
			String currentUser = pref.getString(CURRENT_USER, "");
			
			// Create a button for each user.
			for (Object user: user_list) {
				Button button = new Button(getActivity());
				button.setText((CharSequence) user);
				button.setOnClickListener(new UserSelectionFragmentListener());
				// Check to see if this button is for the currently selected user. If so, highlight it.
				if (user.equals(currentUser)) {
					//button.setBackground(getResources().getDrawable(R.drawable.button_border));
					button.setBackgroundColor(Color.parseColor(COLOR_SELECTED_USER));
				}
				l.addView(button);
			}

			return rootView;
		}
	}
	
	public void createUserButtonClick (View view) {
		Log.v(TAG, "createUserButtonClick");
		EditText input = (EditText) findViewById(R.id.new_user_input);
		String newUser = input.getText().toString();
		
		TextView status = (TextView) findViewById(R.id.status_message);
		if (newUser.equals("")) {
			status.setText(R.string.blank_user_error);
			return;
		}
		if (newUser.equals(NEW_USER)) {
			status.setText(R.string.new_user_error);
			return;
		}
		
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
		// NEWREL: Make case insensitive, but preserve case in the set.
		
		if (user_list.contains(newUser)) {
			status.setText(R.string.user_exists_error);
			return;
		}
		
		// Create the preferences file for this user.
		SharedPreferences userPref = getSharedPreferences(newUser, Context.MODE_PRIVATE);
		SharedPreferences.Editor userEditor = userPref.edit();
		for (String op : operators) {
			userEditor.putBoolean(op, true);
		}
		for (Integer num : numbers) {
			userEditor.putBoolean(num.toString(), true);
		}
		userEditor.commit();

		// Add the new user to the set of users.
		user_list.add(newUser);
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(USER_LIST, user_list);
		editor.commit();
		
		// Transition back to the User Selection Screen.
		getFragmentManager().beginTransaction().replace(R.id.container, new UserSelectionFragment()).commit();
		return;
	}
	
	public static class NewUserFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_user, container, false);
			return rootView;
		}
	}
	
	public void deleteUserButtonClick (View view) {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
		String currentUser = pref.getString(CURRENT_USER, "");
		
		Log.v(TAG, String.format("deleteUserButtonClick:%s.", currentUser));
		
		user_list.remove(currentUser);
		
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(USER_LIST, user_list);
		editor.remove(CURRENT_USER);
		editor.commit();
		
		// Transition back to the User Selection Screen.
		getFragmentManager().beginTransaction().replace(R.id.container, new UserSelectionFragment()).commit();
		return;
	}
	
	public static class UserFragment extends Fragment {
		
		public class UserFragmentListener implements View.OnClickListener {

			@Override
			public void onClick(View view) {
				Activity activity = getActivity();
				String currentUser = activity.getPreferences(Context.MODE_PRIVATE).getString(CURRENT_USER, "");
				CheckBox b = (CheckBox) view;
			    String bText = b.getText().toString();
				boolean bChecked = b.isChecked();
				Log.v(TAG, String.format("userPreferncesCheckBoxClick:%s.%s.%b.", currentUser, bText, bChecked));
				SharedPreferences.Editor userEditor = activity.getSharedPreferences(currentUser, Context.MODE_PRIVATE).edit();
				userEditor.putBoolean(bText, bChecked);
				userEditor.commit();
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user, container, false);
			String currentUser = getActivity().getPreferences(Context.MODE_PRIVATE).getString(CURRENT_USER, "");
			LinearLayout l = (LinearLayout) rootView.findViewById(R.id.userPreferencesLinearLayout);
			Activity activity = getActivity();
			
			SharedPreferences userPref = activity.getSharedPreferences(currentUser, Context.MODE_PRIVATE);
			Map<String,?> keys = userPref.getAll();
			Map<String, Boolean> sortedKeys = new TreeMap<String, Boolean>((Map<String, Boolean>) keys);
			// NEWREL: the operators should be in there separately from the numbers.
			for(Map.Entry<String, Boolean> entry : sortedKeys.entrySet()) {
				CheckBox b = new CheckBox(activity);
				b.setText((CharSequence) entry.getKey());
				b.setOnClickListener(new UserFragmentListener());
				b.setChecked(entry.getValue());
				l.addView(b);
			}
			
			// NEWREL: display the score.
			
			return rootView;
		}
	}
}
