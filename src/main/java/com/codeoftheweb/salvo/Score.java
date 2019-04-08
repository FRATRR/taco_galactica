package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private double score;
    private Date endDate;

    //Player Relation----------------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    //Game Relation----------------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    //Constructor-----------------------------------------------------------------
    public Score() {
    }

    public Score(double score, Date endDate, Player player, Game game) {
        this.score = score;
        this.endDate = endDate;
        this.player = player;
        this.game = game;
    }

    //Getters---------------------------------------------------------------------

    public long getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }


    //Setters----------------------------------------------------------------

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
