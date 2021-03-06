package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity

public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String shipType;
    private boolean sunk;
    @ElementCollection
    @Column(name = "location_id")
    private List<String> locations;

    //Game Player connection----------------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    //Constructor----------------------------------------------------------------
    public Ship() {
    }

    public Ship(String shipType, GamePlayer gamePlayer, List locations) {
        this.shipType = shipType;
        this.gamePlayer = gamePlayer;
        this.locations = locations;
        this.sunk = false;
    }

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

    public String getShipType() {
        return shipType;
    }

    public List<String> getLocations() {
        return locations;
    }

    public boolean isSunk() {
        return sunk;
    }
    //Setters----------------------------------------------------------------


    public void setSunk(boolean sunk) {
        this.sunk = sunk;
    }


}



