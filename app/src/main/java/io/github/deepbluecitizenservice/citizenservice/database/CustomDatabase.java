package io.github.deepbluecitizenservice.citizenservice.database;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.deepbluecitizenservice.citizenservice.data.Problem;

public class CustomDatabase{
    private DatabaseReference db;
    public static final int DB_OPTION_OPEN = 1111, DB_OPTION_SOLVED=22222;

    public CustomDatabase(DatabaseReference reference){
        this.db = reference;
    }

    //Create a new user- only called once during first login
    public void createUser(String name, String email, String id, Uri photoURL){
        UserModel user = new UserModel(name, email, photoURL);
        db.child("users").child(id).setValue(user);
    }

    //Create a new problem
    public void createProblem(String url, int status, double locationX, double locationY, String location,
                              String creatorKey, long SLA, long timeCreated, String description, int category){

        ProblemModel problem = new ProblemModel(url, status, locationX, locationY, location, creatorKey,
                SLA, timeCreated, description, category);

        String key = db.child("problems").push().getKey();
        HashMap<String, Object> mp = new HashMap<>();

        mp.put(key, false);

        String place;

        if(status==Problem.STATUS_SOLVED){
            place=ProblemModel.SOLVED_PROBLEM;
        }
        else{
            place=ProblemModel.OPEN_PROBLEM;
        }

        db.child("users").child(creatorKey).child(place+"Problems").updateChildren(mp);
        db.child("problems").child(key).setValue(problem);
    }

    public void updateProblemToSolved(String uid, String problemId, Uri SolutionURL){
        db.child("users").child(uid).child(ProblemModel.OPEN_PROBLEM).removeValue();
        db.child("users").child(uid).child(ProblemModel.SOLVED_PROBLEM).setValue(problemId);
        db.child("problems").child(problemId).child("solutionUrl").setValue(SolutionURL);
    }
}