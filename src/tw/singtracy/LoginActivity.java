package tw.singtracy;

import java.io.IOException;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
	
	public void login(View view){
		String username = ((EditText) findViewById(R.id.username)).getText().toString();
		String password = ((EditText) findViewById(R.id.password)).getText().toString();
		(new LoginTask()).execute(username, password);
	}
	
	public void register(View view){
		startActivityForResult(new Intent(this, RegisterActivity.class), 1);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 1){
			setResult(RESULT_OK, data);
			finish();
		}
	}
	
	private class LoginTask extends AsyncTask<String, Void, String>{
		private ProgressDialog dialog;
		String error;
		
		protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setMessage("Please wait...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				KiiUser user = KiiUser.logIn(params[0], params[1]);
				return user.getAccessToken();
			} catch (BadRequestException e) {
				error = e.getMessage();
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
			
			if(token != null){
				Intent intent = new Intent();
				intent.putExtra("token", token);
				
				setResult(RESULT_OK, intent);
				finish();
			}else{
				Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
