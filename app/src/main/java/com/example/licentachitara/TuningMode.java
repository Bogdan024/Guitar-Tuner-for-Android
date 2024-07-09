package com.example.licentachitara;

import java.io.Serializable;

public class TuningMode implements Serializable {
    private String name;
    private String firstString;
    private String secondString;
    private String thirdString;
    private String fourthString;
    private String fifthString;
    private String sixthString;

    public TuningMode(String name, String firstString, String secondString, String thirdString, String fourthString, String fifthString, String sixthString) {
        this.name = name;
        this.firstString = firstString;
        this.secondString = secondString;
        this.thirdString = thirdString;
        this.fourthString = fourthString;
        this.fifthString = fifthString;
        this.sixthString = sixthString;
    }

    public String getName() {
        return name;
    }

    public String getFirstString() {
        return firstString;
    }

    public String getSecondString() {
        return secondString;
    }

    public String getThirdString() {
        return thirdString;
    }

    public String getFourthString() {
        return fourthString;
    }

    public String getFifthString() {
        return fifthString;
    }

    public String getSixthString() {
        return sixthString;
    }
}
