package edu.vt.scm.groupsafe;

import android.app.NotificationManager;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.widget.BaseAdapter;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    GroupFragment mGroupFragment;
    MapFragment mMapFragment;
    ChatFragment mChatFragment;
    SettingsFragment mSettingsFragment;

    String username;
    Location location;
    Group group;
    Boolean isGroupLeader;
    NetWorker netWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Group"));
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        final PagerAdapter adapter = new PagerAdapter(this, getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        username = getIntent().getExtras().getString("username");
        location = null;
        group = null;
        netWorker = netWorker.getSInstance();
        isGroupLeader = false;
    }

    public void userJoined(final String username) {

        String url = "http://ec2-54-158-251-62.compute-1.amazonaws.com:8080/users/" + username;

        netWorker.get(url, new NetWorker.VolleyCallback() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject json = new JSONObject(response);
                    String name = json.getString("firstName") + " " + json.getString("lastName");
                    String phoneNumber = json.getString("phoneNumber");

                    GroupMember user = new GroupMember(username, phoneNumber, name, null);

                    group.memberList.add(user);
                    ((BaseAdapter) mGroupFragment.listView.getAdapter()).notifyDataSetChanged();

                } catch (Exception e) {
                    //ERROR: server must send json
                }
            }

            @Override
            public void onFailure(String string) { connectionToServerLost(); }
        });
    }

    public void userLeft(String username) {

        for (GroupMember member : group.memberList) {
            if (member.getUsername().equals(username)) {
                group.memberList.remove(member);
                ((BaseAdapter) mGroupFragment.listView.getAdapter()).notifyDataSetChanged();
                mMapFragment.updateMap("left", member);
                return;
            }
        }
    }

    public void userPromoted(String username) {

        for (GroupMember member : group.memberList) {
            if (member.getUsername().equals(username)) {
                group.host = member;
                ((BaseAdapter) mGroupFragment.listView.getAdapter()).notifyDataSetChanged();
                mMapFragment.updateMap("newHost", member);
                if (username.equals(this.username)) {
                    isGroupLeader = true;
                }
                return;
            }
        }
    }

    public void userLocationChanged(String username, Location location) {

        for (GroupMember member : group.memberList) {
            if (member.getUsername().equals(username)) {
                member.setLocation(location);
                mMapFragment.updateMap("location", member);

                if (username.equals(group.host.getUsername())) {

                    //TODO:Check distance from every member?

                } else if (group.host.getLocation() != null) {

                    float distance = member.getLocation().distanceTo(group.host.getLocation());

                    if (distance <= group.getVicinity()) {
                        return;
                    }

                    NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MyApplication.getAppContext())
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Member Wandered!")
                            .setContentText(member.getUsername() + "has left the range of " +
                                    group.getVicinity() + " meters.");
                    mBuilder.setAutoCancel(true);

                    NotificationManager nm =
                            (NotificationManager) MyApplication.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(123456, mBuilder.build());
                }
                return;
            }
        }
    }

    public void connectionToServerLost() {

        //TODO: kick user back to login screen
    }
}
