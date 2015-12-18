package jp.ac.kcsfrecrdingequalizer;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class EqualizerView extends Activity{
	private static final float VISUALIZER_HEIGHT_DIP = 50f;

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private Equalizer mEqualizer;
	private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    private TextView mStatusTextView;
    
    Uri filePath;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mStatusTextView = new TextView(this);

        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mStatusTextView);

        path();

        setContentView(mLinearLayout);

    }
    
	
	public void path(){
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();

		File dir = new File(path);
		final File[] files = dir.listFiles();
		final String[] str_items;
		str_items = new String[files.length + 1];
		for (int i = 0; i < files.length; i++) {
		    File file = files[i];
		    str_items[i] = file.getName();
		}
		str_items[files.length] = "キャンセル";

		new AlertDialog.Builder(this)
		.setTitle("ファイルを選択")
		.setItems(str_items, new DialogInterface.OnClickListener(){
		    public void onClick(DialogInterface dialog, int which) {

		        if (which < files.length){
		            File file = files[which];
		            filePath = Uri.fromFile(file);
		            Toast.makeText(EqualizerView.this, 
		                "ファイルパス：「" + filePath + "」を選択しました。", 
		                Toast.LENGTH_LONG).show();
		            
		            
		            
		            mMediaPlayer = MediaPlayer.create(EqualizerView.this, filePath);
		            setupVisualizerFxAndUI();
		            setupEqualizerFxAndUI();

		            mVisualizer.setEnabled(true);

		            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		                public void onCompletion(MediaPlayer mediaPlayer) {
		                    mVisualizer.setEnabled(false);
		                }
		            });

		            mMediaPlayer.start();
		            mStatusTextView.setText("Playing audio...");

		            
		            
		            
		        }
		    }
		}
		)
		.show();
	}


	private void setupEqualizerFxAndUI() {
		
		mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);

        TextView eqTextView = new TextView(this);
        eqTextView.setText("Equalizer:");
        mLinearLayout.addView(eqTextView);

        short bands = mEqualizer.getNumberOfBands();

        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < bands; i++) {
            final short band = i;

            TextView freqTextView = new TextView(this);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");
            mLinearLayout.addView(freqTextView);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + " dB");

            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + " dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(mEqualizer.getBandLevel(band));
            
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);

            mLinearLayout.addView(row);
        }
		
	}

	private void setupVisualizerFxAndUI(){
		
		mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
        mLinearLayout.addView(mVisualizerView);
        
        try{
        	if(mMediaPlayer != null){
        		mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
            	mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                            int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
        	}else{
        		System.err.println("再生に失敗しました。");
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
	
	@Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mEqualizer.release();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.recording, menu);
		return true;
	}

}
