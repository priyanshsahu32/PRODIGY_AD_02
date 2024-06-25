package com.pcsahu.todoapi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pcsahu.todoapi.Adapters.FinishedTaskAdapter;
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

public class FinishedTaskFragment extends Fragment implements RecycleViewClickListener {


    SharedPreferenceClass sharedPreferenceClass;

    EditText title_field;

    RecyclerView recyclerView;

    TextView empty_tv;
    ProgressBar progressBar;


    String token;

    ArrayList<TodoModel> arrayList;

    FinishedTaskAdapter finishedTaskAdapter;
    TodoListAdapter todoListAdapter;
    public FinishedTaskFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.fragment_finished_task, container, false );

        sharedPreferenceClass = new SharedPreferenceClass( getContext() );
        token = sharedPreferenceClass.getValue_string( "token" );
        recyclerView = view.findViewById( R.id.recycler_view );
        empty_tv = view.findViewById( R.id.empty_tv );
        progressBar = view.findViewById( R.id.progressbar2 );


        recyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
        recyclerView.setHasFixedSize( true );


        getTask();


        return view;

    }

    private void getTask() {


        arrayList = new ArrayList<>();

        progressBar.setVisibility( View.VISIBLE );
        String url = "https://prodigy-ad-02.onrender.com/api/todo/";

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

                            finishedTaskAdapter = new FinishedTaskAdapter(getActivity(),arrayList,FinishedTaskFragment.this);
                            recyclerView.setAdapter( finishedTaskAdapter );

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
                    e.printStackTrace();
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

    private void deleteTodo(String id, int position) {

        String url  ="https://prodigy-ad-02.onrender.com/api/todo/"+id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean( "success" )){
                        Toast.makeText( getActivity(), response.getString( "msg" ), Toast.LENGTH_SHORT ).show();
                        arrayList.remove( position );

                        finishedTaskAdapter.notifyItemRemoved( position );
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

    public void showDeleteDialog(final String id, final int position) {


        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate( R.layout.custom_dialog_layout,null  );




        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle( "Are you want to delete this finished task?" )
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


    @Override
    public void onItemClick(int position) {

        FinishedTaskAdapter.MyViewHolder viewHolder = (FinishedTaskAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition( position );
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

    }

    @Override
    public void onEditButtonClick(int position) {

    }

    @Override
    public void onDeleteButtonClick(int position) {

        showDeleteDialog(arrayList.get(position).getId(),position);

    }

    @Override
    public void onDoneButtonClick(int position) {

    }
}
