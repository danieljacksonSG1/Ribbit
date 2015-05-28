package au.com.wsit.ribbit.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import au.com.wsit.ribbit.adapters.UserAdapter;
import au.com.wsit.ribbit.utils.ParseConstants;
import au.com.wsit.ribbit.R;


public class EditFriendsActivity extends Activity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);

        mGridView = (GridView) findViewById(R.id.friendsGrid);

        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        mGridView.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e)
            {
                if (e == null)
                {
                    // Success
                    setProgressBarIndeterminateVisibility(false);
                    mUsers = users;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for(ParseUser user : mUsers)
                    {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    if(mGridView.getAdapter() == null)
                    {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mGridView.setAdapter(adapter);
                    }
                    else
                    {
                        ((UserAdapter)mGridView.getAdapter()).refill(mUsers);
                    }


                    addFriendsCheckmarks();


                }
                else
                {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setMessage(e.getMessage())
                           .setTitle(R.string.generic_error_title)
                           .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }


            }
        });


    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    } */
//
//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//
//
//    }

    private void addFriendsCheckmarks()
    {
            mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> friends, ParseException e)
                {
                        if (e == null)
                        {
                            // List returned
                            for (int i = 0; i < mUsers.size(); i++)
                            {
                                ParseUser user = mUsers.get(i);
                                for (ParseUser friend : friends)
                                {
                                    if (friend.getObjectId().equals(user.getObjectId()))
                                    {
                                        mGridView.setItemChecked(i, true);

                                    }
                                }
                            }
                        }
                        else
                        {
                            Log.e(TAG, e.getMessage());
                        }
                }
            });

    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);


        if (mGridView.isItemChecked(position))
        {
            // add a friend
            mFriendsRelation.add(mUsers.get(position));
            checkImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            // Remove friend
            mFriendsRelation.remove(mUsers.get(position));
            checkImageView.setVisibility(View.INVISIBLE);

        }
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    Log.e(TAG, e.getMessage());
                }

            }
        });

        }
    };
}
