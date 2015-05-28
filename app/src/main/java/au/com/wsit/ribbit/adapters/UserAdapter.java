package au.com.wsit.ribbit.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import au.com.wsit.ribbit.R;
import au.com.wsit.ribbit.utils.MD5Util;
import au.com.wsit.ribbit.utils.ParseConstants;

/**
 * Created by guyb on 20/01/15.
 */
public class UserAdapter extends ArrayAdapter<ParseUser>
{
    protected Context mContext;
    protected List<ParseUser> mUsers;

    public static final String TAG = UserAdapter.class.getSimpleName();

    public UserAdapter(Context context, List<ParseUser> users)
    {
        super(context, R.layout.message_item, users);
        mContext = context;
        mUsers = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;


        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
            holder = new ViewHolder();
            holder.userImageView = (ImageView)convertView.findViewById(R.id.userImageView);
            holder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            holder.checkImageView = (ImageView) convertView.findViewById(R.id.checkImageView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set the data in the view
        ParseUser user = mUsers.get(position);
        String email = user.getEmail().toLowerCase();

        if(email.equals(""))
        {
            holder.userImageView.setImageResource(R.drawable.avatar_empty);
        }
        else
        {
            String hash = MD5Util.md5Hex(email);
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash + "?s=204&d=404";

            Picasso.with(mContext).load(gravatarUrl).placeholder(R.drawable.avatar_empty).into(holder.userImageView);

        }

        Date createdAt = user.getCreatedAt();
        long now = new Date().getTime();
        String convertedDate = DateUtils.getRelativeTimeSpanString(createdAt.getTime(), now, DateUtils.SECOND_IN_MILLIS).toString();

        GridView gridView = (GridView)parent;

        if (gridView.isItemChecked(position))
        {
            holder.checkImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.checkImageView.setVisibility(View.INVISIBLE);
        }

        holder.nameLabel.setText(user.getUsername());

        return convertView;
    }

    private static class ViewHolder
    {
        ImageView userImageView;
        ImageView checkImageView;
        TextView nameLabel;

    }

    public void refill(List<ParseUser> users)
    {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

}
