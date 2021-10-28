package com.ztobbal.Commands;

public class Goal {
    public int elapsed, team_id;
    public String team_name, player, detail;

    public Goal(int elapsed, int team_id, String team_name, String player, String detail) {
        this.elapsed = elapsed;
        this.team_id = team_id;
        this.team_name = team_name;
        this.player = player;
        this.detail = detail;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public int getTeam_id() {
        return team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "com.ztobbal.Commands.Goal{" +
                "elapsed=" + elapsed +
                ", team_id=" + team_id +
                ", team_name='" + team_name + '\'' +
                ", player='" + player + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
