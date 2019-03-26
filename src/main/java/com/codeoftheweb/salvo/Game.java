package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;


import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Date gameDate;

    //Connect to GamePlayer----------------------------------------------------------------------------------------------------------------

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<GamePlayer> gameplayers;


    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gameplayers.add(gamePlayer);
    }

    //Connect to Score----------------------------------------------------------------------------------------------------------------
    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public void addScore(Score score) {
        score.setGame(this);
        scores.add(score);
    }

    //Constructors----------------------------------------------------------------------------------------------------------------
    public Game() {
        this.gameDate = new Date();
    }

    //Getters----------------------------------------------------------------------------------------------------------------
    public Date getGameDate() {
        return gameDate;
    }

    public long getId() {
        return id;
    }

    public Set<GamePlayer> getGameplayers() {
        return gameplayers;
    }

    public Set<Score> getScores() {
        return scores;
    }

    //Setters----------------------------------------------------------------------------------------------------------------
    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }


    }


