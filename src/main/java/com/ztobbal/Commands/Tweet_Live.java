package com.ztobbal.Commands;

import io.github.redouane59.twitter.dto.tweet.MediaCategory;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.UploadMediaResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static com.ztobbal.Commands.Constants.TwitterLib.twitterClientR59;

public class Tweet_Live {
    // Parameter a faire sur le Main
    public String hashtag, pathToDirOfBoardLive, pathToTemplateBoard;
    public int idLeague, hour, minute;
    public Color fontForBoard;



    // Init Objet match_live
    public Match_Live match_live = null;

    // HM des buts tweeter
    /*
    * String "HomeT hmTScore - awTScore AwT"
    * String statusId of the tweet of that scores
    * */
    public HashMap<String, String> listButForReplay = new HashMap<>();

    // Conf pour twitter4j
    public ConfigurationBuilder conf = new ConfigurationBuilder();
    TwitterFactory tf;


    public Tweet_Live(String hashtag, String pathToDirOfBoardLive, String pathToTemplateBoard, int idLeague, int hour, int minute, Color fontForBoard) {
        this.hashtag = hashtag;
        this.pathToDirOfBoardLive = pathToDirOfBoardLive;
        this.pathToTemplateBoard = pathToTemplateBoard;
        this.idLeague = idLeague;
        this.hour = hour;
        this.minute = minute;
        this.fontForBoard = fontForBoard;
        Functions.configTwitter4J(conf);
        tf = new TwitterFactory(conf.build());
    }

    public void Tweet(Match_Live match_live_pl) {



        try{

            HashMap<String, String> messagesATweet = match_live_pl.stockMessage();

            // Messages à afficher
            if(messagesATweet != null && messagesATweet.size() > 0){
                System.out.println(new Date() + "-------------------------"+getHashtag()+" messages-------------------------");
                for (Map.Entry<String, String> entry : messagesATweet.entrySet()){
                    Tweet status;

                    if(entry.getValue().equals("")){
                        twitterClientR59.postTweet(entry.getKey());
                    }else{
                        File mediaToUpload = new File(entry.getValue());
                        UploadMediaResponse img = twitterClientR59.uploadMedia(mediaToUpload, MediaCategory.TWEET_IMAGE);
                        status = twitterClientR59.postTweet(entry.getKey(), null, img.getMediaId());

                        if(status != null){
                            // Ajout dans la HM
                            if(entry.getKey().contains("GOAL!")){
                                // Retourne le nom du fichier
                                String mediaName = mediaToUpload.getName().split("\\.")[0];
                                listButForReplay.put(mediaName, status.getId());
                            }

                            // Suppression du fichier
                            if(mediaToUpload.delete()){
                                System.out.println(new Date() +" : Delete of " + mediaToUpload + " has been done successfully");
                            }
                        }
                    }

                }
                System.out.println(new Date() + "--------------------End of messages---------------------------------");
            }
            // Enregistre les replays
            replayGoals();

            // Tweet les replay s'il y en a
            File replayDir = new File(getPathToDirOfBoardLive() + "/Replay");
            if(replayDir.isDirectory()){
                String[] listReplayDir = replayDir.list();

                if (listReplayDir != null && listReplayDir.length > 0) {

                    for(final File fileEntry : Objects.requireNonNull(replayDir.listFiles())){
                        String fileNameToCheck = fileEntry.getName().split("\\.")[0];
                        String formatOfFile = fileEntry.getName().split("\\.")[1];
                        if(listButForReplay.containsKey(fileNameToCheck)){
                            // Utilisation de la librairie Twitter4J pour l'upload des videos
                            Twitter twitter = tf.getInstance();

                            String idToQuote = listButForReplay.get(fileNameToCheck);
                            Tweet twToQuote = twitterClientR59.getTweet(idToQuote);

                            String hmTeam = fileNameToCheck.split("_")[0];
                            String awTeam = fileNameToCheck.split("_")[2];
                            String scoreToScrap = fileNameToCheck.split("_")[1];
                            String hmScore = scoreToScrap.split("-")[0];
                            String awScore = scoreToScrap.split("-")[1];




                            if(twToQuote.getText().contains(hmTeam) && twToQuote.getText().contains(awTeam) &&
                                    twToQuote.getText().contains(hmScore) && twToQuote.getText().contains(awScore) && formatOfFile.equals("mp4")){
                                System.out.println(new Date() + "-------------------------"+getHashtag()+" Trying tweeting replay-------------------------");

                                System.out.println(new Date() + "Trying to quote this tweet >>>> : " + twToQuote.getText());
                                StatusUpdate statusWitVid = new StatusUpdate("🎥 | Replay of that goal https://twitter.com/FSoccerAlert/status/" +idToQuote);
                                UploadedMedia media;

                                if (fileEntry.length() > 1000000) { // Les vidéo de plus d'1MB doivent être "chunked"
                                    media = twitter.uploadMediaChunked(fileEntry.getName(), new BufferedInputStream(new FileInputStream(fileEntry)));
                                } else {
                                    media = twitter.uploadMedia(fileEntry);
                                }

                                if (media != null) {
                                    statusWitVid.setMediaIds(media.getMediaId());
                                }
                                twitter.updateStatus(statusWitVid);

                                // Suppression du fichier
                                if (fileEntry.delete()) {
                                    System.out.println(new Date() + " : Delete of the video " + fileEntry + " successful");

                                    // Suppression de la Map
                                    listButForReplay.keySet().removeIf(but -> but.equals(fileNameToCheck));
                                }
                            }
                            System.out.println(new Date() + "-------------------------"+getHashtag()+" End of tweeting replay-------------------------");
                        }
                    }
                }
            }




            match_live_pl.messagesAndImg.clear();
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }

    }

