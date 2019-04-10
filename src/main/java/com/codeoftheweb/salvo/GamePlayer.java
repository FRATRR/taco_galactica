package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    //Player connection----------------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    //Game connection----------------------------------------------------------------

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    //Ship connection----------------------------------------------------------------
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();

    //Salvo connection----------------------------------------------------------------
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvos = new HashSet<>();

    //Constructors----------------------------------------------------------------

    public GamePlayer() {
    }

    public GamePlayer(Player player, Game game) {
        this.player = player;
        this.game = game;
    }
    //Getters----------------------------------------------------------------

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    public long getId() {
        return id;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }



    //Setters----------------------------------------------------------------

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }


    public String toString() {
        return "GamePlayer: " + this.getId();
    }
    //Add----------------------------------------------------------------

    public void addShips(Ship ship) {
        ship.setGamePlayer(this);
        ships.add(ship);
    }


}

