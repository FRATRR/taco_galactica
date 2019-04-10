package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Base64;
import java.util.Set;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;
    private String password;
    private Boolean removeHowto;

    //GamePlayer connection----------------------------------------------------------------------------------------------------------------

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<GamePlayer> gameplayers;


    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        gameplayers.add(gamePlayer);
    }
    //Score connection----------------------------------------------------------------------------------------------------------------

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<Score> scores;


    public void addScore(Score score) {
        score.setPlayer(this);
        scores.add(score);
    }

    //Constructors----------------------------------------------------------------------------------------------------------------
    public Player() {
    }

    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.removeHowto = true;
    }

    //Getters----------------------------------------------------------------------------------------------------------------
    public String getUserName() {
        return userName;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }


    public String toString() {

        return "player => " + this.userName;
    }

    public Boolean getRemoveHowto() {
        return removeHowto;
    }

    public Score getScore(Game game){
        return this.scores.stream().filter(score -> score.getGame().equals(game)).findFirst().orElse(null);
    }



    //Setters----------------------------------------------------------------------------------------------------------------
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRemoveHowto(Boolean removeHowto) {
        this.removeHowto = removeHowto;
    }

}
