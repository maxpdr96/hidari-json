package com.hidarisoft.hidarijson.utils;

import java.util.Map;

public class CreatorFields {

    private CreatorFields(){
    }

    public static String[] convertStringArrays(String value){
        return value.split("\\.");
    }
}
