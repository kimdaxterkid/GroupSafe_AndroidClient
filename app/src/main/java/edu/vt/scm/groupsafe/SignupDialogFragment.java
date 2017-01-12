package edu.vt.scm.groupsafe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignupDialogFragment extends DialogFragment {

    private SignupDialogListener callback;

    public interface SignupDialogListener {
        void signup(String username, String password, String firstName, String lastName,
                    String email, String phoneNumber);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_signup, null);
        builder.setView(view);

        callback = (SplashScreenActivity) getActivity();

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        Button signupButton = (Button) view.findViewById(R.id.signupButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                SignupDialogFragment.this.getDialog().cancel();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                EditText editText = (EditText) view.findViewById(R.id.username);
                String username = editText.getText().toString();
                editText = (EditText) view.findViewById(R.id.password);
                String password = editText.getText().toString();
                editText = (EditText) view.findViewById(R.id.firstName);
                String firstName = editText.getText().toString();
                editText = (EditText) view.findViewById(R.id.lastName);
                String lastName = editText.getText().toString();
                editText = (EditText) view.findViewById(R.id.email);
                String email = editText.getText().toString();
                editText = (EditText) view.findViewById(R.id.phoneNumber);
                String phoneNumber = editText.getText().toString();

                if (!username.equals("") && !password.equals("") && !firstName.equals("") &&
                        !lastName.equals("") && !email.equals("") && !phoneNumber.equals("")) {
                    callback.signup(username, password, firstName, lastName, email, phoneNumber);
                }
            }
        });

        return builder.create();
    }
}