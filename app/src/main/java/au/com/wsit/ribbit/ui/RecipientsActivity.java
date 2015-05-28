package au.com.wsit.ribbit.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import au.com.wsit.ribbit.adapters.UserAdapter;
import au.com.wsit.ribbit.utils.FileHelper;
import au.com.wsit.ribbit.utils.ParseConstants;
import au.com.wsit.ribbit.R;


public class RecipientsActivity extends ActionBarActivity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected List<ParseUser> mFriends;
    protected MenuItem mSendMenuItem;
    protected ListView RecipientsList;
    protected Uri mMediaUri;
    protected String mFileType;
    protected GridView mGridView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_grid);

        mGridView = (GridView)findViewById(R.id.friendsGrid);
        mGridView.setOnItemClickListener(mOnItemClickListener);
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Get the data set from the activity that started this one (MainActivity)
        mMediaUri = getIntent().getData();
        mFileType = getIntent().getStringExtra(ParseConstants.KEY_FILE_TYPE);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);





    }



    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e)
            {
                setProgressBarIndeterminateVisibility(false);
                if (e == null)
                {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for(ParseUser user : mFriends)
                    {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    if(mGridView.getAdapter() == null)
                    {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    }
                    else
                    {
                        ((UserAdapter)mGridView.getAdapter()).refill(mFriends);
                    }


                }
                else
                {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.generic_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        // Get the send menuItem when we create the action bar so we can hide and show it
        mSendMenuItem = menu.getItem(0);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            ParseObject message = createMessage();
            if (message == null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.generic_error_title);
                builder.setMessage(R.string.file_error);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();

                dialog.show();
            }
            send(message);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected ParseObject createMessage()
    {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null)
        {
            return null;
        }
        else
        {
            if(mFileType.equals(ParseConstants.TYPE_IMAGE))
            {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }


    }

    protected ArrayList<String>getRecipientIds()
    {
        ArrayList<String>recipientIds = new ArrayList<String>();
        for (int i = 0; i < mGridView.getCheckedItemCount(); i++)
        {
            if (mGridView.isItemChecked(i))
            {
                recipientIds.add(mFriends.get(i).getObjectId());
            }

        }
        return recipientIds;
    }

    protected void send(ParseObject message)
    {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                {
                    // Success
                    Toast.makeText(RecipientsActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
                    sendPushNotifications();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setTitle(R.string.generic_error_title)
                           .setMessage(R.string.error_sending_message)
                           .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(mGridView.getCheckedItemCount() > 0)
                {
                    getActionBar();

                    mSendMenuItem.setVisible(true);


                }
                else
                {
                    mSendMenuItem.setVisible(false);
                }

            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position))
            {
                // add a recipient

                checkImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                // Remove the recipient

                checkImageView.setVisibility(View.INVISIBLE);

            }

        }
    };

    protected void sendPushNotifications()
    {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        // Send push notifications
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.new_message) + ParseUser.getCurrentUser().getUsername());
        push.sendInBackground();
    }
}
