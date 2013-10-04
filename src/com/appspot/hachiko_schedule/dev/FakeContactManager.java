package com.appspot.hachiko_schedule.dev;

import android.content.Context;
import android.content.res.XmlResourceParser;
import com.appspot.hachiko_schedule.friends.ContactManager;
import com.appspot.hachiko_schedule.R;
import com.appspot.hachiko_schedule.data.FriendItem;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kazuki Nishiura
 */
public class FakeContactManager extends ContactManager {
    private Context context;

    public FakeContactManager(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public List<FriendItem> getListOfContactEntries() {
        List<FriendItem> listOfContacts
                = new ArrayList<FriendItem>();
        XmlResourceParser parser = context.getResources().getXml(R.xml.dummy_people_100);
        try {
            int eventType = parser.getEventType();
            String displayName = null;
            final String NAME_TAG = "name";
            final String RECORD_TAG = "record";
            int fakeId = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && NAME_TAG.equals(parser.getName())) {
                    displayName = parser.nextText();
                } else if (eventType == XmlPullParser.END_TAG && RECORD_TAG.equals(parser.getName())) {
                    listOfContacts.add(new FriendItem(fakeId++, displayName, null, "hoge@fuga.js"));
                }
                eventType = parser.next();
            }
        } catch (Exception e) {

        }
        return listOfContacts;
    }
}
