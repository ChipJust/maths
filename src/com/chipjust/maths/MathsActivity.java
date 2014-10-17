package com.chipjust.maths;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;

public class MathsActivity extends Activity {
	protected static final String TAG = "MathsActivity";
	protected static final String USERS_FILE = "Com.ChipJust.MathsActivity.UsersFile";

	protected static final List<String> operators = Arrays.asList("+", "-", "x", "/");
	protected static final List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
	

	protected static final String CURRENT_USER = "Current User";

}
