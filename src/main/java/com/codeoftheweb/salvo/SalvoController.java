package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {
    //Set Paths and Lists----------------------------------------------------------------------------------------------------------------------
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;


    // Games list + url --------------------------------------------------------------------------------------------------------------------
    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> DTO = new HashMap<>();
        if (authentication != null) {
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());

            if (currentPlayer != null) {


                DTO.put("player", playerInfo(currentPlayer));

            } else {
                DTO.put("player", "Null");
            }
            DTO.put("games", gameRepo.findAll().stream()
                    .map(game -> gameInfo(game))
                    .collect(Collectors.toList()));
            return DTO;

        } else {

            DTO.put("games", gameRepo.findAll().stream()
                    .map(game -> gameInfo(game))
                    .collect(Collectors.toList()));
            return DTO;
        }
    }

    // Players list + url --------------------------------------------------------------------------------------------------------------------
    @RequestMapping("/players")
    public Map<String, Object> getPlayers(Player player) {
        Map<String, Object> info = new HashMap<>();
        info.put("Players", playerRepository.findAll().stream()
                .map(player1 -> playerInfo(player1))
                .collect(Collectors.toList()));
        return info;
    }

    //Scores
    @RequestMapping("/scores")
    public Map<String, Object> allScores(Score score) {
        Map<String, Object> info = new HashMap<>();
        info.put("Scores", scoreRepository.findAll().stream()
                .map(score1 -> scoreInfo(score1))
                .collect(Collectors.toList()));
        return info;

    }


    // Game view   + url --------------------------------------------------------------------------------------------------------------------
    @RequestMapping(path = "/game_view/{gamePlayerId}")
    public Object findGamePlayer(@PathVariable Long gamePlayerId, Authentication authentication) {

        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer currentGp = gamePlayerRepository.getOne(gamePlayerId);


        if (currentGp.getPlayer().getId() != currentPlayer.getId()) {

            return new ResponseEntity<>("Not allowed", HttpStatus.UNAUTHORIZED);

        } else {

            return gameViewInfo(currentGp);

        }
    }


    // Player list for specific game --------------------------------------------------------------------------------------------------------------------
    @RequestMapping("/game/{gameId}/players")
    public Object findPlayers(@PathVariable Long gameId, Authentication authentication) {

        Game currentGame = gameRepo.getOne(gameId);
        if (currentGame.getGameplayers().size() < 2) {
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            GamePlayer newGP = gamePlayerRepository.save(new GamePlayer(currentPlayer, currentGame));

            return new ResponseEntity<>(sendInfo("gp", newGP.getId()), HttpStatus.CREATED);

        } else {

            return new ResponseEntity<>("Game if full", HttpStatus.FORBIDDEN);
        }
    }

    private Map<String, Object> sendInfo(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    // Game state--------------------------------------------------------------------------------------------------------------------
    public String gameState(GamePlayer gamePlayer) {

        Integer playerCounter = 5;
        Integer opponentCounter = 5;

        for (Ship shipPlayer : gamePlayer.getShips()
        ) {
            if (shipPlayer.isSunk()) {

                opponentCounter -= 1;
            }
        }
        for (Ship shipOpponent : getOpponent(gamePlayer).getShips()
        ) {
            if (shipOpponent.isSunk()) {

                playerCounter -= 1;
            }
        }
        System.out.println("Player counter ->" + playerCounter);
        System.out.println("Opponent counter ->" + opponentCounter);
        System.out.println("Player turn ->" + gamePlayer.getSalvos().size());
        System.out.println("Player turn ->" + getOpponent(gamePlayer).getSalvos().size());

        if ((playerCounter == 0) && (opponentCounter == 0) && (gamePlayer.getSalvos().size() == getOpponent(gamePlayer).getSalvos().size())) {

            System.out.println(" TIE --- player -> " + gamePlayer.getPlayer().getScore(gamePlayer.getGame()));
            System.out.println(" TIE --- opponent ->" + getOpponent(gamePlayer).getPlayer().getScore(gamePlayer.getGame()));

            if ((gamePlayer.getPlayer().getScore(gamePlayer.getGame()) == null) && (getOpponent(gamePlayer).getPlayer().getScore(gamePlayer.getGame()) == null)) {

                System.out.println("adding score");
                scoreRepository.save(new Score(.5, new Date(), gamePlayer.getPlayer(), gamePlayer.getGame()));
                scoreRepository.save(new Score(.5, new Date(), getOpponent(gamePlayer).getPlayer(), gamePlayer.getGame()));
            }
            return ("tie");


        } else if (((playerCounter == 0) && (opponentCounter != 0) && (gamePlayer.getSalvos().size() == getOpponent(gamePlayer).getSalvos().size()))) {

            System.out.println("WIN");
            if ((gamePlayer.getPlayer().getScore(gamePlayer.getGame()) == null) && (getOpponent(gamePlayer).getPlayer().getScore(gamePlayer.getGame()) == null)) {

                System.out.println("adding score");
                scoreRepository.save(new Score(1.0, new Date(), gamePlayer.getPlayer(), gamePlayer.getGame()));
                scoreRepository.save(new Score(0.0, new Date(), getOpponent(gamePlayer).getPlayer(), gamePlayer.getGame()));
            }

            return ("win");


        } else if ((playerCounter != 0) && (opponentCounter == 0) && (gamePlayer.getSalvos().size() == getOpponent(gamePlayer).getSalvos().size())) {

            System.out.println("LOST");
            if ((gamePlayer.getPlayer().getScore(gamePlayer.getGame()) == null) && (getOpponent(gamePlayer).getPlayer().getScore(gamePlayer.getGame()) == null)) {

                System.out.println("adding score");
                scoreRepository.save(new Score(0.0, new Date(), gamePlayer.getPlayer(), gamePlayer.getGame()));
                scoreRepository.save(new Score(1.0, new Date(), getOpponent(gamePlayer).getPlayer(), gamePlayer.getGame()));
            }
            return ("lost");

        } else {
            return "playing";
        }

    }


    //Remove how to modal----------------------------------------------------------------------------------------------------------------------------------------
    @RequestMapping(path = "/howto", method = RequestMethod.POST)
    public ResponseEntity<Object> removeModal(Authentication authentication) {
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());

        if (currentPlayer.getUserName() != null) {


            currentPlayer.setRemoveHowto(false);

            playerRepository.save(currentPlayer);

            return new ResponseEntity<>("Modal removed", HttpStatus.ACCEPTED);

        } else {
            return new ResponseEntity<>("Log in", HttpStatus.UNAUTHORIZED);

        }


    }
