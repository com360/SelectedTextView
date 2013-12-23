package com.suchangli.text;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView mSelectedText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		mSelectedText = (TextView) findViewById(R.id.selected_textview);
		
	}
}

 
