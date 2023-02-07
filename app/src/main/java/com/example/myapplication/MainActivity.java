package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

//import javafx.util.Pair;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MainActivity extends AppCompatActivity {
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    TextView textView;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.results);
        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                fetchData();
            }
        });
    }

    public void fetchData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://fetch-hiring.s3.amazonaws.com/hiring.json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Response: " + response,Toast.LENGTH_SHORT).show();
                        parseJson(response);
                        Log.d("error", "arr");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });

        queue.add(stringRequest);
    }

    public void parseJson(String response){
        try {
            ArrayList<JSONObject> json_objects = new ArrayList<JSONObject>();
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++){
                json_objects.add(jsonArray.getJSONObject(i));
            }
            Collections.sort(json_objects, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject jsonObj1, JSONObject jsonObj2) {
                    int listID1 = 0;
                    try {
                        listID1 = jsonObj1.getInt("listId");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    int listID2 = 0;
                    try {
                        listID2 = jsonObj2.getInt("listId");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    String name1 = "";
                    try {
                        name1 = jsonObj1.getString("name");
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    String name2 = "";
                    try {
                        name2 = jsonObj2.getString("name");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    if (listID1 > listID2)
                        return 1;
                    else if (listID1 < listID2)
                        return -1;
                    return name1.compareTo(name2);
                }
            });

            Map<Integer, ArrayList<Pair<Integer, String>>> groupbyMap = new HashMap<>();
            for (int i = 0; i < json_objects.size(); i++){
                int idVar = json_objects.get(i).getInt("id");
                Integer listIDVar = json_objects.get(i).getInt("listId");
                String nameVar = json_objects.get(i).getString("name");
                Pair<Integer, String> pairListIdName = new Pair<>(idVar, nameVar);
                groupbyMap.computeIfAbsent(listIDVar, k -> new ArrayList<>()).add(pairListIdName);
            }

            for (Map.Entry<Integer, ArrayList<Pair<Integer, String>>> set :
                    groupbyMap.entrySet()) {

                    textView.append("listId: " + set.getKey() + ", {");
                    boolean commaFlag = false;
                    for (int i = 0; i < set.getValue().size(); i++){
                        String nameVar = set.getValue().get(i).second;
                        if (nameVar.length() == 0 || nameVar == "null") {
                            continue;
                        } else {
                            if (commaFlag) {
                                textView.append(",");
                                commaFlag = true;
                            }
                            textView.append(" id: " + set.getValue().get(i).first + ", name: "
                                    + set.getValue().get(i).second + "\n");
                        }
                    }
                    textView.append(" } \n");
            }

//            for (int i = 0; i < json_objects.size(); i++){
//                String listIDVar, nameVar;
//                int idVar = json_objects.get(i).getInt("id");
//                listIDVar = json_objects.get(i).getString("listId");
//                nameVar = json_objects.get(i).getString("name");
//                if (nameVar.length() == 0 || nameVar == "null") {
//                    continue;
//                } else {
//                    textView.append("Name: " + nameVar + ", id: " + idVar + ", listID: " + listIDVar + "\n");
//                }
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}