package edu.vt.scm.groupsafe;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity
                                  implements SignupDialogFragment.SignupDialogListener {

    private DialogFragment dialog;
    private NetWorker netWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        netWorker = netWorker.getSInstance();

        final EditText usernameEditText = (EditText) findViewById(R.id.username);
        final EditText passwordEditText = (EditText) findViewById(R.id.password);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        TextView signupText = (TextView) findViewById(R.id.signupText);

        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        final SplashScreenActivity activity = this;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                final String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!username.equals("") && !password.equals("")) {

                    Map<String,String> params = new HashMap<String, String>();
                    params.put("username", username);
                    params.put("password", password);

                    String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/users/login";
                    netWorker.post(url, params, new NetWorker.VolleyCallback() {
                        @Override
                        public void onSuccess(String response) {

                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(String string) {

                            Context context = getBaseContext();
                            CharSequence text = "ERROR LOGGING IN";
                            int duration = Toast.LENGTH_SHORT;

                            Toast.makeText(context, text, duration).show();
                        }
                    });
                }
            }
        });

        signupText.setPaintFlags(signupText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dialog = new SignupDialogFragment();
                dialog.show(getFragmentManager(), "signup");
            }
        });
    }

    @Override
    public void signup(String username, String password, String firstName, String lastName,
                       String email, String phoneNumber) {

        dialog.dismiss();

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("phoneNumber", phoneNumber);
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("password", password);

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/users/signup";

        netWorker.post(url, params, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                Context context = getBaseContext();
                CharSequence text = "ACCOUNT CREATED";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }

            @Override
            public void onFailure(String error) {

                Context context = getBaseContext();
                CharSequence text = "ERROR CREATING ACCOUNT";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        });
    }
}
