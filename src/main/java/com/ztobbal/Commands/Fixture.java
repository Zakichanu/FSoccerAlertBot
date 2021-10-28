package com.ztobbal.Commands;

public class Fixture {
    public int fixture_id, elapsed, hm_team_id, away_team_id, hm_goal, away_goal;
    public String event_date, round, venue, hm_team_name, away_team_name, hm_logo, away_logo, score, status;

    public Fixture(int fixture_id, int elapsed, int hm_team_id, int away_team_id, int hm_goal, int away_goal, String event_date,
                   String round, String venue, String hm_team_name, String away_team_name, String hm_logo,
                   String away_logo, String score, String status) {
        this.fixture_id = fixture_id;
        this.elapsed = elapsed;
        this.hm_team_id = hm_team_id;
        this.away_team_id = away_team_id;
        this.hm_goal = hm_goal;
        this.away_goal = away_goal;
        this.event_date = event_date;
        this.round = round;
        this.venue = venue;
        this.hm_team_name = hm_team_name;
        this.away_team_name = away_team_name;
        this.hm_logo = hm_logo;
        this.away_logo = away_logo;
        this.score = score;
        this.status = status;
    }

    public int getFixture_id() {
        return fixture_id;
    }

    public void setFixture_id(int fixture_id) {
        this.fixture_id = fixture_id;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public int getHm_team_id() {
        return hm_team_id;
    }

    public void setHm_team_id(int hm_team_id) {
        this.hm_team_id = hm_team_id;
    }

    public int getAway_team_id() {
        return away_team_id;
    }

    public void setAway_team_id(int away_team_id) {
        this.away_team_id = away_team_id;
    }

    public int getHm_goal() {
        return hm_goal;
    }

    public void setHm_goal(int hm_goal) {
        this.hm_goal = hm_goal;
    }

    public int getAway_goal() {
        return away_goal;
    }

    public void setAway_goal(int away_goal) {
        this.away_goal = away_goal;
    }

    public String getEvent_date() {
        return event_date;
    }

    public void setEvent_date(String event_date) {
        this.event_date = event_date;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getHm_team_name() {
        return hm_team_name;
    }

    public void setHm_team_name(String hm_team_name) {
        this.hm_team_name = hm_team_name;
    }

    public String getAway_team_name() {
        return away_team_name;
    }

    public void setAway_team_name(String away_team_name) {
        this.away_team_name = away_team_name;
    }

    public String getHm_logo() {
        return hm_logo;
    }

    public void setHm_logo(String hm_logo) {
        this.hm_logo = hm_logo;
    }

    public String getAway_logo() {
        return away_logo;
    }

    public void setAway_logo(String away_logo) {
        this.away_logo = away_logo;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Fixture{" +
                "fixture_id=" + fixture_id +
                ", elapsed=" + elapsed +
                ", hm_team_id=" + hm_team_id +
                ", away_team_id=" + away_team_id +
                ", hm_goal=" + hm_goal +
                ", away_goal=" + away_goal +
                ", event_date='" + event_date + '\'' +
                ", round='" + round + '\'' +
                ", venue='" + venue + '\'' +
                ", hm_team_name='" + hm_team_name + '\'' +
                ", away_team_name='" + away_team_name + '\'' +
                ", hm_logo='" + hm_logo + '\'' +
                ", away_logo='" + away_logo + '\'' +
                ", score='" + score + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