//add new players----------------------------------------------------------------------------------------------------------------------------------------

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String userName, @RequestParam String password) {

        if (userName.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        } else if (playerRepository.findByUserName(userName) != null) {
            return new ResponseEntity<>("Username already taken", HttpStatus.FORBIDDEN);
        } else {

            playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }


    }

    //add new games------------------------------------------------------------------------------------------------------------------------------------
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> createGame(Authentication authentication) {

        Player currentPlayer = playerRepository.findByUserName(authentication.getName());

        if (currentPlayer != null) {

            Game newGame = gameRepo.save(new Game());
            GamePlayer newGP = gamePlayerRepository.save(new GamePlayer(currentPlayer, newGame));
            return new ResponseEntity<>("New game created", HttpStatus.CREATED);

        } else {
            return new ResponseEntity<>("Log in or signup to create game", HttpStatus.FORBIDDEN);
        }

    }

    // add ships -----------------------------------------------------------------------------------------------------------------------------------------
    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> addShips(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody Set<Ship> ships) {

        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer currentGP = gamePlayerRepository.getOne(gamePlayerId);

        if (currentPlayer.getId() == currentGP.getPlayer().getId() && currentGP.getShips().size() < 5) {
            for (Ship ship : ships) {
                ship.setGamePlayer(currentGP);
                shipRepository.save(ship);
            }

            return new ResponseEntity<>("Ships added", HttpStatus.CREATED);

        } else if (currentGP.getShips().size() > 4) {
            return new ResponseEntity<>("You already placed five ships", HttpStatus.FORBIDDEN);

        } else if (currentGP.getShips().size() > 4 && getOpponent(currentGP).getPlayer().getUserName() == null) {
            return new ResponseEntity<>("Your ships have been placed wait for opponent to join the game", HttpStatus.FORBIDDEN);

        } else if (currentPlayer.getId() != currentGP.getPlayer().getId()) {
            return new ResponseEntity<>("Log in or signup ", HttpStatus.UNAUTHORIZED);

        } else {
            return null;
        }
    }

    // add salvoes ------------------------------------------------------------------------------------------------------------------------------
    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes(@PathVariable Long gamePlayerId, Authentication authentication, GamePlayer gamePlayer, @RequestBody Salvo salvo) {
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        GamePlayer currentGP = gamePlayerRepository.getOne(gamePlayerId);
        Integer turn = currentGP.getSalvos().size() + 1;

        if (getOpponent(currentGP) != null) {
            if (currentPlayer.getId() == currentGP.getPlayer().getId() && getOpponent(currentGP).getShips().size() == 5 && currentGP.getSalvos().size() <= getOpponent(currentGP).getSalvos().size()) {
                salvo.setGamePlayer(currentGP);
                salvo.setTurn(turn);
                salvoRepository.save(salvo);


                return new ResponseEntity<>("Salvoes added", HttpStatus.CREATED);

            } else if (currentPlayer.getId() != currentGP.getPlayer().getId()) {
                return new ResponseEntity<>("Log in or signup", HttpStatus.UNAUTHORIZED);

            } else if (getOpponent(currentGP).getShips().size() < 5) {
                return new ResponseEntity<>("Wait for opponent to place all the ships", HttpStatus.FORBIDDEN);
            } else {
                return null;
            }
        } else if (getOpponent(currentGP) == null) {
            return new ResponseEntity<>("No opponent", HttpStatus.FORBIDDEN);
        } else {
            return null;
        }

    }

    //set up for all info --------------------------------------------------------------------------------------------------------------------------------


    public LinkedHashMap<String, Object> gameInfo(Game game) {
        LinkedHashMap<String, Object> info = new LinkedHashMap<>();

        info.put("id", game.getId());
        info.put("created", game.getGameDate());
        info.put("gamePlayers", game.getGameplayers().stream()
                .map(gamePlayer -> gamePlayerInfo(gamePlayer))
                .collect(Collectors.toList()));
        return info;
    }

    //Game view info -> game + my ships + opponent salvoes------------------------------------------------------------------------------------------------
    public Map<String, Object> gameViewInfo(GamePlayer gamePlayer) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", gamePlayer.getGame().getId());
        info.put("created", gamePlayer.getGame().getGameDate());
        info.put("gamePlayers", gamePlayer.getGame().getGameplayers().stream()
                .map(gp -> gamePlayerInfo(gp))
                .collect(Collectors.toList()));
        info.put("ships", gamePlayer.getShips().stream()
                .map(ship -> shipInfo(ship))
                .collect(Collectors.toList()));

        info.put("gameState", gameState(gamePlayer));
        info.put("Score", scoreInfo(gamePlayer.getPlayer().getScore(gamePlayer.getGame())));
        return info;
    }

    public LinkedHashMap<String, Object> gamePlayerInfo(GamePlayer gamePlayer) {
        LinkedHashMap<String, Object> info = new LinkedHashMap<>();

        info.put("gpid", gamePlayer.getId());
        info.put("Player", playerInfo(gamePlayer.getPlayer()));
        info.put("salvos", salvoInfo(gamePlayer));
        return info;
    }

    public Map<String, Object> playerInfo(Player player) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("Id", player.getId());
        info.put("Username", player.getUserName());
        info.put("Howto", player.getRemoveHowto());
        return info;
    }


    public Map<String, Object> shipInfo(Ship ship) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("Type", ship.getShipType());
        info.put("Locations", ship.getLocations());
        info.put("Sunk", ship.isSunk());
        return info;
    }

    //---------------------             SALVO INFO                           ---------------------------------------------------

    public List<Object> salvoInfo(GamePlayer gamePlayer) {

        if (getOpponent(gamePlayer) != null) {
            //new list to return
            List<Object> salvoesInfoList = new ArrayList<>();

            //opponent ships list
            ArrayList<Ship> opponentShipsList = new ArrayList<Ship>(getOpponent(gamePlayer).getShips());

            //all salvoes list sorted by turn
            List<Salvo> allSalvoesList = new ArrayList<>(gamePlayer.getSalvos());

            allSalvoesList.sort(Comparator.comparingInt(Salvo::getTurn));

            //accumulated salvoes list
            ArrayList<String> accSalvoes = new ArrayList<>();

            //looping  trough all sorted salvoes

            //1st loop SALVO
            for (Salvo salvo : allSalvoesList) {
                Map<String, Object> salvoInfo = new LinkedHashMap<>();
                List<Object> fleetInfo = new ArrayList<>();
                accSalvoes.addAll(salvo.getShotsLocations());
                //2nd loop SHIP
                for (Ship ship : opponentShipsList) {
                    Map<String, Object> shipStatus = new LinkedHashMap<>();
                    ArrayList<String> turnHits = new ArrayList<>();
                    ArrayList<String> accHits = new ArrayList<>();

                    // 3rd loop SHIP LOCATIONS
                    for (String shipLoc : ship.getLocations()) {

                        // 4th loop SALVO LOCATIONS
                        for (String salvoLoc : salvo.getShotsLocations()) {

                            if (shipLoc == salvoLoc) {

                                turnHits.add(shipLoc);
                            }
                        }
                        //4th loop ACC SALVO LOCATIONS
                        for (String salvoLoc2 : accSalvoes) {

                            if (shipLoc == salvoLoc2) {

                                accHits.add(shipLoc);
                            }
                        }
                    }
                    if (ship.getLocations().size() <= accHits.size()) {
                        ship.setSunk(true);
                    }
                    //adding info to ship obj


                    shipStatus.put("type", ship.getShipType());
                    shipStatus.put("Sunk", ship.getLocations().size() <= accHits.size());

                    //add ship obj to list
                    fleetInfo.add(shipStatus);

                    //checking game status

                }
                salvoInfo.put("turn", salvo.getTurn());
                salvoInfo.put("fleet", fleetInfo);

                ArrayList<Object> hitList = new ArrayList<>(salvo.getShotsLocations());
                ArrayList<Object> allOpponentShipLoc = new ArrayList<>(getOpponent(salvo.getGamePlayer()).getShips().stream()
                        .map(ship -> ship.getLocations()).flatMap(List::stream)
                        .collect(Collectors.toList()));
                hitList.retainAll(allOpponentShipLoc);
                salvoInfo.put("shots", accSalvoes);
                salvoInfo.put("hits", hitList);
                salvoesInfoList.add(salvoInfo);


            }
            return salvoesInfoList;

        } else {

            return null;
        }
    }


    public GamePlayer getOpponent(GamePlayer gamePlayer) {


        return gamePlayer.getGame().getGameplayers().stream().filter(gp -> gp.getId() != gamePlayer.getId()).findFirst().orElse(null);
    }


    public Map<String, Object> scoreInfo(Score score) {
        Map<String, Object> info = new LinkedHashMap<>();
        if (score != null) {
            info.put("Game", score.getGame().getId());
            info.put("Player", playerInfo(score.getPlayer()));
            info.put("Score", score.getScore());
            info.put("End", score.getEndDate());
            return info;
        } else {
            info.put("Player", null);
            info.put("Score", null);
            info.put("End", null);

            return info;
        }
    }

    public Map<String, Object> gamePlayersOnlyInfo(GamePlayer gamePlayer) {

        Map<String, Object> info = new LinkedHashMap<>();

        info.put("Player", playerInfo(gamePlayer.getPlayer()));
        return info;
    }

    public Map<String, Object> playersInfo(Game game) {

        Map<String, Object> info = new LinkedHashMap<>();

        info.put("Players", game.getGameplayers().stream()
                .map(gamePlayer -> gamePlayersOnlyInfo(gamePlayer))
                .collect(Collectors.toList()));
        return info;
    }


}


