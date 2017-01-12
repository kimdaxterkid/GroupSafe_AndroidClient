package edu.vt.scm.groupsafe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView tv = (TextView) view.findViewById(R.id.tv_settings);

        String URL = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/users/";

        NetWorker nw = NetWorker.getSInstance();
        nw.get(URL, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                tv.setText(result);
            }

            @Override
            public void onFailure(String result) {
            }
        });

        return view;
    }
}