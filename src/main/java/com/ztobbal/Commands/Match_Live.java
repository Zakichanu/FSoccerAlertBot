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
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Match_Live {
    public String hashtag, pathToDirOfBoardLive, pathToTemplateBoard;
    public int idLeague;
    public Color fontForBoard;

    public Match_Live(String hashtag, String pathToDirOfBoardLive, String pathToTemplateBoard, int idLeague, Color fontForBoard) {
        this.hashtag = hashtag;
        this.pathToDirOfBoardLive = pathToDirOfBoardLive;
        this.pathToTemplateBoard = pathToTemplateBoard;
        this.idLeague = idLeague;
        this.fontForBoard = fontForBoard;
    }

    //Matchs de la journée
    public ArrayList<Fixture> fixtures = new ArrayList<>();

    // Liste des buts dans le match
    public HashMap<Integer, ArrayList<Goal>> goalshMap = new HashMap<>();

    // Liste des chaine a tweet
    public HashMap<String, String> messagesAndImg = new HashMap<>();

    // Stockage des cartons rouges
    public ArrayList<Card> cards = new ArrayList<>();

    // Formatage des différentes dates
    public SimpleDateFormat event_date_api = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public SimpleDateFormat event_date_output = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public void get_fixtures() {
        try{

            // Clear des HM
            fixtures.clear();

            if(goalshMap.size() > 0){
                goalshMap.clear();
            }


            // Récupération de l'id de la BPL
            int id_league = getIdLeague();
            DateFormat dateFormat = new SimpleDateFormat(Constants.FORMAT_DATE);
            Date date = new Date();

            // Récupération des matchs de la journées
            String url_rf = Constants.URL_LEAGUE + id_league + "/" + dateFormat.format(date) + Constants.TIMEZONE;


            //Récupération des fixtures via l'API
            String recup_fixtures = Functions.executeRequest("get", url_rf);
            assert recup_fixtures != null;
            JSONObject json_recup_fixtures = new JSONObject(recup_fixtures);
            JSONObject jrf_pageName = json_recup_fixtures.getJSONObject("api");
            JSONArray jrf_arr = jrf_pageName.getJSONArray("fixtures");

            // Remplissage de mon HM de fixture
            for (int i = 0; i < jrf_arr.length(); i++) {
                JSONObject fixture = jrf_arr.getJSONObject(i);

                // Récupération des informations d'un match
                JSONObject home_team = fixture.getJSONObject(FixtureParam.DOMICILE);
                JSONObject away_team = fixture.getJSONObject(FixtureParam.EXTERIEUR);
                String fix_status = FixtureVal.NOT_STARTED;

                // Parsing datetime of the the game
                Date game_dtime = event_date_api.parse(fixture.getString(FixtureParam.EVENT_DATE));
                String formattedTime = event_date_output.format(game_dtime);


                Fixture actual_fixture = new Fixture(fixture.getInt(FixtureParam.ID), 0,
                        home_team.getInt(FixtureParam.TEAM_ID), away_team.getInt(FixtureParam.TEAM_ID), 0, 0, formattedTime,
                        fixture.getString(FixtureParam.ROUND), fixture.getString(FixtureParam.STADE),
                        home_team.getString(FixtureParam.TEAM_NAME), away_team.getString(FixtureParam.TEAM_NAME),
                        home_team.getString(FixtureParam.TEAM_LOGO), away_team.getString(FixtureParam.TEAM_LOGO), "0-0",
                        fix_status);
                fixtures.add(actual_fixture);

            }
        }catch(Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();

        }

    }

    public void Print_Score() {
        try{
            // Récupération des informations d'un match
            for (Fixture actual_fixture : fixtures) {
                //Message à récupérer pour un tweet
                String message = "";

                // Tableau de score
                File final_scoreFile = null;

                // Création d'une variable retournant le datetime actuel pour marquer le debut
                // d'un match
                Date actual_date = new Date();
                /* String print_actDate = event_date_output.format(actual_date); */
                Date fixtureDate = event_date_output.parse(actual_fixture.getEvent_date());

                // REST Requête pour la gestion des évènements
                String url_ev = Constants.URL_FIXTURE + actual_fixture.getFixture_id() + Constants.TIMEZONE;

                // Vérification du début du match
                if ((actual_date.equals(fixtureDate) || actual_date.after(fixtureDate))
                        && actual_fixture.getStatus().equals(FixtureVal.NOT_STARTED)) {

                    String recup_fixture = Functions.executeRequest("get", url_ev);
                    assert recup_fixture != null;
                    JSONObject json_recup_fixture = new JSONObject(recup_fixture);
                    JSONObject jrf_pageName = json_recup_fixture.getJSONObject("api");
                    JSONArray jrf_arr = jrf_pageName.getJSONArray("fixtures");
                    JSONObject fixture = jrf_arr.getJSONObject(0);
                    if (fixture.getString(FixtureParam.STATUS_MATCH).equals(FixtureVal.FIRST_HALF)) {
                        actual_fixture.setStatus(FixtureVal.FIRST_HALF);
                    }else if(fixture.getString(FixtureParam.STATUS_MATCH).equals(FixtureVal.SECOND_HALF)){
                        actual_fixture.setStatus(FixtureVal.SECOND_HALF);
                    }
                }

                // Début du match
                if (actual_fixture.getElapsed() == 0 && actual_fixture.getStatus().equals(FixtureVal.FIRST_HALF)) {
                    message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                            + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n🔔 Match begins " + actual_fixture.getHm_team_name() + " - "
                            + actual_fixture.getAway_team_name() + " !";
                    try {
                        BufferedImage home_logo = ImageIO.read(new URL(actual_fixture.getHm_logo()));
                        BufferedImage away_logo = ImageIO.read(new URL(actual_fixture.getAway_logo()));
                        BufferedImage final_score = tableau_daffichage(home_logo,
                                away_logo,
                                actual_fixture.hm_team_name,
                                actual_fixture.away_team_name,
                                Integer.toString(actual_fixture.getHm_goal()),
                                Integer.toString(actual_fixture.getAway_goal()));
                        final_scoreFile = new File(getPathToDirOfBoardLive()+actual_fixture.getHm_team_name()+"_"+actual_fixture.getScore()+"_"+actual_fixture.getAway_team_name()+"_"+actual_fixture.getElapsed()+"_joined.png");
                        assert final_score != null;
                        boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                        System.out.println(new Date() +" : saved success? Start of the match : " + actual_fixture.hm_team_name + "-" + actual_fixture.away_team_name + " >>>>"+ final_scoreFile.getAbsolutePath());
                    }catch (Exception e){
                        System.out.print(new Date() + " : ");
                        e.printStackTrace();
                    }

                }

                // Verification de la mi-temps
                if (actual_fixture.getElapsed() >= 40 && !actual_fixture.getStatus().equals(FixtureVal.SECOND_HALF)) {

                    String recup_fixture = Functions.executeRequest("get", url_ev);
                    assert recup_fixture != null;
                    JSONObject json_recup_fixture = new JSONObject(recup_fixture);
                    JSONObject jrf_pageName = json_recup_fixture.getJSONObject("api");
                    JSONArray jrf_arr = jrf_pageName.getJSONArray("fixtures");
                    JSONObject fixture = jrf_arr.getJSONObject(0);

                    if (fixture.getString(FixtureParam.STATUS_MATCH).equals(FixtureVal.HALFTIME)) {
                        if (actual_fixture.getStatus().equals(FixtureVal.FIRST_HALF)) {
                            actual_fixture.setStatus(FixtureVal.HALFTIME);
                            message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                                    + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n🔔 Halftime!\n\nScore : "
                                    + actual_fixture.getHm_team_name() + " " + actual_fixture.getHm_goal() + "-"
                                    + actual_fixture.getAway_goal() + " " + actual_fixture.getAway_team_name() + " !";
                            try {
                                BufferedImage home_logo = ImageIO.read(new URL(actual_fixture.getHm_logo()));
                                BufferedImage away_logo = ImageIO.read(new URL(actual_fixture.getAway_logo()));
                                BufferedImage final_score = tableau_daffichage(home_logo,
                                        away_logo,
                                        actual_fixture.hm_team_name,
                                        actual_fixture.away_team_name,
                                        Integer.toString(actual_fixture.getHm_goal()),
                                        Integer.toString(actual_fixture.getAway_goal()));
                                final_scoreFile = new File(getPathToDirOfBoardLive()+actual_fixture.getHm_team_name()+"_"+actual_fixture.getScore()+"_"+actual_fixture.getAway_team_name()+"_"+actual_fixture.getElapsed()+"_joined.png");
                                assert final_score != null;
                                boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                                System.out.println(new Date() +" : saved success? Halftime : " + actual_fixture.hm_team_name + "-" + actual_fixture.away_team_name + " >>>>"+ final_scoreFile.getAbsolutePath());
                            }catch (Exception e){
                                System.out.print(new Date() + " : ");
                                e.printStackTrace();
                            }

                        }

                    } else if (fixture.getString(FixtureParam.STATUS_MATCH).equals(FixtureVal.SECOND_HALF)) {
                        if (actual_fixture.getStatus().equals(FixtureVal.HALFTIME)) {
                            actual_fixture.setStatus(FixtureVal.SECOND_HALF);
                            message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                                    + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n🔔 Second half starts!\n\nScore : "
                                    + actual_fixture.getHm_team_name() + " " + actual_fixture.getHm_goal() + "-"
                                    + actual_fixture.getAway_goal() + " " + actual_fixture.getAway_team_name() + " !";
                            try {
                                BufferedImage home_logo = ImageIO.read(new URL(actual_fixture.getHm_logo()));
                                BufferedImage away_logo = ImageIO.read(new URL(actual_fixture.getAway_logo()));
                                BufferedImage final_score = tableau_daffichage(home_logo,
                                        away_logo,
                                        actual_fixture.hm_team_name,
                                        actual_fixture.away_team_name,
                                        Integer.toString(actual_fixture.getHm_goal()),
                                        Integer.toString(actual_fixture.getAway_goal()));
                                final_scoreFile = new File(getPathToDirOfBoardLive()+actual_fixture.getHm_team_name()+"_"+actual_fixture.getScore()+"_"+actual_fixture.getAway_team_name()+"_"+actual_fixture.getElapsed()+"_joined.png");
                                assert final_score != null;
                                boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                                System.out.println(new Date() +" : saved success? Second Half : " + actual_fixture.hm_team_name + "-" + actual_fixture.away_team_name + " >>>>"+  final_scoreFile.getAbsolutePath());
                            }catch (Exception e){
                                System.out.print(new Date() + " : ");
                                e.printStackTrace();
                            }
                            actual_fixture.setElapsed(45);
                        }
                    }
                }

                // Fin du match
                if (actual_fixture.getElapsed() >= 80 && !actual_fixture.getStatus().equals(FixtureVal.FULLTIME)) {

                    String recup_fixture = Functions.executeRequest("get", url_ev);
                    assert recup_fixture != null;
                    JSONObject json_recup_fixture = new JSONObject(recup_fixture);
                    JSONObject jrf_pageName = json_recup_fixture.getJSONObject("api");
                    JSONArray jrf_arr = jrf_pageName.getJSONArray("fixtures");
                    JSONObject fixture = jrf_arr.getJSONObject(0);

                    if (fixture.getString(FixtureParam.STATUS_MATCH).equals(FixtureVal.FULLTIME)) {
                        if (actual_fixture.getStatus().equals(FixtureVal.SECOND_HALF)) {
                            message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                                    + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n🔔 End of the match!\n\nScore : " + actual_fixture.getHm_team_name()
                                    + " " + actual_fixture.getHm_goal() + "-" + actual_fixture.getAway_goal() + " "
                                    + actual_fixture.getAway_team_name() + " !";
                            actual_fixture.setStatus(FixtureVal.FULLTIME);
                            try {
                                BufferedImage home_logo = ImageIO.read(new URL(actual_fixture.getHm_logo()));
                                BufferedImage away_logo = ImageIO.read(new URL(actual_fixture.getAway_logo()));
                                BufferedImage final_score = tableau_daffichage(home_logo,
                                        away_logo,
                                        actual_fixture.hm_team_name,
                                        actual_fixture.away_team_name,
                                        Integer.toString(actual_fixture.getHm_goal()),
                                        Integer.toString(actual_fixture.getAway_goal()));
                                final_scoreFile = new File(getPathToDirOfBoardLive()+actual_fixture.getHm_team_name()+"_"+actual_fixture.getScore()+"_"+actual_fixture.getAway_team_name()+"_"+actual_fixture.getElapsed()+"_joined.png");
                                assert final_score != null;
                                boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                                System.out.println(new Date() +" : saved success? Fulltime : " + actual_fixture.hm_team_name + "-" + actual_fixture.away_team_name + " >>>>"+ final_scoreFile.getAbsolutePath());

                            }catch (Exception e){
                                System.out.print(new Date() + " : ");
                                e.printStackTrace();
                            }

                        }

                    }
                }

                // Condition pendant le match
                if (actual_fixture.getStatus().equals(FixtureVal.FIRST_HALF)
                        || actual_fixture.getStatus().equals(FixtureVal.SECOND_HALF)) {
                    int id_fixture = actual_fixture.getFixture_id();

                    // REST Requête pour la gestion des évènements
                    String url_rg = Constants.URL_EVENT + id_fixture;
                    String recup_events = Functions.executeRequest("get", url_rg);
                    assert recup_events != null;
                    JSONObject json_recup_events = new JSONObject(recup_events);
                    JSONObject jre_pageName = json_recup_events.getJSONObject("api");
                    JSONArray jre_arr = jre_pageName.getJSONArray("events");

                    // Liste des buts dans le match
                    ArrayList<Goal> api_goals = new ArrayList<>();
                    ArrayList<Card> api_cards = new ArrayList<>();

                    // Déclaration de l'arraylist qui stockera tout les buts d'un seul match
                    ArrayList<Goal> goalListCurrentGame = new ArrayList<>();

                    // Stockage de tout les buts et cartons rouges renseigner dans l'API
                    for (int i = 0; i < jre_arr.length(); i++) {
                        JSONObject event = jre_arr.getJSONObject(i);
                        String type = event.getString(EventParam.TYPE);
                        String detail_event = event.getString(EventParam.DETAIL);
                        if (type.equals(EventVal.GOAL)) {
                            Goal actual_goal = new Goal(event.getInt(EventParam.ELAPSED), event.getInt(EventParam.TEAM_ID),
                                    event.getString(EventParam.TEAM_NAME), event.getString(EventParam.PLAYER),
                                    detail_event);
                            api_goals.add(actual_goal);
                        } else if (type.equals(EventVal.CARD) && detail_event.equals(EventVal.RED_CARD)) {
                            Card actual_card = new Card(event.getInt(EventParam.ELAPSED), event.getInt(EventParam.TEAM_ID),
                                    event.getString(EventParam.TEAM_NAME), event.getString(EventParam.PLAYER),
                                    detail_event);
                            api_cards.add(actual_card);
                        }
                    }



                    // Actualisation de l'AL volatile des but
                    if (!goalshMap.isEmpty() && goalshMap.containsKey(fixtures.indexOf(actual_fixture))) {
                        goalListCurrentGame = goalshMap.get(fixtures.indexOf(actual_fixture));
                    }


                    // Ajout des but
                    if (api_goals.size() > 0 && api_goals.size() > goalListCurrentGame.size()) {

                        Goal dernier_but = api_goals.get(api_goals.size() - 1);
                        message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                                + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n⚽ GOAL!";

                        message += " FOR " + dernier_but.getTeam_name() + " !";
                        // Vérification si le nom du joueur est répertorié
                        if(!dernier_but.getPlayer().equals("")){
                            message += " SCORER :  " +  dernier_but.getPlayer();
                        }

                        message += " " + dernier_but.getElapsed() + "'\n\n";
                        int goalHM = 0;
                        int goalAW = 0;

                        // MAJ de la value dans la HM
                        goalshMap.put(fixtures.indexOf(actual_fixture), api_goals);

                        // Récupération du nombre de but de part et d'autre
                        for (Goal api_goal : api_goals) {
                            if (api_goal.getTeam_name().equals(actual_fixture.getHm_team_name())) {
                                goalHM++;
                            } else if (api_goal.getTeam_name().equals(actual_fixture.getAway_team_name())) {
                                goalAW++;
                            }
                        }

                        actual_fixture.setAway_goal(goalAW);
                        actual_fixture.setHm_goal(goalHM);
                        actual_fixture.setScore(actual_fixture.getHm_goal() + "-" + actual_fixture.getAway_goal());
                        message += actual_fixture.getHm_team_name() + " " + actual_fixture.getHm_goal() + " - "
                                + actual_fixture.getAway_goal() + " " + actual_fixture.getAway_team_name();
                        try {
                            BufferedImage home_logo = ImageIO.read(new URL(actual_fixture.getHm_logo()));
                            BufferedImage away_logo = ImageIO.read(new URL(actual_fixture.getAway_logo()));
                            BufferedImage final_score = tableau_daffichage(home_logo,
                                    away_logo,
                                    actual_fixture.hm_team_name,
                                    actual_fixture.away_team_name,
                                    Integer.toString(actual_fixture.getHm_goal()),
                                    Integer.toString(actual_fixture.getAway_goal()));
                            final_scoreFile = new File(getPathToDirOfBoardLive()+actual_fixture.getHm_team_name()+"_"+actual_fixture.getScore()+"_"+actual_fixture.getAway_team_name()+"_"+actual_fixture.getElapsed()+"_joined.png");
                            assert final_score != null;
                            boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                            System.out.println(new Date() +" : saved success? Goals for" + dernier_but.getTeam_name() + " >>>>"+ final_scoreFile.getAbsolutePath());
                        }catch (Exception e){
                            System.out.print(new Date() + " : ");
                            e.printStackTrace();
                        }

                        actual_fixture.setElapsed(dernier_but.getElapsed());

                    }

                    // Ajout des cartons rouges
                    if (api_cards.size() > 0 && api_cards.size() > cards.size()) {
                        Card dernier_carton = api_cards.get(api_cards.size() - 1);
                        message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                                + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n🔴 RED CARD! FOR " + dernier_carton.getTeam_name() +" : "+dernier_carton.getPlayer() + " "
                                + dernier_carton.getElapsed() + " '\n\n" + actual_fixture.getHm_team_name() + " "
                                + actual_fixture.getHm_goal() + " - " + actual_fixture.getAway_goal() + " "
                                + actual_fixture.getAway_team_name();
                        actual_fixture.setElapsed(dernier_carton.getElapsed());
                        cards.add(dernier_carton);

                        System.out.println(new Date() +" : saved success? Red Card For : "+ dernier_carton.getTeam_name());
                        actual_fixture.setElapsed(dernier_carton.getElapsed());
                    }

                    // But annulé
                    if (goalListCurrentGame.size() > 0 && api_goals.size() == goalListCurrentGame.size() - 1) {
                        Goal dernier_but = goalListCurrentGame.get(goalListCurrentGame.size() - 1);

                        // Pour pas être bait par l'API mdrr
                        if(actual_fixture.getElapsed() >= dernier_but.getElapsed() && actual_fixture.getElapsed() <= dernier_but.getElapsed()+3){
                            message = "[#"+getHashtag()+"/#"+ actual_fixture.getHm_team_name().substring(actual_fixture.getHm_team_name().lastIndexOf(" ")+1)
                                    + actual_fixture.getAway_team_name().substring(actual_fixture.getAway_team_name().lastIndexOf(" ")+1)+"]\n\n❌ GOAL CANCELED !";

                            message += " FOR " + dernier_but.getTeam_name() + " !";
                            // Vérification si le nom du joueur est répertorié
                            if(!dernier_but.getPlayer().equals("")){
                                message += "  SCORER: " + dernier_but.getPlayer();
                            }

                            message += " " + dernier_but.getElapsed() + "'\n\n";
                            if (dernier_but.getTeam_name().equals(actual_fixture.getAway_team_name())) {
                                actual_fixture.setAway_goal(actual_fixture.getAway_goal() - 1);
                            } else if (dernier_but.getTeam_name().equals(actual_fixture.getHm_team_name())) {
                                actual_fixture.setHm_goal(actual_fixture.getHm_goal() - 1);
                            }

                            actual_fixture.setScore(actual_fixture.getHm_goal() + "-" + actual_fixture.getAway_goal());

                            message += actual_fixture.getHm_team_name() + " " + actual_fixture.getHm_goal() + " - "
                                    + actual_fixture.getAway_goal() + " " + actual_fixture.getAway_team_name();
                            try {
                                BufferedImage home_logo = ImageIO.read(new URL(actual_fixture.getHm_logo()));
                                BufferedImage away_logo = ImageIO.read(new URL(actual_fixture.getAway_logo()));
                                BufferedImage final_score = tableau_daffichage(home_logo,
                                        away_logo,
                                        actual_fixture.hm_team_name,
                                        actual_fixture.away_team_name,
                                        Integer.toString(actual_fixture.getHm_goal()),
                                        Integer.toString(actual_fixture.getAway_goal()));
                                final_scoreFile = new File(getPathToDirOfBoardLive()+actual_fixture.getHm_team_name()+"_"+actual_fixture.getScore()+"_"+actual_fixture.getAway_team_name()+"_"+actual_fixture.getElapsed()+"_joined.png");
                                assert final_score != null;
                                boolean success = ImageIO.write(final_score, "png", final_scoreFile);
                                System.out.println(new Date() +" : saved success? Goal Refused For : "+ dernier_but.getTeam_name() + " >>>" + final_scoreFile.getAbsolutePath());
                            }catch (Exception e){
                                System.out.print(new Date() + " : ");
                                e.printStackTrace();
                            }

                            goalListCurrentGame.remove(dernier_but);
                            // MAJ de la value dans la HM
                            goalshMap.put(fixtures.indexOf(actual_fixture), goalListCurrentGame);

                            actual_fixture.setElapsed(dernier_but.getElapsed());
                        }


                    }


                    // Incrémentation d'une minutes dans le tableau d'affichage (norme pour le
                    // retour des évènement)
                    int actual_minute = actual_fixture.getElapsed() + 1;
                    actual_fixture.setElapsed(actual_minute);

                    System.out.println(new Date() +" : " + actual_fixture.hm_team_name +" - "+ actual_fixture.away_team_name +"  : "+ actual_fixture.getElapsed());

                }

                // Incrémentation dans l'HM pour les tweet et l'image associé
                if(!message.equals("")){
                    messagesAndImg.put(message, ((final_scoreFile != null) ? final_scoreFile.getAbsolutePath() : ""));
                }
            }
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }




    }

    public HashMap<String, String> stockMessage(){
        try{
            Print_Score();
            if(messagesAndImg.size() > 0){
                return messagesAndImg;
            }
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }


        return null;
    }

    // Traitement de l'image de tableau de bord
    public BufferedImage tableau_daffichage(BufferedImage home_team, BufferedImage away_team, String home_teamName, String away_teamName, String home_score, String away_score){
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
            tableau_score_dynamique.setColor(getFontForBoard());
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


            tableau_score_dynamique.dispose();

            return tableau_score;
        }catch (Exception e){
            System.out.print(new Date() + " : ");
            e.printStackTrace();
        }
        return null;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getPathToDirOfBoardLive() {
        return pathToDirOfBoardLive;
    }

    public void setPathToDirOfBoardLive(String pathToDirOfBoardLive) {
        this.pathToDirOfBoardLive = pathToDirOfBoardLive;
    }

    public String getPathToTemplateBoard() {
        return pathToTemplateBoard;
    }

    public void setPathToTemplateBoard(String pathToTemplateBoard) {
        this.pathToTemplateBoard = pathToTemplateBoard;
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
