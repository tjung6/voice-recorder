package com.deitel.voicerecorder;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VoiceRecorder extends Activity {
   private static final String TAG = VoiceRecorder.class.getName();
   private MediaRecorder recorder; // Records the audio 
   private MediaPlayer mediaPlayer; // Plays the audio 
   // private Handler handler; // Handler for updating the visualizer
   private boolean recording; // Is the app currently recording
   
   MenuItem mi; 
   MenuItem mi2; 
   
   //private VisualizerView visualizer; 
   
   // Global variables for buttons 
   private ToggleButton recordButton;
   private Button saveButton;
   private Button deleteButton;
   private ToggleButton playButton;
   File tempFile;
   
   // Global variables for timer 
   private long startTime = 0; 
   private Handler myHandler = new Handler(); 
   long timeInMillies = 0L; 
   long timeSwap = 0L; 
   long finalTime = 0L; 
   private TextView textTimer; 
   
   private WheelImageView vinyl; // Record spinner 
   /*
   private SeekBar mPlaySeekBar;
   private static final int SEEK_BAR_MAX = 10000;
   private final Handler seekBarHandler = new Handler();
*/ 
	
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main); // set the Activity's layout
      
      // get the Activity's Buttons and VisualizerView
      recordButton = (ToggleButton) findViewById(R.id.recordButton);
      saveButton = (Button) findViewById(R.id.saveButton);
      saveButton.setEnabled(false); // disable saveButton initially
      deleteButton = (Button) findViewById(R.id.deleteButton);
      deleteButton.setEnabled(false); // disable deleteButton initially
      playButton  = (ToggleButton) findViewById(R.id.playButton);
      // visualizer = (VisualizerView) findViewById(R.id.visualizerView);
      playButton.setEnabled(false);

      // register listeners
      playButton.setOnCheckedChangeListener(playButtonListener);
      saveButton.setOnClickListener(saveButtonListener);
      deleteButton.setOnClickListener(deleteButtonListener);
      recordButton.setOnCheckedChangeListener(recordButtonListener); 
      // viewSavedRecordingsButton.setOnClickListener(viewSavedRecordingsListener);
      
      vinyl = (WheelImageView) findViewById(R.id.vinyl);
            
      // handler = new Handler(); // create the Handler for visualizer update
      /*
      mPlaySeekBar = (SeekBar) findViewById(R.id.progressSeekBar);
      mPlaySeekBar.setOnSeekBarChangeListener(progressChangeListener);
      mPlaySeekBar.setMax(SEEK_BAR_MAX);
      mPlaySeekBar.setVisibility(View.INVISIBLE); */ 
   } 
   
   @Override
	public boolean onCreateOptionsMenu(Menu menu) {
       mi = menu.add(0, 1, 0, "Settings"); // Creates "Settings" tab 
       mi2 = menu.add(0, 2, 0, "Files"); // Creates "Files" tab 
       
       mi.setIntent(new Intent(this, Recorder_preferences.class));
       mi2.setIntent(new Intent(this, SavedRecordings.class));
       return super.onCreateOptionsMenu(menu);
	}
   
   // Creates MediaRecorder
   @Override
   protected void onResume()
   {
      super.onResume();
      recordButton.setOnCheckedChangeListener(recordButtonListener); // Registers recordButton's listener
   } 
   
   // Releases MediaRecorder
   @Override
   protected void onPause()
   {
	   super.onPause();
	   recordButton.setOnCheckedChangeListener(null); // Removes recordButton's listener
      
	   if (recorder != null)
	   {
    	  // handler.removeCallbacks(updateVisualizer); // stop updating GUI
    	  // visualizer.clear(); // clear visualizer for next recording

    	  recordButton.setChecked(false); // Makes recordButton clickable 
    	  recorder.release(); // Releases MediaRecorder resources
    	  
    	  // Recording is over 
    	  recording = false; 
    	  recorder = null; 
    	  
    	  ((File) deleteButton.getTag()).delete(); // delete the temporary file
	   } 
	   /*
	   mediaPlayer.seekTo(mPlaySeekBar.getProgress());
	   timeSwap = 0L; 
	   startTime = SystemClock.uptimeMillis(); 
	   myHandler.postDelayed(updateTimerMethod, 0); */ 
	   
   } 
   
   OnSeekBarChangeListener progressChangeListener = new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         if (fromUser)
         {
            mediaPlayer.seekTo(seekBar.getProgress());
			   timeSwap = progress; 
			   startTime = SystemClock.uptimeMillis(); 
			   myHandler.postDelayed(updateTimerMethod, 0);
         }
      } // end method onProgressChanged 

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) 
      {
      } // end method onStartTrackingTouch

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) 
      {
      } // end method onStopTrackingTouch
   }; // end OnSeekBarChangeListener
   
   private Runnable mMyRunnable = new Runnable()
   {
       @Override
       public void run()
       {
          //Change state here
       }
    };
   
   // Starts or stops a recording
   OnCheckedChangeListener recordButtonListener = new OnCheckedChangeListener() {
	   @Override
       public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		   // If you click (turn on) the record button 
		   if (isChecked) {
			  //  mPlaySeekBar.setVisibility(View.INVISIBLE);
			   textTimer = (TextView) findViewById(R.id.tV_timer);
			   timeSwap = 0L; 
			   startTime = SystemClock.uptimeMillis(); 
			   myHandler.postDelayed(updateTimerMethod, 0);
            	
			   // recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.record3)); 
			   vinyl.setImageDrawable(getResources().getDrawable(R.drawable.vinylrecord_rec));
			   startRecordPlayingAnimation();
			   // visualizer.clear(); // clear visualizer for next recording
            	
			   // When the recorder is recording, disable all the buttons 
			   saveButton.setEnabled(false); // Disable save button
			   deleteButton.setEnabled(false); // Disable delete button
			   playButton.setEnabled(false); // Disables play button  
			   mi.setEnabled(false); 
			   mi2.setEnabled(false); 
			   
			   Toast message = Toast.makeText(VoiceRecorder.this, "Press again to stop recording", Toast.LENGTH_SHORT);
               message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
               message.show(); // Display the Toast
			   
               String type = Recorder_preferences.getRecordType(VoiceRecorder.this);
               boolean isHighQuality = Recorder_preferences.isHighQuality(VoiceRecorder.this);
               
               // Creates MediaRecorder & configures recording options
               if (recorder == null)
                  recorder = new MediaRecorder(); // create MediaRecorder 
               recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
               if(type.compareTo("audio/amr") == 0) {
	               recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
	               recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
               } else {
            	   recorder.setOutputFormat(
                   MediaRecorder.OutputFormat.THREE_GPP);
            	   recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
               }
               
               recorder.setAudioEncodingBitRate(16);                    
               recorder.setAudioSamplingRate(isHighQuality ? 44100:22050);
               
               try {
            	   // create temporary file to store recording
            	   if(type.compareTo("audio/amr") != 0)
            	   tempFile = File.createTempFile("VoiceRecorder", ".3gpp", getExternalFilesDir(null));
            	   else 
            		   tempFile = File.createTempFile("VoiceRecorder", ".amr", getExternalFilesDir(null));
                  
            	   // Stores File as tag for saveButton and deleteButton 
            	   saveButton.setTag(tempFile);
            	   deleteButton.setTag(tempFile);
                  
	               // set the MediaRecorders output file
	               recorder.setOutputFile(tempFile.getAbsolutePath());
	               recorder.prepare(); // prepare to record   
	               recorder.start(); // start recording
	               
	               recording = true; // we are currently recording
                   
	               // handler.post(updateVisualizer); // start updating view
               } catch (IllegalStateException e) {
                  Log.e(TAG, e.toString());
               } 
               catch (IOException e) {
                  Log.e(TAG, e.toString());
               }                
               
               recordButton.setEnabled(true); 
           } 
		   // Record button is not checked 
		   else { /*
			   mPlaySeekBar.setVisibility(View.VISIBLE);
			   mPlaySeekBar.setProgress(0); */ 
			   
			   // Reset the timer 
			   timeSwap = 0L; 
			   myHandler.removeCallbacks(updateTimerMethod);
            	
           	   // recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.record));
            
			   vinyl.setImageDrawable(getResources().getDrawable(R.drawable.vinylrecord_idle));
			   stopRecordPlayingAnimation();
			   
               recorder.stop(); // stop recording
               recorder.reset(); // reset the MediaRecorder
               recording = false; // we are no longer recording
            	
               playButton.setEnabled(true);
               saveButton.setEnabled(true); // enable saveButton
               deleteButton.setEnabled(true); // enable deleteButton
               recordButton.setEnabled(false); // disable recordButton
               mi.setEnabled(true); 
               mi2.setEnabled(true); 
               
               Toast message = Toast.makeText(VoiceRecorder.this, "Save or Delete current audio file before recording again", Toast.LENGTH_SHORT);
               message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
               message.show(); // Display the Toast
               
 		 	  	
               if (recorder != null) {
            	   recorder.release();
            	   recorder = null;
 		 	   }
 		 	  
 		 	   mediaPlayer = new MediaPlayer();
	      	  
 		 	   try {
 		 		   mediaPlayer.setDataSource(tempFile.getAbsolutePath());
 		 	   } catch (IllegalArgumentException e) {
	 			
	 			e.printStackTrace();
	      	  } catch (SecurityException e) {
	 			
	 			e.printStackTrace();
	      	  } catch (IllegalStateException e) {
	 			
	 			e.printStackTrace();
	      	  } catch (IOException e) {
	 			
	 			e.printStackTrace();
	      	  }
	
	            } 
	         } 
	      }; 
  
   
	      OnCheckedChangeListener playButtonListener = new OnCheckedChangeListener() {
	    	  @Override
	          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	    		  if(isChecked) {
	    			   
	   			   startTime = SystemClock.uptimeMillis(); 
	   			   myHandler.postDelayed(updateTimerMethod, 0);
	   			   
		    		  deleteButton.setEnabled(false);
		    		  saveButton.setEnabled(true); 
		    		  recordButton.setEnabled(false);
		    		  
		    		  try{
						  mediaPlayer.prepare();
					  } catch (IllegalStateException e) {
						  e.printStackTrace();
					  } catch (IOException e) {
						  e.printStackTrace();
					  }
		    		  
		    		 // mPlaySeekBar.setMax(mediaPlayer.getDuration());
		    		  
			     	  mediaPlayer.start(); 
			     	  playButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause1));
			     	  saveButton.setEnabled(true);
			     	  
			     	// updateSeekBar();
			     	 
		    	  } else {
		    		  mediaPlayer.stop(); 
		    		  playButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.play1));
		    		  deleteButton.setEnabled(true);
		    		  saveButton.setEnabled(true); 
		    		  recordButton.setEnabled(false);
		    		  timeSwap += timeInMillies; 
					   myHandler.removeCallbacks(updateTimerMethod);
		    	  }	
	    		  
	    		  
	    		  mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
	    		               @Override
	    		               public void onCompletion(MediaPlayer mp) {
	    		                  playButton.setChecked(false); // unchecked state
	    		                  saveButton.setEnabled(true); 
	    		                  // mPlaySeekBar.setProgress(0);
	    		                  textTimer.setText("0:00");
	    		             	  timeSwap = 0L; 
	    		       		   myHandler.removeCallbacks(updateTimerMethod);
	    		               } // end method onCompletion
	    		            } // end OnCompletionListener
	    		         ); // end call to setOnCompletionListener
			  }
	      };
   
	      // Saves a recording
	      OnClickListener saveButtonListener = new OnClickListener() {
	    	  @Override
	    	  public void onClick(final View v) {
	    		  // Gets reference to LayoutInflater service
	    		  LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	         
	    		  // Inflate name_edittext.xml to create an EditText
	    		  View view = inflater.inflate(R.layout.name_edittext, null);
	    		  final EditText nameEditText = (EditText) view.findViewById(R.id.nameEditText);       
	            
	    		  // Creates input dialog to get recording name from user
	    		  AlertDialog.Builder inputDialog = new AlertDialog.Builder(VoiceRecorder.this);
	    		  inputDialog.setView(view); // set the dialog's custom View
	    		  inputDialog.setTitle(R.string.dialog_set_name_title);
	    		  
	    		  inputDialog.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() { 
	    			  public void onClick(DialogInterface dialog, int which) {
	    				  // create a SlideshowInfo for a new slideshow
	    				  String name = nameEditText.getText().toString().trim();
	                  
	    				  // If the audio file name entered is longer than 0 characters long 
	    				  if (name.length() != 0) {
	    					  // create Files for temp file and new file name
	    					  File tempFile = (File) v.getTag();
	    					  File newFile;
	    					  String type=Recorder_preferences.getRecordType(VoiceRecorder.this);
	    					  if(type.compareTo("audio/amr")!=0) {
	    						  newFile = new File(getExternalFilesDir(null).getAbsolutePath() + File.separator + name + ".3gpp");
	    					  } else {
	    						  newFile = new File(getExternalFilesDir(null).getAbsolutePath() + File.separator + name + ".amr");
	    					  }
	                     
	    					  tempFile.renameTo(newFile); // rename the file
		                      saveButton.setEnabled(false); // disable 
		                      deleteButton.setEnabled(false); // disable 
		                      recordButton.setEnabled(true); // enable 
		                      playButton.setEnabled(false); // enable 
		                      
		                      TextView tv=(TextView) findViewById(R.id.tV_timer);
		                      tv.setText("0:00");
		                      
		                      Toast message = Toast.makeText(VoiceRecorder.this, "Audio File Saved", Toast.LENGTH_SHORT);
		                      message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
		                      message.show(); // Display the Toast
	    				  } 
	    				  else // Display message that  must have a name
	    				  {	
	    					  Toast message = Toast.makeText(VoiceRecorder.this, R.string.message_name, Toast.LENGTH_SHORT);
		                      message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
		                      message.show(); // Display the Toast
		                      
		                      recordButton.setEnabled(true); // enable
		                      
	    				  } // end else
	    			  } // end method onClick 
	    		  } // end anonymous inner class
	         ); // end call to setPositiveButton
	         
	         inputDialog.setNegativeButton(R.string.button_cancel, null);
	         inputDialog.show();
	      } // end method onClick
	   }; // end OnClickListener
   
	   // Deletes the temporary recording
	   OnClickListener deleteButtonListener = new OnClickListener() 
	   {
		   @Override
	       public void onClick(final View v) {
			   // Creates an input dialog to get recording name from user
			   AlertDialog.Builder confirmDialog = new AlertDialog.Builder(VoiceRecorder.this);
			   //confirmDialog.setTitle(R.string.dialog_confirm_title); 
			   confirmDialog.setMessage(R.string.dialog_confirm_message); 

			   // If user confirms deletion of audio file 
			   confirmDialog.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() { 
				   public void onClick(DialogInterface dialog, int which) {
					   ((File) v.getTag()).delete(); // Deletes the temp file
					   
					   // mPlaySeekBar.setVisibility(View.INVISIBLE);

					   
					   // Audio file is deleted so these features are disabled 
					   saveButton.setEnabled(false); 
					   deleteButton.setEnabled(false); 
					   playButton.setEnabled(false); 
					   
					   // Audio file is deleted so these features are enabled 
					   recordButton.setEnabled(true); 
					   
					   // Creates an empty new audio file since the old one is deleted 
					   mediaPlayer = new MediaPlayer(); 
					   
					   // Sets the timer back to zero when the file is deleted 
					   TextView tv=(TextView) findViewById(R.id.tV_timer);
	               	   tv.setText("0:00");
	               	   
	               	Toast message = Toast.makeText(VoiceRecorder.this, "Deleted current audio file", Toast.LENGTH_SHORT);
	                message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
	                message.show(); // Display the Toast
	               } // end method onClick 
			   } // end anonymous inner class
			   ); // end call to setPositiveButton
         
			   confirmDialog.setNegativeButton(R.string.button_cancel, null);
	           confirmDialog.show();         
	           recordButton.setEnabled(false); // enable recordButton
	           saveButton.setEnabled(true); 
		   } // end method onClick
	   }; // end OnClickListener
	   
	   private Runnable updateTimerMethod = new Runnable() {
		   public void run() {
			   timeInMillies = SystemClock.uptimeMillis() - startTime;
		       finalTime = timeSwap + timeInMillies;
	
			   int seconds = (int) (finalTime / 1000);
			   int minutes = seconds / 60;
			   seconds = seconds % 60;
			   int milliseconds = (int) (finalTime % 1000);
			   textTimer.setText("" + minutes + ":" + String.format("%02d", seconds));
			   myHandler.postDelayed(this, 0);
		   }
	   };
   /*
   // launch Activity to view saved recordings
   OnClickListener viewSavedRecordingsListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         // launch the SaveRecordings Activity
         Intent intent = 
            new Intent(VoiceRecorder.this, SavedRecordings.class);
         startActivity(intent);
      } // end method onClick
   }; // end OnClickListener
   */ 
	   
	   
	   // Animation Methods   
	   private void startRecordPlayingAnimation() {
		   String speed = Recorder_preferences.getRecordSpeed(VoiceRecorder.this);
		   vinyl.startAnimation(Long.parseLong(speed), true);
		   //vinyl.startAnimation(2500, true); 
	   }
	   
	   private void stopRecordPlayingAnimation() {
		   vinyl.stopAnimation();
	   }
	   /*
	   private Runnable mUpdateSeekBar = new Runnable() {
	        @Override
	        public void run() {
	            if (mediaPlayer.isPlaying()) {
	                updateSeekBar();
	            }
	        }
	    };
	    
	    private void updateSeekBar() {
	        if (mediaPlayer.isPlaying()) {
	            mPlaySeekBar.setProgress(mediaPlayer.getCurrentPosition());
	            seekBarHandler.postDelayed(mUpdateSeekBar, 10);
	        }
	    } */ 
	   
	   
	   
} // end class VoiceRecorder
   