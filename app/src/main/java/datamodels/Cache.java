package datamodels;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Shamyyoun on 3/29/2015.
 */
public class Cache {
    /**
     * method used to update active user reg_id in SP
     */
    public static void updateRegId(Context context, String value) {
        updateCachedString(context, Constants.SP_CONFIG, Constants.SP_KEY_REG_ID, value);
    }

    /**
     * method used to get active user reg_id from SP
     */
    public static String getRegId(Context context) {
        return getCachedString(context, Constants.SP_CONFIG, Constants.SP_KEY_REG_ID);
    }

    /*
     * method, used to update string value in SP
     */
    private static void updateCachedString(Context context, String spName, String valueName, String value) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(valueName, value);
        editor.commit();
    }

    /*
     * method, used to get cached String from SP
     */
    private static String getCachedString(Context context, String spName, String valueName) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String value = sp.getString(valueName, null);

        return value;
    }
}
