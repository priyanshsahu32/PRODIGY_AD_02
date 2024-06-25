package com.pcsahu.todoapi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pcsahu.todoapi.Adapters.TodoListAdapter;
import com.pcsahu.todoapi.UtilsService.SharedPreferenceClass;
import com.pcsahu.todoapi.interfaces.RecycleViewClickListener;
import com.pcsahu.todoapi.models.TodoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements RecycleViewClickListener {

    FloatingActionButton floatingActionButton;

    SharedPreferenceClass sharedPreferenceClass;

    EditText title_field;

    RecyclerView recyclerView;

    TextView empty_tv;
    ProgressBar progressBar;


    String token;

    ArrayList<TodoModel> arrayList;

    TodoListAdapter todoListAdapter;

    TodoListAdapter.MyViewHolder myViewHolder;


    public HomeFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.fragment_home, container, false );
        floatingActionButton = view.findViewById( R.id.add_task );

        sharedPreferenceClass = new SharedPreferenceClass( getContext() );
        token = sharedPreferenceClass.getValue_string( "token" );
        recyclerView = view.findViewById( R.id.recycler_view );
        empty_tv = view.findViewById( R.id.empty_tv );
        progressBar = view.findViewById( R.id.progressbar2 );


        floatingActionButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        } );


        recyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
        recyclerView.setHasFixedSize( true );

        getTask();


        return view;
    }

    private void getTask() {
        arrayList = new ArrayList<>();

        progressBar.setVisibility( View.VISIBLE );
        String url = "https://prodigy-ad-02.onrender.com/api/todo";

        JsonObjectRequest jor = new JsonObjectRequest( Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean( "success" )) {
                        JSONArray jsonArray = response.getJSONArray( "todos" );

                        if(jsonArray.length()==0){

                            empty_tv.setVisibility( View.VISIBLE );

                        }
                        else{
                            empty_tv.setVisibility( View.GONE );
                            for(int i = 0;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject( i );

                                TodoModel todoModel = new TodoModel( jsonObject.getString( "_id" ),jsonObject.getString( "title" ),jsonObject.getString( "description" ) );
                                arrayList.add(todoModel );


                            }

                            todoListAdapter = new TodoListAdapter(getActivity(),arrayList,HomeFragment.this);
                            recyclerView.setAdapter( todoListAdapter );

                        }

                    }
                    progressBar.setVisibility( View.GONE );
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility( View.GONE );
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText( getActivity(),error.toString(),Toast.LENGTH_SHORT ).show();
                NetworkResponse response = error.networkResponse;

                if(error==null || error.networkResponse == null){
                    return;
                }

                String body;

//                final String statusCode = String.valueOf( error.networkResponse.statusCode );

                try{
                    body  = new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject = new JSONObject(body);

                    Toast.makeText( getActivity(),"Error"+body,Toast.LENGTH_SHORT );


                    if(errorObject.getString( "msg" ).equals( "Token not valid" )){
                        sharedPreferenceClass.clear();
                        startActivity( new Intent(getActivity(), loginActivity.class) );
                        Toast.makeText(getActivity(),"Session expired",Toast.LENGTH_SHORT).show();
                    }

                }catch (UnsupportedEncodingException | JSONException e){

                }

                progressBar.setVisibility(View.GONE );

            }
        } ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();

                headers.put( "Content-Type", "application/json" );
                headers.put( "Authorization", token );
                return headers;
            }
        };

        int socketTime = 3000;

        RetryPolicy policy = new DefaultRetryPolicy( socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT );

        jor.setRetryPolicy( policy );

        RequestQueue rq = Volley.newRequestQueue( getContext() );
        rq.add( jor );


    }

    private void showAlertDialog() {



        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate( R.layout.custom_dialog_layout,null  );
        final EditText title_field = alertLayout.findViewById(R.id.title);

        final EditText description_field = alertLayout.findViewById(R.id.description);


        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView( alertLayout )
                .setTitle( "ADD TASK" )
                .setPositiveButton( "Add",null )
                .setNegativeButton( "Cancel",null )
                .create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveBtn = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String titlestr = title_field.getText().toString();
                        String desc = description_field.getText().toString();

                        if (!TextUtils.isEmpty(titlestr)) {
                            addTask(titlestr, desc);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Please enter title", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });



        dialog.show();






    }

    public void showUpdateDialog(final String id, String title, String description) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate( R.layout.custom_dialog_layout,null );

        final EditText title_field = alertLayout.findViewById(R.id.title);

        final EditText description_field = alertLayout.findViewById(R.id.description);

        title_field.setText( title );
        description_field.setText( description );

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView( alertLayout )
                .setTitle( "Update TASK" )
                .setPositiveButton( "Update",null )
                .setNegativeButton( "Cancel",null )
                .create();
        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button positiveBtn = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String titlestr = title_field.getText().toString();
                        String desc = description_field.getText().toString();

                        updateTask(id,titlestr,desc);
                        dialog.dismiss();
                    }
                });

            }
        } );

        alertDialog.show();





    }

    public void showFinishedDialog(String id, int position) {


        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate( R.layout.custom_dialog_layout,null  );




        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle( "Move to finished task" )
                .setPositiveButton( "Yes",null )
                .setNegativeButton( "No",null )
                .create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveBtn = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        finishTodo(arrayList.get(position).getId(),position);
                        dialog.dismiss();

                    }
                });
            }
        });

        dialog.show();

    }

    private void finishTodo(String id, int position) {


        String url  ="https://prodigy-ad-02.onrender.com/api/todo/"+id;

        HashMap<String,String> body = new HashMap<>();

        body.put( "finished","true" );


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.PUT, url, new JSONObject( body ), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if(response.getBoolean( "success" )){
                        arrayList.remove( position );
                        getTask();


                        todoListAdapter.notifyItemRemoved( position );
                        Toast.makeText( getActivity(),response.getString( "msg" ),Toast.LENGTH_SHORT );



                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText( getActivity(),error.toString(),Toast.LENGTH_SHORT );
            }
        } ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("Content-Type","application/json");

                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy( new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) );
        RequestQueue requestQueue = Volley.newRequestQueue( getContext() );

        requestQueue.add(jsonObjectRequest);



    }

    public void showDeleteDialog(final String id, final int position) {


        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate( R.layout.custom_dialog_layout,null  );




        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle( "Are you want to delete the task?" )
                .setPositiveButton( "Yes",null )
                .setNegativeButton( "No",null )
                .create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveBtn = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        deleteTodo(arrayList.get(position).getId(),position);
                        dialog.dismiss();

                    }
                });
            }
        });

        dialog.show();

    }

    private void updateTask(String id, String title, String desc) {
        String url  ="https://prodigy-ad-02.onrender.com/api/todo/"+id;

        HashMap<String,String> body = new HashMap<>();

        body.put( "title",title );
        body.put("description",desc);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.PUT, url, new JSONObject( body ), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if(response.getBoolean( "success" )){
                        getTask();
                        Toast.makeText( getActivity(),"Updated Task Successfull",Toast.LENGTH_SHORT );

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText( getActivity(),error.toString(),Toast.LENGTH_SHORT );
            }
        } ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();

                params.put("Content-Type","application/json");

                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy( new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) );
        RequestQueue requestQueue = Volley.newRequestQueue( getContext() );

        requestQueue.add(jsonObjectRequest);







        }


    private void deleteTodo(String id, int position) {

        String url  ="https://prodigy-ad-02.onrender.com/api/todo/"+id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean( "success" )){
                        Toast.makeText( getActivity(), response.getString( "msg" ), Toast.LENGTH_SHORT ).show();
                        arrayList.remove( position );

                        todoListAdapter.notifyItemRemoved( position );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        jsonObjectRequest.setRetryPolicy( new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) );
        RequestQueue requestQueue = Volley.newRequestQueue( getContext() );

        requestQueue.add(jsonObjectRequest);
    }

    private void addTask(String title, String desc) {

        String Url = "https://prodigy-ad-02.onrender.com/api/todo";

        HashMap<String,String> body = new HashMap<>();

        body.put( "title",title );
        body.put("description",desc);

        JsonObjectRequest jor = new JsonObjectRequest( Request.Method.POST, Url, new JSONObject(body), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getBoolean( "success" )){
                        Toast.makeText( getActivity(),"Task Added Successfully",Toast.LENGTH_SHORT );
                        getTask();

                    }

                }catch (JSONException e){
                    e.printStackTrace();

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

                        Toast.makeText(getActivity(),obj.getString( "msg" ),Toast.LENGTH_SHORT).show();

                    }catch(JSONException | UnsupportedEncodingException je){
                        je.printStackTrace();

                    }
                }

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

        jor.setRetryPolicy( policy );

        RequestQueue rq = Volley.newRequestQueue(getContext());
        rq.add(jor);


    }




    @Override
    public void onItemClick(int position) {

        TodoListAdapter.MyViewHolder viewHolder = (TodoListAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            if (viewHolder.accordian_body.getVisibility() == View.VISIBLE) {
                viewHolder.accordian_body.setVisibility(View.GONE);
            } else {
                viewHolder.accordian_body.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onLongItemClick(int position) {

        showUpdateDialog(arrayList.get(position).getId(),arrayList.get(position).getTitle(),arrayList.get( position ).getDescription());

    }

    @Override
    public void onEditButtonClick(int position) {
        showUpdateDialog(arrayList.get(position).getId(),arrayList.get(position).getTitle(),arrayList.get( position ).getDescription());
    }



    @Override
    public void onDeleteButtonClick(int position) {

        showDeleteDialog(arrayList.get(position).getId(),position);


    }




    @Override
    public void onDoneButtonClick(int position) {
        showFinishedDialog(arrayList.get(position).getId(),position);

    }


}
