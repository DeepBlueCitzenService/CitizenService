package io.github.deepbluecitizenservice.citizenservice;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

class ImageClassifier {

    private static final String serverURL =
            "https://deepblue-tensorflow.herokuapp.com/classify_image/classify/";

    private URL connectURL;
    private String imagePath;
    private FileInputStream fileInputStream;

    ImageClassifier(String imgPath) throws MalformedURLException, FileNotFoundException {
        this.fileInputStream = new FileInputStream(imgPath);
        this.imagePath = imgPath;
        connectURL = new URL(serverURL);
    }

    List<Pair<String, Float>> uploadAndClassify() throws IOException, JSONException {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + imagePath +"\"" + lineEnd);
        dos.writeBytes(lineEnd);

        int maxBufferSize = 1024;
        int bytesAvailable = fileInputStream.available();
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            dos.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable,maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0,bufferSize);
        }

        fileInputStream.close();

        dos.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);
        dos.flush();
        dos.close();

        if(conn.getResponseCode() == 200){
            return parseResponse(conn.getInputStream());
        }
        else {
            return null;
        }
    }

    private List<Pair<String, Float>> parseResponse(InputStream is) throws IOException, JSONException {
        List<Pair<String, Float>> result = new ArrayList<>();

        StringBuilder jsonSB =new StringBuilder();
        for(int ch; ( ch = is.read() ) != -1;){ jsonSB.append( (char)ch); }

        String jsonString = jsonSB.toString();

        JSONObject responseObject = new JSONObject(jsonString);
        boolean success = responseObject.getBoolean("success");

        if(success){
            Iterator<String> keys = responseObject.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if(Objects.equals(key, "success")) continue;
                String strValue = responseObject.getString(key);
                Float value = Float.parseFloat(strValue);
                result.add(new Pair<>(key, value));
            }
            Collections.sort(result, new Comparator<Pair<String, Float>>() {
                @Override
                public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                    if(o1.second > o2.second) return -1;
                    else return 1;
                }
            });
        }
        else return null;

        return result;

    }

    static int getBestCategory(List<Pair<String, Float>> result){
        return ProblemModel.getCategory(result.get(0).first);
    }
}
