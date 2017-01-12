package edu.vt.scm.groupsafe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class LeaveGroupDialogFragment extends DialogFragment {

    private LeaveGroupDialogListener callback;

    public interface LeaveGroupDialogListener {
        void leaveGroup();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
            callback = (LeaveGroupDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement LeaveGroupDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_leave_group, null);
        builder.setView(view);

        Button noButton = (Button) view.findViewById(R.id.noButton);
        Button yesButton = (Button) view.findViewById(R.id.yesButton);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                LeaveGroupDialogFragment.this.getDialog().cancel();
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                callback.leaveGroup();
            }
        });

        return builder.create();
    }
}
