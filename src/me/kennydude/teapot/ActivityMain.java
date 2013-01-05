package me.kennydude.teapot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ActivityMain extends Activity implements BuildPropManager.Logger {
	
	SharedPreferences prefs;

	@Override
	public void onCreate(Bundle bi){
		super.onCreate(bi);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		setContentView(R.layout.activity_main);
		Button b = (Button) findViewById(R.id.button);
		
		setLabel();
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(prefs.getBoolean("enabled", false) == true){
					deactivate();
				} else{
					activate();
				}
				
			}
			
		});
		log = (TextView) findViewById(R.id.log);
		
	}
	
	public void setLabel(){
		Button b = (Button) findViewById(R.id.button);
		if(prefs.getBoolean("enabled", false) == true){
			b.setText(R.string.deactivate);
		} else{
			b.setText(R.string.activate);
		}
	}
	
	TextView log;
	
	public void log(final String e){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				log.setText(log.getText() + "\n" + e);
			}
			
		});
	}
	
	public void showWait(){
		findViewById(R.id.button).setEnabled(false);
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
	}
	
	public void endWait(final boolean enabled){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				findViewById(R.id.button).setEnabled(true);
				findViewById(R.id.progress).setVisibility(View.GONE);
				prefs.edit().putBoolean("enabled", enabled).commit();
				setLabel();
			}
			
		});
	}
	
	
	
	public void activate(){
		showWait();
		new Thread(new Runnable(){

			@Override
			public void run() {
				log("Activating Teapot...");
				
				BuildPropManager prop = new BuildPropManager(ActivityMain.this);
		    	prop.setProperty("ro.error.receiver.default", "me.kennydude.teapot");
		    	prop.save();
		        
		    	log("Teapot is active on your system.");
		        endWait(true);
			}
			
		}).start();
	}
	
	
	
	public void deactivate(){
		showWait();
		new Thread(new Runnable(){

			@Override
			public void run() {
				log("Deactivating Teapot...");
				
				BuildPropManager prop = new BuildPropManager(ActivityMain.this);
		    	prop.remove("ro.error.receiver.default");
		    	prop.save();
		        
		    	log("Teapot is not active on your system");
		        endWait(false);
			}
			
		}).start();
	}
	
}
