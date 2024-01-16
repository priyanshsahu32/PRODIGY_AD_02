package com.pcsahu.todoapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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

public class RegisterActivity extends AppCompatActivity {
    private EditText name_ET;
    private EditText password_ET;
    private EditText email_ET;
    private AppCompatButton registerBtn;
    ProgressBar progressBar;
    SharedPreferenceClass sharedPreferenceClass;

    private String name,email,password;

    UtilService utilService = new UtilService();
    private AppCompatButton loginbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );


        name_ET  = findViewById( R.id.nameRegister_ET );
        password_ET  = findViewById( R.id.passwordRegister_ET );
        email_ET = findViewById( R.id.emailRegister_ET );
        registerBtn = findViewById( R.id.RegisterBtn );
        progressBar = findViewById( R.id.progressbarRegister );
        loginbtn = findViewById( R.id.loginRegister );
        sharedPreferenceClass = new SharedPreferenceClass( this );


        loginbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,loginActivity.class);
                startActivity( intent );
            }
        } );

        registerBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utilService.hideKeyboard( v,RegisterActivity.this );
                name = name_ET.getText().toString();
                email = email_ET.getText().toString();
                password = password_ET.getText().toString();

                if(validate( v ) && emailValid(email)){
                    registerUser(v);
                }



            }
        } );





    }

    private boolean emailValid(String email) {
        boolean isvalid =  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if(!isvalid){
            Toast.makeText( this,"Email not Valid",Toast.LENGTH_SHORT ).show();

        }

        return isvalid;
    }

    private void registerUser(View view) {
        progressBar.setVisibility( view.VISIBLE );
//        "name":name,
//        "email":email,
//        "password":password

        HashMap<String,String> params = new HashMap<>();

        params.put( "username",name );

        params.put("email",email);
        params.put("password",password);
        String apikey = "https://todoapii-production.up.railway.app/api/todo/auth/register";

        JsonObjectRequest jor = new JsonObjectRequest( Request.Method.POST, apikey, new JSONObject( params ), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getBoolean( "success" )){
                        String token = response.getString( "token" );

                        sharedPreferenceClass.setValue_string( "token",token );

                        Toast.makeText( RegisterActivity.this,token,Toast.LENGTH_SHORT ).show();

                        startActivity( new Intent(RegisterActivity.this,MainActivity.class) );
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

//                        Toast.makeText(RegisterActivity.this,obj.getString( "msg" ),Toast.LENGTH_SHORT).show();
                        Toast.makeText( RegisterActivity.this,"USER ALREADY REGISTERED",Toast.LENGTH_SHORT ).show();
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


    public boolean validate(View view){
        boolean isValid;

        if(!TextUtils.isEmpty( name )){
            isValid = true;
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


        }else{
            utilService.showSnackBar(view,"please enter the name");
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences todo_pref = getSharedPreferences( "user_todo",MODE_PRIVATE );
        if(todo_pref.contains( "token" ) ){
            startActivity( new Intent(RegisterActivity.this,MainActivity.class ));
            finish();
        }
    }
}