    //Sauvegarde les replays des buts qu'il a pu récupérer
    public void replayGoals(){
        try{
            if(listButForReplay != null && !listButForReplay.isEmpty()){
                System.out.println(new Date() + "-------------------------"+getHashtag()+" Saving replays-------------------------");
                Document homePage = Jsoup.connect("https://goals.zone").get();
                for (Map.Entry<String, String> entry : listButForReplay.entrySet()){
                    try {
                        if(!entry.getKey().equals("")){
                            System.out.println(new Date() +" : Scrapping to find a Replay for "+entry.getKey()+" ...");

                            String fileName = entry.getKey().replaceAll(" ", "") + ".mp4";

                            String hmTName = entry.getKey().split("_")[0];
                            String awTName = entry.getKey().split("_")[2];

                            Elements searchHMTeam = homePage.select("[href*="+hmTName+"]");
                            Elements searchAwayTeam = homePage.select("[href*="+awTName+"]");



                            Document scrappCurrentFix = null;
                            if(searchHMTeam != null && searchHMTeam.size() > 0){
                                scrappCurrentFix = Jsoup.connect( "https://goals.zone" + searchHMTeam.get(0).attr("href")).ignoreHttpErrors(true).get();
                            }else if(searchAwayTeam != null && searchAwayTeam.size() > 0){
                                scrappCurrentFix = Jsoup.connect( "https://goals.zone"+  searchAwayTeam.get(0).attr("href")).ignoreHttpErrors(true).get();
                            }

                            if(scrappCurrentFix != null) {
                                String scoreToScrap = entry.getKey().split("_")[1];
                                String hmScore = scoreToScrap.split("-")[0];
                                String awScore = scoreToScrap.split("-")[1];


                                Element goalList = scrappCurrentFix.getElementById("goals-list");
                                if (goalList != null) {
                                    System.out.println(new Date() + " : Got the goal-list for " + entry.getKey());
                                    Elements buttonList = goalList.getElementsByTag("button");

                                    if (buttonList != null) {
                                        buttonList.forEach(element -> {
                                            try {
                                                if (element.text().contains(hmScore) && element.text().contains("-") && element.text().contains(awScore)) {
                                                    String idCollapse = element.attr("data-target").substring(1);
                                                    Element collapse = goalList.getElementById(idCollapse);
                                                    Element tagWithLink = collapse.select("a:contains(Original Link)").get(0);
                                                    String linkOfReplay = tagWithLink.attr("href");

                                                    Document pageOfReplay = Jsoup.connect(linkOfReplay).ignoreHttpErrors(true).get();

                                                    if (pageOfReplay != null) {
                                                        Elements replayTag = pageOfReplay.select("[src*=.mp4]");
                                                        String replayPath = null;
                                                        if (replayTag.first() != null) {
                                                            replayPath = replayTag.first().attr("src");
                                                        }

                                                        if (replayPath != null) {
                                                            if (!replayPath.startsWith("https:") && !replayPath.contains("https:")) {
                                                                replayPath = "https:" + replayTag.first().attr("src");
                                                            }


                                                            String commande = "curl " + replayPath + " --output " + fileName;
                                                            ProcessBuilder builder = new ProcessBuilder(commande.split(" "));
                                                            builder.directory(new File(getPathToDirOfBoardLive() + "/Replay/"));
                                                            builder.start();

                                                            File savedFile = new File(getPathToDirOfBoardLive() + "/Replay/" + fileName);

                                                            if(savedFile.exists()){
                                                                System.out.println(new Date() + savedFile.getAbsolutePath() + " has been saved!");
                                                            }

                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                System.out.print(new Date() + " : ");
                                                e.printStackTrace();
                                            }
                                        });
                                    }


                                }
                            }
                        }else{
                            listButForReplay.remove(entry.getKey());
                        }


                    }catch (Exception e){
                        System.out.print(new Date() + " : ");
                        e.printStackTrace();
                    }
                }
                System.out.println(new Date() + "-------------------------"+getHashtag()+" End of saving replays-------------------------");
            }
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }

    }




    // Rendu périodique
    public void Tasking_Tweet() {

        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            public void run() {
                Calendar cal = Calendar.getInstance(); // this is the method you should use, not the Date(), because it
                                                       // is desperated.

                int hour = cal.get(Calendar.HOUR_OF_DAY);// get the hour number of the day, from 0 to 23

                int minute = cal.get(Calendar.MINUTE);

                try {


                    if (hour == getHour() && minute == getMinute()) {
                        listButForReplay.clear();
                        match_live = new Match_Live(getHashtag(),getPathToDirOfBoardLive(),getPathToTemplateBoard(),getIdLeague(),getFontForBoard());
                        match_live.get_fixtures();
                        System.out.println(new Date() +" : #" + getHashtag() +" Fixtures :");
                        match_live.fixtures.forEach(fixture -> System.out.println(fixture.hm_team_name + " - " +fixture.away_team_name));
                        System.out.println("--------------------------");
                        setHour(0);
                        setMinute(50 + getMinute()%10);

                        System.out.println(new Date() +" : hour to get fixture = " + getHour());
                        System.out.println(new Date() +" : minute to get fixture = " + getMinute());
                        System.out.println("-----------------------------------------------------");
                    } else if(match_live != null){
                        Tweet(match_live);
                    }


                } catch (Exception e) {
                    System.out.print(new Date() + " : ");
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(tt, 1000, 1000 * 60);//

    }

    public String getHashtag() {
        return hashtag;
    }

    public String getPathToDirOfBoardLive() {
        return pathToDirOfBoardLive;
    }

    public String getPathToTemplateBoard() {
        return pathToTemplateBoard;
    }

    public int getIdLeague() {
        return idLeague;
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

    public Color getFontForBoard() {
        return fontForBoard;
    }
}
