package edu.vt.scm.groupsafe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GroupFragment extends Fragment
                           implements JoinGroupDialogFragment.JoinGroupDialogListener,
                                      CreateGroupDialogFragment.CreateGroupDialogListener,
                                      LeaveGroupDialogFragment.LeaveGroupDialogListener {

    private View view;
    private DialogFragment dialog;
    ListView listView;

    private MainActivity activity;
    private NetWorker netWorker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_group, container, false);

        Button joinGroupButton = (Button) view.findViewById(R.id.joinGroupButton);
        Button createGroupButton = (Button) view.findViewById(R.id.createGroupButton);
        Button leaveGroupButton = (Button) view.findViewById(R.id.leaveGroupButton);

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dialog = new JoinGroupDialogFragment();
                dialog.setTargetFragment(GroupFragment.this, 0);
                dialog.show(getFragmentManager(), "joingroup");
            }
        });
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dialog = new CreateGroupDialogFragment();
                dialog.setTargetFragment(GroupFragment.this, 0);
                dialog.show(getFragmentManager(), "creategroup");
            }
        });
        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dialog = new LeaveGroupDialogFragment();
                dialog.setTargetFragment(GroupFragment.this, 0);
                dialog.show(getFragmentManager(), "leavegroup");
            }
        });

        listView = (ListView) view.findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

                if (activity.username.equals(activity.group.getMemberList().get(pos).getUsername())) {
                    return;
                }
                final int index = pos;
                PopupMenu popup = new PopupMenu(activity, getViewByPosition(pos, listView));
                popup.getMenuInflater().inflate(R.menu.click_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals(getString(R.string.call))) {
                            callUser(activity.group.getMemberList().get(index));
                            return true;
                        } else if (item.getTitle().equals(getString(R.string.text))) {
                            textUser(activity.group.getMemberList().get(index));
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {

                if (activity.username.equals(activity.group.getMemberList().get(pos).getUsername())) {
                    return true;
                }
                if (activity.isGroupLeader)  {
                    final int index = pos;
                    PopupMenu popup = new PopupMenu(activity, getViewByPosition(pos, listView));
                    popup.getMenuInflater().inflate(R.menu.long_click_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals(getString(R.string.promote))) {
                                promoteUser(activity.group.getMemberList().get(index));
                                return true;
                            } else if (item.getTitle().equals(getString(R.string.kick))) {
                                kickUser(activity.group.getMemberList().get(index));
                                return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
                return true;
            }
        });

        activity = (MainActivity) getActivity();
        netWorker = activity.netWorker;

        return view;
    }

    @Override
    public void joinGroup(final String groupName) {

        dialog.dismiss();

        Map<String,String> params = new HashMap<String, String>();
        params.put("members", activity.username);

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/groups/" +
                groupName + "/";

        netWorker.put(url, params, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject json = new JSONObject(response);
                    String vicinity = json.getString("range");
                    String host = json.getString("host");

                    activity.group = new Group(groupName, Integer.parseInt(vicinity));

                    JSONArray members = json.getJSONArray("members");
                    for (int i = 0; i < members.length(); i++) {
                        JSONObject member = members.getJSONObject(i);
                        String username = member.getString("username");
                        String name = member.getString("firstName") + " " + member.getString("lastName");
                        String phoneNumber = member.getString("phoneNumber");

                        GroupMember user = new GroupMember(username, phoneNumber, name, null);

                        activity.group.memberList.add(user);
                        if (username.equals(host)) {
                            activity.group.host = user;
                        }
                    }

                    listView.setAdapter(new GroupAdapter(getContext(), activity));

                    LinearLayout joinCreateLayout = (LinearLayout) view.findViewById(R.id.joinCreateButtonLayout);
                    LinearLayout leaveLayout = (LinearLayout) view.findViewById(R.id.leaveButtonLayout);
                    LinearLayout listTitleLayout = (LinearLayout) view.findViewById(R.id.listTitleLayout);
                    TextView listTitle = (TextView) view.findViewById(R.id.listTitle);

                    joinCreateLayout.setVisibility(View.GONE);
                    leaveLayout.setVisibility(View.VISIBLE);
                    listTitleLayout.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.VISIBLE);

                    listTitle.setText(groupName);

                    activity.mChatFragment.connectToServer();
                    activity.mMapFragment.initializeMap();

                } catch (Exception e) {
                    //ERROR: response should be a JSON
                }
            }

            @Override
            public void onFailure(String string) {

                Context context = activity.getBaseContext();
                CharSequence text = "ERROR JOINING GROUP";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        });
    }

    @Override
    public void createGroup(final String groupName, final int groupVicinity) {

        dialog.dismiss();

        Map<String,String> params = new HashMap<String, String>();
        params.put("groupName", groupName);
        params.put("range", Integer.toString(groupVicinity));
        params.put("host", activity.username);

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/groups";

        netWorker.post(url, params, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject json = new JSONObject(response);

                    activity.group = new Group(groupName, groupVicinity);

                    JSONArray members = json.getJSONArray("members");
                    for (int i = 0; i < members.length(); i++) {
                        JSONObject member = members.getJSONObject(i);
                        String username = member.getString("username");
                        String name = member.getString("firstName") + " " + member.getString("lastName");
                        String phoneNumber = member.getString("phoneNumber");

                        GroupMember user = new GroupMember(username, phoneNumber, name, null);

                        activity.group.memberList.add(user);
                        activity.group.host = user;
                    }

                    listView.setAdapter(new GroupAdapter(getContext(), activity));

                    LinearLayout joinCreateLayout = (LinearLayout) view.findViewById(R.id.joinCreateButtonLayout);
                    LinearLayout leaveLayout = (LinearLayout) view.findViewById(R.id.leaveButtonLayout);
                    LinearLayout listTitleLayout = (LinearLayout) view.findViewById(R.id.listTitleLayout);
                    TextView listTitle = (TextView) view.findViewById(R.id.listTitle);

                    joinCreateLayout.setVisibility(View.GONE);
                    leaveLayout.setVisibility(View.VISIBLE);
                    listTitleLayout.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.VISIBLE);

                    listTitle.setText(groupName);

                    activity.isGroupLeader = true;

                    activity.mChatFragment.connectToServer();
                    activity.mMapFragment.initializeMap();

                } catch (Exception e) {
                    //ERROR: response should be a JSON
                }
            }

            @Override
            public void onFailure(String error) {

                Context context = activity.getBaseContext();
                CharSequence text = "ERROR CREATING GROUP";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        });
    }

    @Override
    public void leaveGroup() {

        dialog.dismiss();

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/groups/" +
                activity.group.getGroupName() + "?members=" + activity.username;

        netWorker.delete(url, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                activity.group = null;

                LinearLayout joinCreateLayout = (LinearLayout) view.findViewById(R.id.joinCreateButtonLayout);
                LinearLayout leaveLayout = (LinearLayout) view.findViewById(R.id.leaveButtonLayout);
                LinearLayout listTitleLayout = (LinearLayout) view.findViewById(R.id.listTitleLayout);

                joinCreateLayout.setVisibility(View.VISIBLE);
                leaveLayout.setVisibility(View.GONE);
                listTitleLayout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);

                activity.mChatFragment.disconnectToServer();
                activity.mMapFragment.destroyMap();
            }

            @Override
            public void onFailure(String string) {

                Context context = activity.getBaseContext();
                CharSequence text = "ERROR LEAVING GROUP";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        });
    }

    private void promoteUser(final GroupMember user) {

        //TODO
        Map<String,String> params = new HashMap<String, String>();
        params.put("host", user.getUsername());

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/groups/" +
                activity.group.getGroupName() + "/";

        netWorker.put(url, params, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                activity.isGroupLeader = false;
                activity.mChatFragment.hostUpdate(user.getUsername());

                Context context = activity.getBaseContext();
                CharSequence text = "USER PROMOTED";
                int duration = Toast.LENGTH_SHORT;

                //Toast.makeText(context, text, duration).show();
            }

            @Override
            public void onFailure(String string) {

                Context context = activity.getBaseContext();
                CharSequence text = "ERROR PROMOTING USER";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        });
    }

    private void kickUser(GroupMember user) {

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/groups/" +
                activity.group.getGroupName() + "?members=" + user.getUsername();

        netWorker.delete(url, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                Context context = activity.getBaseContext();
                CharSequence text = "USER KICKED";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }

            @Override
            public void onFailure(String string) {

                Context context = activity.getBaseContext();
                CharSequence text = "ERROR KICKING USER";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration).show();
            }
        });
    }

    private void callUser(GroupMember user) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + user.getPhoneNumber()));
            startActivity(intent);
        }
    }

    private void textUser(GroupMember user) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + user.getPhoneNumber()));
        startActivity(intent);
    }

    private View getViewByPosition(int pos, ListView listView) {

        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
