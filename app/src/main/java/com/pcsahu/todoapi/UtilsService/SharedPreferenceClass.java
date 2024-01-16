package com.pcsahu.todoapi.UtilsService;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
public class SharedPreferenceClass{

    private static final String USER_PREF = "user_todo";
    private SharedPreferences appShared;
    private SharedPreferences.Editor prefsEditor;

    public SharedPreferenceClass(Context context){
        appShared = context.getSharedPreferences( USER_PREF , Activity.MODE_PRIVATE );
        this.prefsEditor = appShared.edit();

    }

    public int getValue_int(String key){
        return appShared.getInt( key,0 );
    }

    public void setValue_int(String key,int val){
        prefsEditor.putInt( key,val ).commit();
    }


    public String getValue_string(String key){
        return appShared.getString( key,"");
    }

    public void setValue_string(String key,String val){
        prefsEditor.putString( key,val ).commit();
    }



    public boolean getValue_bool(String key){
        return appShared.getBoolean( key,false);
    }

    public void setValue_bool(String key,boolean val){
        prefsEditor.putBoolean( key,val ).commit();
    }

    public void clear(){
        prefsEditor.clear().commit();
    }




}
