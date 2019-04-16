package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;


@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Integer turn;
    @ElementCollection
    @Column(name = "shots_location_id")
    private List<String> shotsLocations;

    //Constructor----------------------------------------------------------------

    public Salvo() {
    }

    public Salvo(Integer turn, List shotsLocations, GamePlayer gamePlayer) {
        this.turn = turn;
        this.shotsLocations = shotsLocations;
        this.gamePlayer = gamePlayer;
    }

    //Game Player Relation----------------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    //Getters and Setter Game Player----------------------------------------------------------------
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    //Getters----------------------------------------------------------------
    public long getId() {
        return id;
    }

    public Integer getTurn() {
        return turn;
    }

    public List<String> getShotsLocations() {
        return shotsLocations;
    }

    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public String toString() {
        return "Salvo: "+turn+" locs: "+shotsLocations;
    }
}
