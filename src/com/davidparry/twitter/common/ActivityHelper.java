/*
Copyright (c) 2009-2010, David Parry
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the David Parry nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY David Parry ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL David Parry BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.davidparry.twitter.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.davidparry.twitter.ButlerException;
import com.davidparry.twitter.R;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ActivityHelper {
	private static final String tag = "ActivityHelper";
	private Activity activity;
	private static String INTENT_MESSAGE_KEY = "com.davidparry.twitter.message";
	private static String INTENT_RESULT_KEY = "com.davidparry.twitter.result";
	private static final String VERSION = "13";
	public static final String TWEETS_FILE_PATH = "/sdcard/tbutler.ser";
	
	public ActivityHelper(Activity activity){
		this.activity = activity;
	}
	/**
	 * shows the toast message from the message in the Intent
	 */
	public void showToastMessage(){
		Bundle extras = this.activity.getIntent().getExtras();
		if (extras != null) {
			if (extras.get(INTENT_MESSAGE_KEY) != null) {
				Toast.makeText(this.activity,extras.getString(INTENT_MESSAGE_KEY) , Toast.LENGTH_LONG);
			}
		}
	}
	/**
	 * @param message
	 */
	public void startActivity(Class activityClass,String result,String message){
		Intent rt = new Intent();
		if(result != null){
			rt.putExtra(INTENT_RESULT_KEY, result);
		}
		if(message != null){
			rt.putExtra(INTENT_MESSAGE_KEY, message);
		}
		rt.setClass(this.activity,activityClass);
		this.activity.startActivity(rt);
	}
	
	
	public boolean whatsNew() {
		if(VERSION.equals(PreferenceManager.getDefaultSharedPreferences(this.activity).getString("whatsnew", ""))) {
			return false;
		} else {
			return true;
		}
	}
	
	public void showWhatsNewDialog() {
		Log.d(tag, "showWhatsNewDialog() version value "+PreferenceManager.getDefaultSharedPreferences(this.activity).getString("whatsnew", ""));
		if(whatsNew()){
			LayoutInflater factory = LayoutInflater.from(this.activity);
	        final View textEntryView = factory.inflate(com.davidparry.twitter.R.layout.whats_new, null);
	        new AlertDialog.Builder(this.activity).setIcon(R.drawable.icon).setTitle("What's New").setView(textEntryView).setPositiveButton("Done",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface d, int which) {
							Dialog dialog = (Dialog) d;
							SharedPreferences prefs = activity.getPreferences(Context.MODE_WORLD_WRITEABLE);
							Editor edit = prefs.edit();
					        edit.putString("whatsnew", VERSION);
					        edit.commit();
						}
					}).setNegativeButton("Email Me",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							AlertDialog d = (AlertDialog) dialog;
							Toast.makeText( d.getContext().getApplicationContext(), "Email application starting..." , Toast.LENGTH_SHORT).show();
				        	Log.d(tag, "Sending email "); 
				        	Intent emailIntent = new Intent(Intent.ACTION_SEND);
				        	emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Enhancement/Bug Report for TButler");
				        	emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        	emailIntent.putExtra(Intent.EXTRA_TEXT, "");
				        	String[] tos = new String[]{"d.dry.parry@gmail.com"};
				        	emailIntent.putExtra(Intent.EXTRA_EMAIL, tos);
				        	emailIntent.setType("message/rfc822");
				        	try {
								d.getContext().getApplicationContext().startActivity(emailIntent);
							} catch (Exception e) {
								Log.e(tag, "Error starting email application ",e);
							}
						}
					}).show();
	        }
   }
	
	public void writeTweets(TwitterResult result) throws ButlerException {
		if(checkState()){
			writeXStreamObject(result);
		}
	}
	public TwitterResult readTweets(){
		if(checkState()){
			return readXStreamObject();
		}
		return null;
	}
	public static boolean checkState(){
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    mExternalStorageAvailable = true;
		}
		return mExternalStorageAvailable;
	}
	
	private TwitterResult readXStreamObject(){
		  XStream xs = new XStream(new DomDriver());
		  TwitterResult result = new TwitterResult();
	        try {
	            FileInputStream fis = new FileInputStream(TWEETS_FILE_PATH);
	            xs.fromXML(fis, result);
	        } catch (FileNotFoundException ex) {
	            Log.e(tag, "Error reading XStreamFile", ex);
	        }
	        return result;
	}
	
	private void writeXStreamObject(TwitterResult result) throws ButlerException{
		 XStream xs = new XStream();
	        try {
	            FileOutputStream fs = new FileOutputStream(TWEETS_FILE_PATH);
	            xs.toXML(result, fs);
	        } catch (FileNotFoundException e) {
	           Log.e(tag, "Error writing XStream File", e);
	           throw new ButlerException(e);
	        }
	}
	
	

	
		
}
