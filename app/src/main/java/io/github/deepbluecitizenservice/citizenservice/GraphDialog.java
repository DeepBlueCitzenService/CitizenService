package io.github.deepbluecitizenservice.citizenservice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetJavaScriptEnabled")
public class GraphDialog extends Dialog {

    public static final int ALL_PROBLEMS = 0;

    private List<Pair<String, Integer>> graphData;
    private String graphTitle;
    private WebView webView;
    private Context context;

    public GraphDialog(Context context, String graphTitle, int type) {
        super(context);
        this.graphTitle = graphTitle;
        this.context = context;

        switch (type){
            case ALL_PROBLEMS:
                graphData = getAllStats();
                break;
            default:
                triggerLoadUrl();
                graphData = new ArrayList<>();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_graph);

        webView = (WebView) findViewById(R.id.graph_chart_web_view);
        TextView graphLabel = (TextView) findViewById(R.id.graph_chart_title);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int graphSize = (int) (metrics.widthPixels * 0.8);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(graphSize, graphSize);
        webView.setLayoutParams(layoutParams);

        graphLabel.setText(graphTitle);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.getSettings().setJavaScriptEnabled(true);
    }

    private void triggerLoadUrl(){
        webView.loadUrl("file:///android_res/raw/web_chart.html");

    }

    private class WebAppInterface {

        @JavascriptInterface
        public int getNoOfRows(){
            return graphData.size();
        }

        @JavascriptInterface
        public String getItem(int idx){
            return graphData.get(idx).first;
        }

        @JavascriptInterface
        public int getValue(int idx){
            return graphData.get(idx).second;
        }

    }

    private List<Pair<String, Integer>> getAllStats(){
        final List<Pair<String, Integer>> result = new ArrayList<>(2);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean onceDone = false;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!onceDone){
                    onceDone = true;
                    result.add(new Pair<>(context.getString(R.string.problem_unsolved), (int) dataSnapshot.child("problems").getChildrenCount()));
                    result.add(new Pair<>(context.getString(R.string.problem_solved), (int) dataSnapshot.child("solutions").getChildrenCount()));
                    triggerLoadUrl();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        return result;
    }

}
