import com.ztobbal.Commands.Constants;

import com.ztobbal.Commands.Tweet_Live;
import com.ztobbal.Commands.Tweet_Result_Final;


import java.awt.*;
import java.io.*;
import java.util.Calendar;

import static com.ztobbal.Commands.Constants.TwitterLib.twitterClientR59;

public class MAIN {
    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance(); // this is the method you should use, not the Date(), because it
        // is desperated.

        int hour = cal.get(Calendar.HOUR_OF_DAY);// get the hour number of the day, from 0 to 23

        int minute = cal.get(Calendar.MINUTE);

        try {
            //////////////////// Premier League /////////////////////////////////////////
            // Tweet des résultats finaux de la journée
            Tweet_Result_Final finalPL = new Tweet_Result_Final(Constants.PremierLeague.PATH_GENBOARD_FINAL, "PremierLeague",Constants.PremierLeague.TEMPLATE_BOARD,
                    Constants.PATH_BLACK_BALL,23, 30, Constants.PremierLeague.ID, Color.BLACK);
            finalPL.Tasking_Tweet();

            // Match en temps réel
            Tweet_Live livePL = new Tweet_Live("PremierLeague", Constants.PremierLeague.PATH_GENBOARD_LIVE, Constants.PremierLeague.TEMPLATE_BOARD, Constants.PremierLeague.ID,
                    hour, minute, Color.BLACK);
            livePL.Tasking_Tweet();

            //////////////////// Ligue 1 /////////////////////////////////////////
            // Tweet des résultats finaux de la journée
            Tweet_Result_Final finalL1 = new Tweet_Result_Final(Constants.Ligue1.PATH_GENBOARD_FINAL, "Ligue1",Constants.Ligue1.TEMPLATE_BOARD,
                    Constants.PATH_BLACK_BALL,23, 32, Constants.Ligue1.ID, Color.BLACK);
            finalL1.Tasking_Tweet();

            // Match en temps réel
            Tweet_Live liveL1 = new Tweet_Live("Ligue1", Constants.Ligue1.PATH_GENBOARD_LIVE, Constants.Ligue1.TEMPLATE_BOARD, Constants.Ligue1.ID,
                    hour, minute+1, Color.BLACK);
            liveL1.Tasking_Tweet();

            //////////////////// Bundesliga /////////////////////////////////////////
            // Tweet des résultats finaux de la journée
            Tweet_Result_Final finalBundes = new Tweet_Result_Final(Constants.Bundesliga.PATH_GENBOARD_FINAL, "Bundesliga",Constants.Bundesliga.TEMPLATE_BOARD,
                    Constants.PATH_BLACK_BALL,23, 35, Constants.Bundesliga.ID, Color.BLACK);
            finalBundes.Tasking_Tweet();

            // Match en temps réel
            Tweet_Live liveBundes = new Tweet_Live("Bundesliga", Constants.Bundesliga.PATH_GENBOARD_LIVE, Constants.Bundesliga.TEMPLATE_BOARD, Constants.Bundesliga.ID,
                    hour, minute+2, Color.BLACK);
            liveBundes.Tasking_Tweet();

            //////////////////// Liga /////////////////////////////////////////
            // Tweet des résultats finaux de la journée
            Tweet_Result_Final finalLiga = new Tweet_Result_Final(Constants.Liga.PATH_GENBOARD_FINAL, "Liga",Constants.Liga.TEMPLATE_BOARD,
                    Constants.PATH_WHITE_BALL,23, 58, Constants.Liga.ID, Color.WHITE);
            finalLiga.Tasking_Tweet();

            // Match en temps réel
            Tweet_Live liveLiga = new Tweet_Live("Liga", Constants.Liga.PATH_GENBOARD_LIVE, Constants.Liga.TEMPLATE_BOARD, Constants.Liga.ID,
                    hour, minute+3, Color.BLACK);
            liveLiga.Tasking_Tweet();

            //////////////////// SerieA /////////////////////////////////////////
            // Tweet des résultats finaux de la journée
            Tweet_Result_Final finalSerieA = new Tweet_Result_Final(Constants.SerieA.PATH_GENBOARD_FINAL, "SerieA",Constants.SerieA.TEMPLATE_BOARD,
                    Constants.PATH_WHITE_BALL,23, 40, Constants.SerieA.ID, Color.WHITE);
            finalSerieA.Tasking_Tweet();

            // Match en temps réel
            Tweet_Live liveSerieA = new Tweet_Live("SerieA", Constants.SerieA.PATH_GENBOARD_LIVE, Constants.SerieA.TEMPLATE_BOARD, Constants.SerieA.ID,
                    hour, minute+4, Color.BLACK);
            liveSerieA.Tasking_Tweet();
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            twitterClientR59.postDm(e.getMessage() + "\n" + sw, twitterClientR59.getUserFromUserName("HakuDZed").getId());


        }
        

    }
}
