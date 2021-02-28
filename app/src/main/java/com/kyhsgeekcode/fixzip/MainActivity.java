package com.kyhsgeekcode.fixzip;

import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.*;
import android.widget.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.*;

public class MainActivity extends Activity implements View.OnClickListener,IConsole
{
	private AdView mAdView;
	@Override
	public void print(final String s)
	{
		runOnUiThread(new Runnable(){
				@Override
				public void run()
				{
					//avoid OutOfMemoryError
					if(adapter.getCount()>10000)
						adapter.remove(adapter.getItem(0));
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
		contents.add(getString(R.string.begin));
		adapter = new ArrayAdapter<String>(this,
																android.R.layout.simple_list_item_1,
																android.R.id.text1,
																contents);


		// Assign adapter to ListView
		lvScreen.setAdapter(adapter); 
		btGo.setOnClickListener(this);
		isInputMode=false;
		mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		/*mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when when the user is about to return
				// to the app after tapping on an ad.
			}
		});*/
		int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if(permissionCheck==PackageManager.PERMISSION_DENIED) {
			requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2000);
			permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		new Thread(new Runnable(){
				@Override
				public void run()
				{
					FixZip.Run(MainActivity.this);
					print(getString(R.string.finish));
					//isInputMode=true;
					return ;
				}
			}).start();
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length<2||grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, R.string.grantPerm,Toast.LENGTH_LONG).show();
				}
			});
			finish();
			//System.runFinalization();
			//System.exit(1);
		}
	}
}
