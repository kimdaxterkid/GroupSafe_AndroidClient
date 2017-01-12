package edu.vt.scm.groupsafe;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class GroupAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private MainActivity activity;

    public GroupAdapter(Context context, MainActivity activity) {

        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return activity.group.getMemberList().size();
    }

    @Override
    public Object getItem(int position) {
        return activity.group.getMemberList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = inflater.inflate(R.layout.list_item_group_member, parent, false);

        TextView titleTextView =
                (TextView) rowView.findViewById(R.id.group_member_title);
        TextView subtitleTextView =
                (TextView) rowView.findViewById(R.id.group_member_subtitle);
        TextView detailTextView =
                (TextView) rowView.findViewById(R.id.group_member_detail);
        ImageView thumbnailImageView =
                (ImageView) rowView.findViewById(R.id.group_member_thumbnail);

        GroupMember groupMember = (GroupMember) getItem(position);

        titleTextView.setText(groupMember.getUsername());
        subtitleTextView.setText(groupMember.getName());

        if (activity.group.host == groupMember) {
            detailTextView.setText("Group Leader");
        } else {
            detailTextView.setText("");
        }

        //Bitmap bitmap = retrieveContactPhoto(context, groupMember.getPhoneNumber());
        //thumbnailImageView.setImageBitmap(bitmap);

        //Temporarily no contact image, requires extra permission work for Android 6.0
        //although above 2 statements should work for anything less
        thumbnailImageView.setImageResource(R.mipmap.ic_launcher);

        return rowView;
    }

    private static Bitmap retrieveContactPhoto(Context context, String number) {

        ContentResolver contentResolver = context.getContentResolver();
        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            cursor.close();
        }

        Bitmap photo = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }

            assert inputStream != null;
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return photo;
    }
}
