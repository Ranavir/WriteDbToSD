package com.stlindia.writedbtosd.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by office on 3/21/2016.
 */
public class URIHelper {
    SharedPreferences mPref; // Shared Preferences reference
    SharedPreferences.Editor mEditor; // Editor reference for Shared preferences
    Context mContext; // Context

    private static final String PREFERENCE_NAME = "dburi_pref_file"; // filename

    // All Shared Preferences Keys

    public static final String KEY_URI = "uri"; // URI as string

    final String TAG = this.getClass().getSimpleName()+" " ;
    final String ENTRY_TAG = "ENTRY--->"+this.getClass().getSimpleName() ;
    final String EXIT_TAG = "EXIT--->"+this.getClass().getSimpleName() ;
    /**
     * @param context
     */
    public URIHelper(Context context) {
        Log.i(ENTRY_TAG, "URIHelper");

        this.mContext = context;
        mPref = mContext.getSharedPreferences(PREFERENCE_NAME, mContext.MODE_PRIVATE);
        mEditor = mPref.edit();
        mEditor.commit();

        Log.i(EXIT_TAG, "URIHelper");
    }
    public void setURI(String uri) {
        Log.i(ENTRY_TAG, "setURI");

        System.out.println(TAG + "URI---->" + uri);
        mEditor.putString(KEY_URI, uri);


        // commit changes
        mEditor.commit();
        Log.i(EXIT_TAG, "setURI");
    }

    public String getURI() {
        String uri = "" ;
        Log.i(ENTRY_TAG, "getURI");

        uri = mPref.getString(KEY_URI, "");
        System.out.println(TAG+"uri retrieved---->"+uri);
        Log.i(EXIT_TAG, "getURI");
        return uri;

    }
}
