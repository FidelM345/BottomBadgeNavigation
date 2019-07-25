package com.example.badgenotification;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;




import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "thebeast";
    private TextView mTextMessage;
    FirebaseFirestore db;

    Button sendRequest;
    EditText club,points;

    List<Integer>docSizeList=new ArrayList<>();

    AHBottomNavigation bottomNavigation;

    boolean start=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
      //  navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sendRequest=findViewById(R.id.button);
        club=findViewById(R.id.clubs);
        points=findViewById(R.id.points);



        int[] tabColors = getApplicationContext().getResources().getIntArray(R.array.colorsme);
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.nav_view);
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_nav_menu);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);

        db = FirebaseFirestore.getInstance();




       testMe();



        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              String club_name= club.getText().toString().trim();
              String club_points= points.getText().toString().trim();



              if (!TextUtils.isEmpty(club_name)&&!TextUtils.isEmpty(club_points)){

                  Map<String, Object> city = new HashMap<>();
                  city.put("points", club_points);

                  db.collection("clubs").document(club_name)
                          .set(city)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  club.setText("");
                                  points.setText("");
                                  Log.d(TAG, "DocumentSnapshot successfully written!");


                              }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  Log.w(TAG, "Error writing document", e);
                              }
                          });


              }



            }
        });




        // Access a Cloud Firestore instance from your Activity



        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                // Do something cool here...

                if(position==2){

                    testMe();

                    bottomNavigation.setNotification("", 2);
                }


                Log.i(TAG, "onTabSelected position: "+position);
                return true;
            }
        });



    }


    public void testMe(){
        db.collection("clubs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int docSize=task.getResult().size();

                            Log.d(TAG,  "DocSize  => " +docSize );

                            //store data in shared pref
                            SharedPreferences sharedpreferences = getSharedPreferences("notifyme", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt("unix",docSize);
                            editor.commit();

                            bottomNavigation.setNotification("", 2);


                            tryMe();


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }



    public  void tryMe(){
        db.collection("clubs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "New club: " + dc.getDocument().getId()+" data size: "+dc.getDocument().getData().size());

                                    //docSizeList.add();


                                    //get data from shared pref
                                    SharedPreferences sharedPreferences4=getSharedPreferences("notifyme",MODE_PRIVATE);
                                    int sub=sharedPreferences4.getInt("unix",0);


                                    Log.d(TAG, "Subtract value: "+snapshots.size());

                                    int sizeNow=snapshots.size();
                                    int badgeVal=sizeNow-sub;


                                    Log.d(TAG, "Difference value: "+badgeVal);

                                    if(badgeVal>0){

                                        String ansVal=String.valueOf(badgeVal);

                                        bottomNavigation.setNotification(ansVal, 2);
                                    }else{

                                        bottomNavigation.setNotification("", 2);

                                    }


                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified club: " + dc.getDocument().getId()+" data: "+dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed club: " + dc.getDocument().getId()+" data: "+dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });


    }






}
