package au.com.wsit.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;

import au.com.wsit.ribbit.ui.MainActivity;
import au.com.wsit.ribbit.utils.ParseConstants;

/**
 * Created by guyb on 7/01/15.
 */
public class RibbitApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "xfiPsrmC7fyfntYRi8Z6ICCLFNt76afwHwJRaVnk", "z43rBzgYkKZdTHEi2Sw631AaBXfPQWE1dbndAalM");

        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void updateParseInstallation(ParseUser user)
    {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }
}
