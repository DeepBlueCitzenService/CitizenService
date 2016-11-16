package io.github.deepbluecitizenservice.citizenservice.tensorflow;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TensorFlow {

    //The Parameter we used to train the classifier
    //No of classes = 3 -> garbage, potholes, traffic
    private static final int NUM_CLASSES = 3;
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "Mul:0";
    private static final String OUTPUT_NAME = "final_result:0";

    //For now the model and labels should be in assets folder
    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";

    private AssetManager assetManager;
    private ImageClassifier tfClassifier;

    private boolean initialized = false;

    private TensorFlow(AssetManager assetManager, ImageClassifier classifier) {
        this.assetManager = assetManager;
        this.tfClassifier = classifier;
    }

    public static TensorFlow getInstance(AssetManager assetManager, ImageClassifier classifier){
        return new TensorFlow(assetManager, classifier);
    }

    //Must be called before classifying a Image
    public void initialize() throws IOException {
        if(ImageClassifier.isLibraryPresent() && checkAssets()){
            tfClassifier.initializeTensorFlow(assetManager, MODEL_FILE, LABEL_FILE,
                    NUM_CLASSES, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
            this.initialized = true;
        }
    }

    private boolean checkAssets(){
        try {
            List<String> assetList = Arrays.asList(assetManager.list(""));
            boolean isGraph = assetList.contains(MODEL_FILE
                    .substring(MODEL_FILE.lastIndexOf("/") + 1, MODEL_FILE.lastIndexOf(".")));
            boolean isLabels = assetList.contains(LABEL_FILE.
                    substring(LABEL_FILE.lastIndexOf("/") + 1, LABEL_FILE.lastIndexOf(".")));
            return isGraph && isLabels;
        } catch (IOException e) {
            return false;
        }
    }

    //A private function to resize src Bitmap as per size
    // of dst Bitmap and copies pixel to dst Bitmap
    private void drawResizedBitmap(Bitmap src, Bitmap dst) {
        Canvas canvas = new Canvas(dst);
        Matrix matrix = new Matrix();

        float minDim = Math.min(src.getWidth(), src.getHeight());
        float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
        float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
        float scaleFactor = dst.getHeight() / minDim;

        matrix.preTranslate(translateX, translateY);
        matrix.postScale(scaleFactor, scaleFactor);

        canvas.drawBitmap(src, matrix, null);
    }

    public List<Classifier.Recognition> classify(Bitmap rgbBitmap) {
        if(initialized){
            Bitmap croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);
            drawResizedBitmap(rgbBitmap, croppedBitmap);

            return tfClassifier.recognizeImage(croppedBitmap);
        }
        return null;
    }


    public boolean isInitialized() {
        return initialized;
    }
}
