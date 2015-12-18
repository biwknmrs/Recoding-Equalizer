package jp.ac.kcsfrecrdingequalizer;


import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Recording extends Activity {
		
	private static String FILE_PATH;
	MediaRecorder recoder;
	Chronometer chrono;
	ImageButton recButton, stopButton, playButton;
	TextView text;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recording);
		
		chrono = (Chronometer)findViewById(R.id.chronometer);
		text = (TextView)findViewById(R.id.text);
        
        recButton = (ImageButton)this.findViewById(R.id.rec_button);
        recButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(recoder == null){

                	
                	recoder = new MediaRecorder();
                	
                	LayoutInflater factory = LayoutInflater.from(Recording.this);
        	        final View inputView = factory.inflate(R.layout.input_dialog, null);
                	
                	new AlertDialog.Builder(Recording.this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("録音")
                    .setView(inputView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	
    	                    chrono.setBase(SystemClock.elapsedRealtime());
    	                    
                        	EditText et = (EditText)inputView.findViewById(R.id.dialog_edittext);
    	                	String file = et.getText().toString();
    	                	
    	                	FILE_PATH = Environment.getExternalStorageDirectory() + "/" + file + ".mp3";
    	                	
    	                    recoder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	                    recoder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    	                    recoder.setOutputFile(FILE_PATH);
    	                    recoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    	                    
    	                    try {
    	                        recoder.prepare();
    	                    } catch (IllegalStateException e) {
    	                        e.printStackTrace();
    	                    } catch (IOException e) {
    	                        e.printStackTrace();
    	                    }
    	                    
    	                    try{
    	                    	Toast.makeText(Recording.this, "ファイル名：" + file, Toast.LENGTH_SHORT).show();
    	                    	Toast.makeText(Recording.this, "録音開始", Toast.LENGTH_SHORT).show();

    		                    text.setText("ファイル名:" + file);
    		                    recoder.start();
    		                    chrono.start();
    	                    }catch(RuntimeException e){
    	                    	e.printStackTrace();
    	                    	Toast.makeText(Recording.this, "録音エラー", Toast.LENGTH_SHORT).show();
    	                    	text.setText("");
    	                    	onDestroy();
    	                    	}
    	                	
                        }
                    })
                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
            	}
            }
                    
        });
        
        stopButton = (ImageButton)this.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(recoder != null){
            		Toast.makeText(Recording.this, "録音終了", Toast.LENGTH_SHORT).show();
                    recoder.stop();
                    recoder.reset();
                    recoder.release();
                    
                    recoder = null;
                    
                    chrono.stop();
            	}
            }
        });
        
        playButton = (ImageButton)this.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(recoder == null){
            		Intent intent = new Intent(Recording.this,
                			jp.ac.kcsfrecrdingequalizer.EqualizerView.class);

                    startActivity(intent);
            	}
            }
        });
    }
	
	@Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() && recoder != null) {
            recoder.reset();
            recoder.release();
            recoder = null;
            
            chrono.stop();
            chrono.setBase(SystemClock.elapsedRealtime());
            super.onDestroy();
        }
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.recording, menu);
		return true;
	}

}
