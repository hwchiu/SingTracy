package tw.singtracy;

import java.io.IOException;
import java.net.Socket;

import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.view.Menu;

import android.media.MediaRecorder;
import android.view.View;

public class RecordActivity extends Activity {

	private MediaRecorder recorder;
	private boolean isRecording = false;
	private Socket senderSocket;
	
	public void record(View view) throws IOException{
		if(!this.isRecording) {
			this.startRecording(Environment.getExternalStorageDirectory().toString() + "/haha.3gpp");
		}
		else {
			this.stopRecording();
		}
	}
	
	protected void startRecording(String file) throws IOException{
		if(!this.isRecording) {
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			// recorder.setOutputFile(file);
			senderSocket = new Socket("127.0.0.1", 8888);
			ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(senderSocket);
			
			recorder.setOutputFile(pfd.getFileDescriptor());
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.isRecording = true;
			recorder.start();
		}
	}
	
	protected void stopRecording(){
		if(this.isRecording) {
			this.isRecording = false;
			try{
				recorder.stop();
			    recorder.release(); 
			    this.senderSocket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.audio, menu);
		return true;
	}

	
	
}
