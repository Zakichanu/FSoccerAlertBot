package com.ztobbal.Commands;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import twitter4j.conf.ConfigurationBuilder;


public class Functions {




    // Execution des requêtes api
    public static String executeRequest(String type, String url) throws UnirestException {
        HttpResponse<String> apiResponse = null;
        if(type.equals("get")){
             apiResponse = Unirest.get(url)
                    .header(Constants.RAPIDAPIKEY.HEADER_KEY_NAME, Constants.RAPIDAPIKEY.HEADER_KEY_VALUE)
                    .header(Constants.RAPIDAPIKEY.HEADER_HOST_NAME, Constants.RAPIDAPIKEY.HEADER_HOST_VALUE)
                    .asString();


        }

        if(apiResponse != null){
            return parsing_requete(apiResponse.getBody());
        }else{
            return null;
        }



    }
    
    public static String parsing_requete(String response){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(response);
        response = gson.toJson(je);
        return response;
    }

    public static void configTwitter4J(ConfigurationBuilder twitter4JConfigBuilder){
        twitter4JConfigBuilder.setDebugEnabled(true);
        twitter4JConfigBuilder.setOAuthConsumerKey(Constants.TwitterKEY.O_AUTH_CONSUMER_KEY);
        twitter4JConfigBuilder.setOAuthConsumerSecret(Constants.TwitterKEY.O_AUTH_CONSUMER_SECRET);
        twitter4JConfigBuilder.setOAuthAccessToken(Constants.TwitterKEY.ACCESS_TOKEN);
        twitter4JConfigBuilder.setOAuthAccessTokenSecret(Constants.TwitterKEY.ACCESS_TOKEN_SECRET);

    }




}
