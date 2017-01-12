package edu.vt.scm.groupsafe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateGroupDialogFragment extends DialogFragment {

    private CreateGroupDialogListener callback;

    public interface CreateGroupDialogListener {
        void createGroup(String groupName, int vicinity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
            callback = (CreateGroupDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement CreateGroupDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_create_group, null);
        builder.setView(view);

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        Button createButton = (Button) view.findViewById(R.id.createButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                CreateGroupDialogFragment.this.getDialog().cancel();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                EditText editText = (EditText) view.findViewById(R.id.groupName);
                String groupName = editText.getText().toString();
                editText = (EditText) view.findViewById(R.id.groupVicinity);
                if (!groupName.equals("") && !editText.getText().toString().equals("")) {
                    int groupVicinity = Integer.parseInt(editText.getText().toString());
                    callback.createGroup(groupName, groupVicinity);
                }
            }
        });

        return builder.create();
    }
}
