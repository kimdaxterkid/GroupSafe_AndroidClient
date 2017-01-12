package edu.vt.scm.groupsafe;

import android.location.Location;

public class GroupMember {

    private String username;
    private String phoneNumber;
    private String name;
    private Location location;

    public GroupMember(String username, String phoneNumber, String name, Location location) {

        this.username = username;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.location = location;
    }

    public String getUsername() { return username; }

    public String getPhoneNumber() { return phoneNumber; }

    public String getName() { return name; }

    public Location getLocation() { return location; }

    public void setLocation(Location location) { this.location = location; }
}
