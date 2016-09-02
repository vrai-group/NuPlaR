package ortigiaenterprises.platerecognizer;

import android.graphics.Bitmap;
import android.os.Parcelable;

import org.opencv.core.Rect;

import java.io.Serializable;

/**
 * Created by DionisioII on 19/07/2016.
 */
public class OcrRes {
    private Bitmap bp;
    private String text;
    private boolean used;
    private Rect boundaries;
    private int meanConfidence;
    private double latitude;
    private double longitude;
    private String dateTime;
    private String survey;
    private String operator;

    public OcrRes(Bitmap bp, String text,Rect boundaries) {
        this.bp = bp;
        this.text = text;
        this.boundaries=boundaries;
        this.used=false;
    }

    public OcrRes(Bitmap bp, String text,double latitude,double longitude,int meanConfidence,String operator,String survey,String dateTime) {
        this.bp = bp;
        this.text = text;
        this.latitude = latitude;
        this.longitude=longitude;
        this.dateTime=dateTime;
        this.survey=survey;
        this.operator=operator;
        this.meanConfidence=meanConfidence;

        this.used=false;
    }


    public Bitmap getBp() {
        return bp;
    }

    public void setBp(Bitmap bp) {
        this.bp = bp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Rect getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(Rect boundaries) {
        this.boundaries = boundaries;
    }

    public int getMeanConfidence() {
        return meanConfidence;
    }

    public void setMeanConfidence(int meanConfidence) {
        this.meanConfidence = meanConfidence;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getSurvey() {
        return survey;
    }

    public void setSurvey(String survey) {
        this.survey = survey;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
