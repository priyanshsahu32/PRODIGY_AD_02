package com.pcsahu.todoapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pcsahu.todoapi.UtilsService.SharedPreferenceClass;
import com.pcsahu.todoapi.UtilsService.UtilService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class loginActivity extends AppCompatActivity {
    public  AppCompatButton loginbtn;


    private EditText password_ET;
    private EditText email_ET;
    private AppCompatButton registerBtn;


    private String email,password;

    UtilService utilService = new UtilService();
    ProgressBar progressBar;

    SharedPreferenceClass sharedPreferenceClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );
        registerBtn  = findViewById( R.id.registerLogin );
        loginbtn = findViewById( R.id.loginBtn );
        password_ET = findViewById( R.id.password_ET );
        email_ET = findViewById( R.id.email_ET );
        progressBar = findViewById( R.id.progressbar );
        sharedPreferenceClass = new SharedPreferenceClass( this );

        registerBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginActivity.this,RegisterActivity.class);
                startActivity( intent );
            }
        } );


        loginbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilService.hideKeyboard( v,loginActivity.this);

                email = email_ET.getText().toString();
                password = password_ET.getText().toString();

                if(validate( v )){
                    loginUser(v);
                }
            }
        } );
    }

    private void loginUser(View view) {
        progressBar.setVisibility( view.VISIBLE );
//        "name":name,
//        "email":email,
//        "password":password

        HashMap<String,String> params = new HashMap<>();



        params.put("email",email);
        params.put("password",password);
        String apikey = "https://todoapii-production.up.railway.app/api/todo/auth/login";

        JsonObjectRequest jor = new JsonObjectRequest( Request.Method.POST, apikey, new JSONObject( params ), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getBoolean( "success" )){
                        String token = response.getString( "token" );

                        sharedPreferenceClass.setValue_string( "token",token );


                        Toast.makeText( loginActivity.this,token,Toast.LENGTH_SHORT ).show();

                        startActivity( new Intent(loginActivity.this,MainActivity.class) );

                    }
                    progressBar.setVisibility( View.GONE );
                }catch (JSONException e){
                    e.printStackTrace();
                    progressBar.setVisibility( View.GONE );
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;

                if(error instanceof ServerError && response !=null){
                    try{
                        String resp = new String(response.data, HttpHeaderParser.parseCharset( response.headers,"utf-8" ) );

                        JSONObject obj = new JSONObject(resp);

                        Toast.makeText(loginActivity.this,obj.getString( "msg" ),Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility( View.GONE );
                    }catch(JSONException | UnsupportedEncodingException je){
                        je.printStackTrace();
                        progressBar.setVisibility( View.GONE );
                    }
                }

            }
        } ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();

                headers.put("Content-Type","application/json");

                return params;
            }
        };

        int socketTime = 3000;

        RetryPolicy policy = new DefaultRetryPolicy(socketTime,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jor.setRetryPolicy( policy );

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(jor);


    }

    private boolean validate(View view) {

        boolean isValid;


        if(!TextUtils.isEmpty( email )){
            isValid = true;

            if(!TextUtils.isEmpty( password )){
                isValid = true;
            }else{
                utilService.showSnackBar(view,"please enter the password");
                isValid = false;
            }
        }else{
            utilService.showSnackBar(view,"please enter the email");
            isValid = false;
        }




        return isValid;

    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences todo_pref = getSharedPreferences( "user_todo",MODE_PRIVATE );
        if(todo_pref.contains( "token" ) ){
            startActivity( new Intent(loginActivity.this,MainActivity.class ));
            finish();
        }
    }
}