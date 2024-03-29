package tw.singtracy;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

public class RegisterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_register, menu);
		return true;
	}
	
	public void register(View v){
		String username = ((EditText) findViewById(R.id.reg_username)).getText().toString();
		String password = ((EditText) findViewById(R.id.reg_password)).getText().toString();
		(new RegisterTask()).execute(username, password);
	}
	
	private class RegisterTask extends AsyncTask<String, Void, String>{
		private ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = new ProgressDialog(RegisterActivity.this);
			dialog.setMessage("Please wait...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				KiiUser user = KiiUser.builderWithName(params[0]).build();
				user.register(params[1]);
				
				return user.getAccessToken();
			} catch (BadRequestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnauthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ForbiddenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConflictException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UndefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(String token){
			dialog.dismiss();
			
			Intent intent = new Intent();
			intent.putExtra("token", token);
			
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
