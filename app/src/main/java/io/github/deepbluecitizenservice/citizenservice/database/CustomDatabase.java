package io.github.deepbluecitizenservice.citizenservice.database;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CustomDatabase{
    private DatabaseReference db;

    public CustomDatabase(DatabaseReference reference){
        this.db = reference;
    }

    //Create a new user- only called once during first login
    public void createUser(String name, String email, String id, Uri photoURL){
        final UserModel user = new UserModel(name, email, photoURL);
        final DatabaseReference userRef = db.child("users").child(id);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    userRef.setValue(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Create a new problem
    public void createProblem(String key, String url, int status, double locationX, double locationY, String location,
                              String creatorKey, long SLA, long timeCreated, String description, int category,
                              String creatorName, String creatorUrl, String solutionUrl){
        ProblemModel problem = new ProblemModel(url, status, locationX, locationY, location, creatorKey,
                SLA, timeCreated, description, category, creatorName, creatorUrl, solutionUrl);

        String place;

        if(status==ProblemModel.STATUS_SOLVED){
            place=ProblemModel.SOLVED_PROBLEM;
        }
        else{
            place=ProblemModel.OPEN_PROBLEM;
        }



        if(status==ProblemModel.STATUS_UNSOLVED) {
            db.child("users").child(creatorKey).child(place+"Problems").child(key).setValue(problem);
            db.child("problems").child(key).setValue(problem);
        }
        else {
            String solKey= db.child("solutions").push().getKey();

            db.child("solutions").child(solKey).setValue(problem);
            db.child("users").child(creatorKey).child(place+"Problems").child(solKey).setValue(problem);
        }
    }

    public void updateProblemToSolved(final String uid, final String problemId, final String SolutionURL, final long timeCreated){
        Log.d("CUSTOM", "CALLED DELETE");

        db.child("problems").orderByKey().startAt(problemId).endAt(problemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot ds : dataSnapshot.getChildren()){
                    ProblemModel problem = ds.getValue(ProblemModel.class);
                    createProblem(problem.getKey(), problem.url, ProblemModel.STATUS_SOLVED, problem.locationX,
                            problem.locationY, problem.locationAddress, problem.creatorKey, problem.sla,
                            timeCreated, problem.description, problem.category, problem.creatorName,
                            problem.creatorURL, SolutionURL);

                    db.child("problems")
                            .child(ds.getKey())
                            .removeValue();

                    db.child("users")
                            .child(uid)
                            .child(ProblemModel.OPEN_PROBLEM+"Problems")
                            .child(ds.getKey())
                            .removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}