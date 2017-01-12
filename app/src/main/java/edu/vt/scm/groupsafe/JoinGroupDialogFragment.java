package edu.vt.scm.groupsafe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class JoinGroupDialogFragment extends DialogFragment {

    private JoinGroupDialogListener callback;

    public interface JoinGroupDialogListener {
        void joinGroup(String groupName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
            callback = (JoinGroupDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement JoinGroupDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_join_group, null);
        builder.setView(view);

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        Button joinButton = (Button) view.findViewById(R.id.joinButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                JoinGroupDialogFragment.this.getDialog().cancel();
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                EditText editText = (EditText) view.findViewById(R.id.groupName);
                String groupName = editText.getText().toString();
                if (!groupName.equals("")) {
                    callback.joinGroup(groupName);
                }
            }
        });

        return builder.create();
    }
}