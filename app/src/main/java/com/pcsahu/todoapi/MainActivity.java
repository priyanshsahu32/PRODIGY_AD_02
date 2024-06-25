package com.pcsahu.todoapi;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pcsahu.todoapi.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.pcsahu.todoapi.UtilsService.SharedPreferenceClass;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private AppCompatButton logout;
    SharedPreferenceClass sharedPreferenceClass;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    private NavigationView navigationView;
    private TextView user_name,user_email;

    private CircleImageView userImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        sharedPreferenceClass = new SharedPreferenceClass( this );

        drawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout);

        navigationView = (NavigationView) findViewById( R.id.navigationview);

        toolbar = findViewById( R.id.toolbar );



        setSupportActionBar( toolbar );
        View hdview = navigationView.getHeaderView( 0 );

        user_email = (TextView) hdview.findViewById( R.id.emailid );

        user_name = (TextView) hdview.findViewById( R.id.username );

        userImageView = (CircleImageView) hdview.findViewById( R.id.avatar );

        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                setDrawerClick(item.getItemId());
                item.setChecked(true);
                drawerLayout.closeDrawers(  );
                return true;
            }
        } );

        initDrawer();

        getUserProfile();


    }

    private void getUserProfile() {

        String url = "https://todoapii-production.up.railway.app/api/todo/auth/register";
        String token = sharedPreferenceClass.getValue_string( "token" );
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    if(response.getBoolean("success")){
                        JSONObject userObj = response.getJSONObject( "user" );
                        user_name.setText( userObj.getString( "username" ) );
                        user_email.setText( userObj.getString( "email" ) );
                        Picasso.with(getApplicationContext()).load(userObj.getString("avatar"))
                                .placeholder( R.drawable.account)
                                .error( R.drawable.account )
                                .into(userImageView);
                    }
                } catch (JSONException e) {
                   e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this,"ERROR",Toast.LENGTH_SHORT).show();

            }
        } ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();

                headers.put("Content-Type","application/json");
                headers.put("Authorization",token);
                return headers;
            }
        };


        int socketTime = 30000;

        RetryPolicy policy = new DefaultRetryPolicy(socketTime,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy( policy );

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(jsonObjectRequest);








    }


    private void initDrawer() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        ft.replace(R.id.content,new HomeFragment() );
        ft.commit();

        drawerToggle = new ActionBarDrawerToggle( this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed( drawerView );
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened( drawerView );
            }
        };


        drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white ));

        drawerLayout.addDrawerListener( drawerToggle );
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate( savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged( newConfig );
        drawerToggle.onConfigurationChanged(  new Configuration());
    }

    private void setDrawerClick(int itemId){
       if(itemId == R.id.action_finish_Task){
               getSupportFragmentManager().beginTransaction().replace(R.id.content,new FinishedTaskFragment()).commit();

       }
       else if(itemId == R.id.action_home){



           getSupportFragmentManager().beginTransaction().replace( R.id.content,new HomeFragment() ).commit();

       }
       else if(itemId == R.id.action_logout){

           sharedPreferenceClass.clear();
           startActivity( new Intent(MainActivity.this, loginActivity.class) );
           finish();

       }
       else if(itemId == R.id.action_correction){
           Intent eml = new Intent(Intent.ACTION_SEND);
           eml.setType("message/rfc822"); // required for email

           eml.putExtra(Intent.EXTRA_EMAIL,new String[]{"priyanshsahu7828@gmail.com"});
           eml.putExtra( Intent.EXTRA_SUBJECT,"COORECTION IN TODO APP" );

           eml.putExtra( Intent.EXTRA_TEXT,"" );
           startActivity( Intent.createChooser( eml,"EMAIL VIA" ) );
       }

       else if(itemId==R.id.policy){

           String url = "https://www.freeprivacypolicy.com/live/212559a8-4210-4bb5-9573-8190b808a3c6";

           Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

           startActivity( intent );

       }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main_menu,menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.share){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType( "text/plain" );
            String sharebody = "hey share this to do app following link: ";
            intent.putExtra( intent.EXTRA_TEXT,sharebody );

            startActivity( intent.createChooser( intent,"Share via" ));

            return true;
        }
        else if(item.getItemId()==R.id.refresh_menu){
            getSupportFragmentManager().beginTransaction().replace( R.id.content,new HomeFragment() ).commit();

            return true;
        }
        return super.onOptionsItemSelected( item );
    }
}
