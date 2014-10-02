package com.chipjust.maths;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Quiz extends Activity {
	
	private static final String TAG = "QuizActivity";
	
	// args for the fragment
	private static final String QUESTION = "question";
	private static final String BUTTONS = "buttons";
	private Map<Integer, Integer> buttons;
	
	private static final List<String> operators = Arrays.asList("+", "-", "x", "/");
	private static final List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
	private int operand1 = 0;
	private int operand2 = 0;
	private String operator = "?";
	private int answer = 0;
	private static final String COLOR_GREEN = "#30E873";
	private static final String COLOR_RED = "#FF1028";
	
	
	// We have one button handler, and we pause between clicks. We only allow one click per question.
	// So this boolean gates the buttonClick function to ignore new input until we draw a new question (fragment)
	// and reset this flag.
	private boolean clicked = false;
	
	Handler handler;
	Runnable runNewQuestion;
	
	// NEWREL: make a timer
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		runNewQuestion = new Runnable() {
			public void run () {
				newQuestion();
			}};
		buttons = new HashMap<Integer, Integer>();
		// NEWREL: initialize the timer
		if (savedInstanceState == null) {
			newQuestion();
		}
	}

	private Integer evaluate (String myOperator, Integer myOperand1, Integer myOperand2) {
		switch (myOperator) {
		case "+":
			return myOperand1 + myOperand2;
		case "-":
			return myOperand1 - myOperand2;
		case "x":
			return myOperand1 * myOperand2;
		case "/":
			if (myOperand2 == 0) {
				break;
			}
			return myOperand1 / myOperand2;
		default:
			break;
		}
		return 0;
	}

	private Bundle setNewQuestion() {
		int i;
		Random rand = new Random();

		// NEWREL:we could filter the list of operands by the user preferences
		// NEWREL:we could filter the list of numbers by the user preferences

		// Operator
		List<String> myOperators = new ArrayList<String>();// NEWREL: move this step to generateWrongAnswers
		myOperators.addAll(operators);// NEWREL: move this step to generateWrongAnswers
		i = rand.nextInt(myOperators.size());
		operator = myOperators.get(i);
		myOperators.remove(i);// NEWREL: move this step to generateWrongAnswers
		
		// Operands
		// NEWREL: selection of operands needs to consider the operator.
		//         In particular div needs a large operand1 which other operators don't
		//		   maybe we should use mult for div, so instead of random selction of x and y in x/y=z
		//         we select y and z from our range and x = y*z
		// NEWREL: need to add some user preference for keeping things positive.
		//         negative numbers as results are harder, that is more advanced and we need to be
		//         able to configure it so that we avoid those questions, again maybe do the selection
		//         of y and z in x - y = z and x = z + y
		i = rand.nextInt(numbers.size());
		operand1 = numbers.get(i);
		i = rand.nextInt(numbers.size());
		operand2 = numbers.get(i);
		
		// Right Answer
		answer = evaluate (operator, operand1, operand2);
		
		// Wrong Answers
		// NEWREL: This should be in a function, maybe Set<Integer> wrongAnswers = generateWrongAnswers(op, operand1, operand2, delta, numberOfWrongAnswers);
		// NEWREL: maybe add a transpose wrong answer, so if the answer was 81 we should have a wrong answer of 18 available - my children do this...
		Set<Integer> wrongAnswers = new HashSet<Integer>();
		Integer wrongAnswer;
		for (String op : myOperators) {
			wrongAnswer = evaluate (op, operand1, operand2);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
		}
		for (Integer delta : new Integer[] {1, 2, 10}) {
			wrongAnswer = evaluate (operator, operand1+delta, operand2);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
			wrongAnswer = evaluate (operator, operand1, operand2+delta);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
			wrongAnswer = evaluate (operator, operand1-delta, operand2);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
			wrongAnswer = evaluate (operator, operand1, operand2-delta);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
		}
		
		// Shuffle answers and assign to buttons
		List<Integer> myAnswers = new ArrayList<Integer>();
		myAnswers.addAll(wrongAnswers);
		Collections.shuffle(myAnswers); // NEWREL: move this step to generateWrongAnswers
		myAnswers.subList(3, myAnswers.size()).clear(); // NEWREL: move this step to generateWrongAnswers
		myAnswers.add(answer); // add the correct answer last
		Collections.shuffle(myAnswers);
		buttons.put(R.id.button_1, myAnswers.get(0));
		buttons.put(R.id.button_2, myAnswers.get(1));
		buttons.put(R.id.button_3, myAnswers.get(2));
		buttons.put(R.id.button_4, myAnswers.get(3));
		
		// Fill in the bundle with question and answers
		Bundle args = new Bundle();
		args.putString(QUESTION, String.format("%d %s %d", operand1, operator, operand2));
		args.putSerializable(BUTTONS, (Serializable) buttons);
		return args;
	}

	private void newQuestion() {
		FragmentTransaction t = getFragmentManager().beginTransaction();
		Fragment fragment = new QuestionFragment();
		Bundle args = setNewQuestion();
		fragment.setArguments(args);
		t.replace(R.id.container, fragment);
		t.commit();
		clicked = false;
		// NEWREL: start the timer
	}
	
	// There is only one handler for all the question buttons.
	public void buttonClick (View view) {
		if (clicked) {
			return;
		}
		clicked = true; // no more clicks
		// NEWREL: stop the timer
		Integer userAnswer = buttons.get(view.getId());
		if (answer == userAnswer) {
			view.setBackgroundColor(Color.parseColor(COLOR_GREEN));
		} else {
			view.setBackgroundColor(Color.parseColor(COLOR_RED));
			// find the right answer and turn than button green.
			for (Object button : buttons.keySet()) {
				if ((int) buttons.get(button) == answer) {
					findViewById((int) button).setBackgroundColor(Color.parseColor(COLOR_GREEN));
					break;
				}
			}
		}
		// NEWREL: log the question, answer, userAnswer and time-consumed-on-this-question in history
		// NEWREL: calculate time remaining and only run the next question if there is time remaining
		handler.postDelayed(runNewQuestion, 750); // NEWREL: make this delay configurable in user preferences
	}

	public static class QuestionFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_question, container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(R.id.question)).setText(args.getString(QUESTION, "No Question"));
			Map<Integer, Integer> buttonMap = (Map<Integer, Integer>) args.getSerializable(BUTTONS);
			for (Object button : buttonMap.keySet()) {
				((Button) rootView.findViewById((int) button)).setText(String.format("%d", buttonMap.get(button)));
			}
			return rootView;
		}
	}
}