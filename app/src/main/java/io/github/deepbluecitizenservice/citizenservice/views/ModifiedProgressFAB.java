package io.github.deepbluecitizenservice.citizenservice.views;

import android.content.Context;
import android.util.AttributeSet;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.progressarc.ProgressArcView;

import java.lang.reflect.Field;

public class ModifiedProgressFAB extends FABProgressCircle {

    public ModifiedProgressFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void show() {
        super.show();
        setArcVisible(VISIBLE);
    }

    @Override
    public void hide() {
        super.hide();
        setArcVisible(GONE);
    }

    // HACK
    private void setArcVisible(int visibility){
        try {
            Field f = getClass().getSuperclass().getDeclaredField("progressArc");
            f.setAccessible(true);
            ((ProgressArcView) f.get(this)).setVisibility(visibility);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
