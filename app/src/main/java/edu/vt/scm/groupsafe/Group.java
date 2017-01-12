package edu.vt.scm.groupsafe;

import java.util.ArrayList;

public class Group {

    private String groupName;
    private int vicinity;

    ArrayList<GroupMember> memberList;
    GroupMember host;

    public Group(String groupName, int vicinity) {

        this.groupName = groupName;
        this.vicinity = vicinity;
        memberList = new ArrayList<GroupMember>();
        host = null;
    }

    public String getGroupName() { return groupName; }

    public int getVicinity() { return vicinity; }

    public ArrayList<GroupMember> getMemberList() { return memberList; }

    public void removeMember(String username) {

        GroupMember removee = null;
        for (GroupMember member : memberList) {
            if (member.getUsername().equals(username)) {
                removee = member;
                break;
            }
        }
        memberList.remove(removee);
    }
}