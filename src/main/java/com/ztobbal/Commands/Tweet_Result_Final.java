package com.ztobbal.Commands;

import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.UploadMediaResponse;

import java.awt.*;
import java.io.File;
import java.util.*;

import static com.ztobbal.Commands.Constants.TwitterLib.twitterClientR59;

public class Tweet_Result_Final {
    public String pathToDirOfBoardFinal, hashatag, pathToTemplateBoard, pathToBall;
    public int hour, minute, idLeague;
    public Color fontForBoard;


    public Tweet_Result_Final(String pathToDirOfBoardFinal, String hashatag, String pathToTemplateBoard, String pathToBall, int hour, int minute, int idLeague, Color fontForBoard) {
        this.pathToDirOfBoardFinal = pathToDirOfBoardFinal;
        this.hashatag = hashatag;
        this.pathToTemplateBoard = pathToTemplateBoard;
        this.pathToBall = pathToBall;
        this.hour = hour;
        this.minute = minute;
        this.idLeague = idLeague;
        this.fontForBoard = fontForBoard;
    }

    public void Tweet(Match_Result match_result)  {

        try{
            // Recuperation des images des tableaux de scores
            match_result.Show_Board();

            // Condition dans le cas il y a au moins un match
            if(match_result.fixtures.size() != 0){
                //   Corps manuscrit du tweet
                Tweet tweetParent = twitterClientR59.postTweet("⏰ [#"+getHashatag()+"] Today's results : ");

                // Parcours des images et tweet de celle-ci
                File repertoire  = new File(getPathToDirOfBoardFinal());
                int index = 0;
                Tweet response, oldResponse = null;
                for(final File fileEntry : Objects.requireNonNull(repertoire.listFiles())){
                    UploadMediaResponse img = twitterClientR59.uploadMedia(fileEntry, MediaCategory.TWEET_IMAGE);
                    if(index == 0){
                        response = twitterClientR59.postTweet("", tweetParent.getId(), img.getMediaId());
                        oldResponse = response;
                    }else{
                        response = twitterClientR59.postTweet("", oldResponse.getId(), img.getMediaId());
                        oldResponse = response;
                    }
                    //Suppression du fichier
                    if(fileEntry.delete()){
                        System.out.println(new Date() +" : Delete of "+ fileEntry.getName() +" successful");
                    }

                    index++;
                }
                match_result.fixtures.clear();
            }
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }

    }

    public void Tasking_Tweet(){

        Timer timer = new Timer();
        TimerTask tt = new TimerTask(){
            public void run(){
                Calendar cal = Calendar.getInstance(); //this is the method you should use, not the Date(), because it is desperated.

                int hour = cal.get(Calendar.HOUR_OF_DAY);//get the hour number of the day, from 0 to 23


                int minute = cal.get(Calendar.MINUTE);
                if(hour == getHour() && (minute >= getMinute() && minute <getMinute()+10)){
                    try {
                        Match_Result match_result = new Match_Result(getIdLeague(), getPathToTemplateBoard(), getPathToDirOfBoardFinal(), getPathToBall(), getFontForBoard());
                        Tweet(match_result);
                        match_result.fixtures.clear();

                    } catch (Exception e) {
                        System.out.print(new Date() + " : ");
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.schedule(tt, 1000, 1000*10*60);//


    }

    public String getPathToDirOfBoardFinal() {
        return pathToDirOfBoardFinal;
    }

    public void setPathToDirOfBoardFinal(String pathToDirOfBoardFinal) {
        this.pathToDirOfBoardFinal = pathToDirOfBoardFinal;
    }

    public String getHashatag() {
        return hashatag;
    }

    public void setHashatag(String hashatag) {
        this.hashatag = hashatag;
    }

    public String getPathToTemplateBoard() {
        return pathToTemplateBoard;
    }

    public void setPathToTemplateBoard(String pathToTemplateBoard) {
        this.pathToTemplateBoard = pathToTemplateBoard;
    }

    public String getPathToBall() {
        return pathToBall;
    }

    public void setPathToBall(String pathToBall) {
        this.pathToBall = pathToBall;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getIdLeague() {
        return idLeague;
    }

    public void setIdLeague(int idLeague) {
        this.idLeague = idLeague;
    }

    public Color getFontForBoard() {
        return fontForBoard;
    }

    public void setFontForBoard(Color fontForBoard) {
        this.fontForBoard = fontForBoard;
    }
}
