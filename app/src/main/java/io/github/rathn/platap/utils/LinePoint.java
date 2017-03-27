package io.github.rathn.platap.utils;

import io.github.rathn.platap.NotYetImplementedException;

/**
 * Created by Neri Ortez on 23/11/2016.
 */
public class LinePoint extends Line{
    private float x;
    private float y;

    public LinePoint(){
        throw new NotYetImplementedException();
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setShowPoint(boolean b, int color) {
        throw new NotYetImplementedException();
    }
}
