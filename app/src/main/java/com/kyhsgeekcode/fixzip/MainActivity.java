package com.kyhsgeekcode.fixzip;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements View.OnClickListener,IConsole
{

	@Override
	public void print(final String s)
	{
		runOnUiThread(new Runnable(){
				@Override
				public void run()
				{
					adapter.add(s);
					adapter.notifyDataSetChanged();
					return ;
				}
			});	
		return ;
	}

	@Override
	public String readLine() throws InterruptedException
	{
		isInputMode=true;
		synchronized(lock)
		{
			lock.wait();
		}
		isInputMode=false;
		return input;
	}
	
	@Override
	public void onClick(View p1)
	{
		if(!isInputMode)
		{
			return;		//ignore!
		}
		String tmp=etCommand.getText().toString();
		synchronized(lock){
			input=tmp;
		}
		etCommand.setText("");
		adapter.add("$"+input);
		adapter.notifyDataSetChanged();
		synchronized(lock)
		{
			lock.notifyAll();
		}
		return ;
	}
	
	Button btGo;
	ListView lvScreen;
	EditText etCommand;
	ArrayList<String> contents;
	boolean isInputMode;
	String input="";
	ArrayAdapter<String> adapter;
	Object lock=new Object();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		btGo=(Button) findViewById(R.id.mainBTDo);
		etCommand=(EditText) findViewById(R.id.mainETCommand);
		lvScreen=(ListView) findViewById(R.id.mainListView);
		contents=new  ArrayList<>();
		contents.add("Begin");
		adapter = new ArrayAdapter<String>(this,
																android.R.layout.simple_list_item_1,
																android.R.id.text1,
																contents);


		// Assign adapter to ListView
		lvScreen.setAdapter(adapter); 
		btGo.setOnClickListener(this);
		isInputMode=false;
		new Thread(new Runnable(){
				@Override
				public void run()
				{
					TestProgram.Run(MainActivity.this);
					print("Program finished");
					//isInputMode=true;
					return ;
				}
			}).start();
    }
}
