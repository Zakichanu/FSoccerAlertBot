package com.ztobbal.Commands;

import com.ztobbal.Commands.Constants.EventParam;
import com.ztobbal.Commands.Constants.EventVal;
import com.ztobbal.Commands.Constants.FixtureParam;
import com.ztobbal.Commands.Constants.FixtureVal;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Match_Result {
    public int idLeague;
    public String pathToTemplateBoard, pathToDirOfBoardFinal, pathToBall;
    public Color fontForBoard;
    public HashMap<Integer, Fixture> fixtures =  new HashMap<>();

    public Match_Result(int idLeague, String pathToTemplateBoard, String pathToDirOfBoardFinal, String pathToBall, Color fontForBoard) {
        this.idLeague = idLeague;
        this.pathToTemplateBoard = pathToTemplateBoard;
        this.pathToDirOfBoardFinal = pathToDirOfBoardFinal;
        this.pathToBall = pathToBall;
        this.fontForBoard = fontForBoard;
    }

    // Affichage du tableau d'affichage
    public void Show_Board() {

        try{
            // Récupération de l'id de la BPL
            int id_league =  getIdLeague();
            DateFormat dateFormat = new SimpleDateFormat(Constants.FORMAT_DATE);
            Date date = new Date();


            // Récupération des matchs de la journées
            String url_rf = Constants.URL_LEAGUE + id_league+"/"+dateFormat.format(date);
            String recup_fixtures = Functions.executeRequest("get", url_rf);
            assert recup_fixtures != null;
            JSONObject json_recup_fixtures = new JSONObject(recup_fixtures);
            JSONObject jrf_pageName = json_recup_fixtures.getJSONObject("api");
            JSONArray jrf_arr = jrf_pageName.getJSONArray("fixtures");

            for(int i = 0; i<jrf_arr.length(); i++){
                JSONObject fixture = jrf_arr.getJSONObject(i);
                String fix_status = fixture.getString(FixtureParam.STATUS_MATCH);
                if(fix_status.equals(FixtureVal.FULLTIME) || fix_status.equals(FixtureVal.AFTEXTRATIME) || fix_status.equals(FixtureVal.FT_PEN)){

                    // Récupération des informations d'un match
                    JSONObject home_team = fixture.getJSONObject( FixtureParam.DOMICILE);
                    JSONObject away_team = fixture.getJSONObject( FixtureParam.EXTERIEUR);
                    JSONObject score = fixture.getJSONObject(FixtureParam.SCORE);
                    Fixture actual_fixture = new Fixture(fixture.getInt(FixtureParam.ID),fixture.getInt(FixtureParam.ELAPSED),
                            home_team.getInt(FixtureParam.TEAM_ID),away_team.getInt(FixtureParam.TEAM_ID),fixture.getInt(FixtureParam.GOAL_HMTEAM),fixture.getInt(FixtureParam.GOAL_AWTEAM),
                            fixture.getString(FixtureParam.EVENT_DATE),fixture.getString(FixtureParam.ROUND),fixture.getString(FixtureParam.STADE),
                            home_team.getString(FixtureParam.TEAM_NAME),away_team.getString(FixtureParam.TEAM_NAME),home_team.getString(FixtureParam.TEAM_LOGO),away_team.getString(FixtureParam.TEAM_LOGO),score.getString(FixtureParam.RESULT_FT),fix_status);
                    fixtures.put(i,actual_fixture);
                }

            }

            // Récupération des informations de but et génération du tableau d'affichage
            for(Map.Entry<Integer, Fixture> entry : fixtures.entrySet()){
                int id_fixture = entry.getValue().getFixture_id();
                String url_rg = Constants.URL_EVENT + id_fixture;
                String recup_events = Functions.executeRequest("get", url_rg);
                assert recup_events != null;
                JSONObject json_recup_events = new JSONObject(recup_events);
                JSONObject jre_pageName = json_recup_events.getJSONObject("api");
                JSONArray jre_arr = jre_pageName.getJSONArray("events");
                //Liste des buts dans le match
                ArrayList<Goal> goals = new ArrayList<>();
                ArrayList<Card> cards = new ArrayList<>();



                for(int i=0; i<jre_arr.length(); i++) {
                    JSONObject event = jre_arr.getJSONObject(i);
                    String type = event.getString(EventParam.TYPE);
                    String detail_event = event.getString(EventParam.DETAIL);
                    if(type.equals(EventVal.GOAL)){
                        Goal actual_goal = new Goal(event.getInt(EventParam.ELAPSED),event.getInt(EventParam.TEAM_ID),event.getString(EventParam.TEAM_NAME),event.getString(EventParam.PLAYER),detail_event);
                        goals.add(actual_goal);
                    }else if(type.equals(EventVal.CARD) && detail_event.equals(EventVal.RED_CARD)){
                        Card actual_card = new Card(event.getInt(EventParam.ELAPSED),event.getInt(EventParam.TEAM_ID),event.getString(EventParam.TEAM_NAME),event.getString(EventParam.PLAYER),detail_event);
                        cards.add(actual_card);
                    }
                }

                // Trie des arraylist
                goals.sort(Comparator.comparing(Goal::getTeam_name).thenComparing(Goal::getPlayer));
                cards.sort(Comparator.comparing(Card::getElapsed));

                // Affichage des scores
                try {
                    BufferedImage home_logo = ImageIO.read(new URL(entry.getValue().getHm_logo()));
                    BufferedImage away_logo = ImageIO.read(new URL(entry.getValue().getAway_logo()));
                    BufferedImage final_score = tableau_daffichage(home_logo, away_logo, entry.getValue().getHm_team_name(),entry.getValue().getAway_team_name(),Integer.toString(entry.getValue().getHm_goal()),Integer.toString(entry.getValue().getAway_goal()), goals, cards);
                    File final_scoreFile = new File(getPathToDirOfBoardFinal()+entry.getValue().getHm_team_name()+entry.getValue().getScore()+entry.getValue().getAway_team_name()+"_joined.png");
                    assert final_score != null;
                    boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                    System.out.println(new Date() +" : saved success? " + final_scoreFile.getAbsolutePath());
                    goals.clear();
                    cards.clear();

                } catch (IOException e) {
                    System.out.print(new Date() + " : ");
                    e.printStackTrace();
                }


            }
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }



    }

    // Traitement de l'image de Tableau de bord
    public BufferedImage tableau_daffichage(BufferedImage home_team, BufferedImage away_team, String home_teamName, String away_teamName, String home_score, String away_score, ArrayList<Goal> goals, ArrayList<Card> cards){
        try {
            // Tableau de score vierge
            BufferedImage tableau_score = ImageIO.read(new File(getPathToTemplateBoard()));
            Graphics2D tableau_score_dynamique = tableau_score.createGraphics();

            // Ajout des logos
            tableau_score_dynamique.drawImage(home_team, 300, 370,300,300, null);
            tableau_score_dynamique.drawImage(away_team, 1300, 370,300,300, null);

            // Import de la police de la PL
            Font score_font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(Constants.PATH_FONT_BOARD));
            score_font = score_font.deriveFont(Font.PLAIN, 250);

            // Mise en place de la police de carac
            Font teamName_font = score_font.deriveFont(Font.PLAIN, 50);
            tableau_score_dynamique.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            tableau_score_dynamique.setFont(score_font);

            // Ecriture du score
            tableau_score_dynamique.drawString(home_score, 700, 600);
            tableau_score_dynamique.drawString(away_score, 1050, 600);

            // Ecriture des équipe
            tableau_score_dynamique.setColor(Color.BLACK);
            tableau_score_dynamique.setFont(teamName_font);
            if(home_teamName.length() >= 11 && away_teamName.length() < 11){
                tableau_score_dynamique.drawString(home_teamName, 375,215);
                tableau_score_dynamique.drawString(away_teamName, 1200,215);
            }else if(home_teamName.length() < 11 && away_teamName.length() < 11) {
                tableau_score_dynamique.drawString(home_teamName, 450, 215);
                tableau_score_dynamique.drawString(away_teamName, 1200, 215);
            }else if(home_teamName.length() < 11){
                tableau_score_dynamique.drawString(away_teamName, 1150,215);
                tableau_score_dynamique.drawString(home_teamName, 375,215);
            }else{
                tableau_score_dynamique.drawString(away_teamName, 1150,215);
                tableau_score_dynamique.drawString(home_teamName, 375,215);
            }

            //Ajout des buteurs
            File but = new File(getPathToBall());
            Font scorer_font = score_font.deriveFont(Font.PLAIN, 25);
            tableau_score_dynamique.setFont(scorer_font);
            tableau_score_dynamique.setColor(getFontForBoard());
            StringBuilder hm_scorer = new StringBuilder();
            StringBuilder away_scorer = new StringBuilder();

            // Récupération des buts et stockage dans des chaines de caracteres
            for (int i = 0; i < goals.size(); i++) {
                if(i == 0 && goals.get(i).getTeam_name().equals(home_teamName) ||  i>0 && goals.get(i-1).getTeam_name().equals(away_teamName) && goals.get(i).getTeam_name().equals(home_teamName)){
                    hm_scorer.append(goals.get(i).getPlayer()).append(" ").append(goals.get(i).getElapsed()).append("e");
                }else if(goals.get(i).getTeam_name().equals(home_teamName) && goals.get(i-1).getPlayer().equals(goals.get(i).getPlayer())){
                    hm_scorer.append(", ").append(goals.get(i).getElapsed()).append("e");
                }else if(goals.get(i).getTeam_name().equals(home_teamName)){
                    hm_scorer.append("\n").append(goals.get(i).getPlayer()).append(" ").append(goals.get(i).getElapsed()).append("e");
                }else if(i == 0 && goals.get(i).getTeam_name().equals(away_teamName) || i>0 && goals.get(i-1).getTeam_name().equals(home_teamName) && goals.get(i).getTeam_name().equals(away_teamName)){
                    away_scorer.append(goals.get(i).getPlayer()).append(" ").append(goals.get(i).getElapsed()).append("e");
                }else if(goals.get(i).getTeam_name().equals(away_teamName) && goals.get(i-1).getPlayer().equals(goals.get(i).getPlayer())){
                    away_scorer.append(", ").append(goals.get(i).getElapsed()).append("e");
                }else if(goals.get(i).getTeam_name().equals(away_teamName)){
                    away_scorer.append("\n").append(goals.get(i).getPlayer()).append(" ").append(goals.get(i).getElapsed()).append("e");
                }

                // Condition quand il y a but Contre son camps
                if(goals.get(i).getDetail().equals("Own Goal") && goals.get(i).getTeam_name().equals(home_teamName)){
                    hm_scorer.append(" (CSC)");
                }else if(goals.get(i).getDetail().equals("Own Goal") && goals.get(i).getTeam_name().equals(away_teamName)){
                    away_scorer.append(" (CSC)");
                }

            }

            // Affichage des buteurs
            if(!home_score.equals("0")){
                tableau_score_dynamique.drawImage(ImageIO.read(but),300,700,60,60,null);
                int y = 700;
                for(String line : hm_scorer.toString().split("\n")){
                    tableau_score_dynamique.drawString(line, 400, y += tableau_score_dynamique.getFontMetrics().getHeight());
                }
            }
            if(!away_score.equals("0")){
                tableau_score_dynamique.drawImage(ImageIO.read(but),1600,700,60,60,null);
                int y = 700;
                for(String line : away_scorer.toString().split("\n")){
                    if(line.length() > 20){
                        tableau_score_dynamique.drawString(line, 1225, y += tableau_score_dynamique.getFontMetrics().getHeight());
                    }else{
                        tableau_score_dynamique.drawString(line, 1350, y += tableau_score_dynamique.getFontMetrics().getHeight());
                    }
                }
            }

            // Ajout des cartons rouges
            File carton = new File(Constants.PATH_RED_CARD);
            StringBuilder hm_card = new StringBuilder();
            StringBuilder away_card = new StringBuilder();

            // Récupération des cartons rouges et stockage dans des chaines de caracteres
            for (int i = 0; i < cards.size(); i++) {
                if(i == 0 && cards.get(i).getTeam_name().equals(home_teamName) ||  i>0 && cards.get(i-1).getTeam_name().equals(away_teamName) && cards.get(i).getTeam_name().equals(home_teamName)){
                    hm_card.append(cards.get(i).getPlayer()).append(" ").append(cards.get(i).getElapsed()).append("e");
                }else if(cards.get(i).getTeam_name().equals(home_teamName) && cards.get(i-1).getPlayer().equals(cards.get(i).getPlayer())){
                    hm_card.append(", ").append(cards.get(i).getElapsed()).append("e");
                }else if(cards.get(i).getTeam_name().equals(home_teamName)){
                    hm_card.append("\n").append(cards.get(i).getPlayer()).append(" ").append(cards.get(i).getElapsed()).append("e");
                }else if(i == 0 && cards.get(i).getTeam_name().equals(away_teamName) || i>0 && cards.get(i-1).getTeam_name().equals(home_teamName) && cards.get(i).getTeam_name().equals(away_teamName)){
                    away_card.append(cards.get(i).getPlayer()).append(" ").append(cards.get(i).getElapsed()).append("e");
                }else if(cards.get(i).getTeam_name().equals(away_teamName) && cards.get(i-1).getPlayer().equals(cards.get(i).getPlayer())){
                    away_card.append(", ").append(cards.get(i).getElapsed()).append("e");
                }else if(cards.get(i).getTeam_name().equals(away_teamName)){
                    away_card.append("\n").append(cards.get(i).getPlayer()).append(" ").append(cards.get(i).getElapsed()).append("e");
                }
            }


            // Affichage des cartons rouges
            if(!hm_card.toString().equals("")){
                tableau_score_dynamique.drawImage(ImageIO.read(carton),300,250,60,60,null);
                int y = 250;
                for(String line : hm_card.toString().split("\n")){
                    tableau_score_dynamique.drawString(line, 400, y += tableau_score_dynamique.getFontMetrics().getHeight());
                }
            }
            if(!away_card.toString().equals("")) {
                tableau_score_dynamique.drawImage(ImageIO.read(carton), 1600, 250, 60, 60, null);
                int y = 250;
                for(String line : away_card.toString().split("\n")) {
                    if (line.length() > 20) {
                        tableau_score_dynamique.drawString(line, 1200, y += tableau_score_dynamique.getFontMetrics().getHeight());
                    } else {
                        tableau_score_dynamique.drawString(line, 1350, y += tableau_score_dynamique.getFontMetrics().getHeight());
                    }
                }

            }


            tableau_score_dynamique.dispose();

            return tableau_score;
        } catch (Exception e) {
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }

        return null;

    }

    public int getIdLeague() {
        return idLeague;
    }

    public void setIdLeague(int idLeague) {
        this.idLeague = idLeague;
    }

    public String getPathToTemplateBoard() {
        return pathToTemplateBoard;
    }

    public void setPathToTemplateBoard(String pathToTemplateBoard) {
        this.pathToTemplateBoard = pathToTemplateBoard;
    }

    public String getPathToDirOfBoardFinal() {
        return pathToDirOfBoardFinal;
    }

    public void setPathToDirOfBoardFinal(String pathToDirOfBoardFinal) {
        this.pathToDirOfBoardFinal = pathToDirOfBoardFinal;
    }

    public String getPathToBall() {
        return pathToBall;
    }

    public void setPathToBall(String pathToBall) {
        this.pathToBall = pathToBall;
    }

    public Color getFontForBoard() {
        return fontForBoard;
    }

    public void setFontForBoard(Color fontForBoard) {
        this.fontForBoard = fontForBoard;
    }
}
