/* eslint-env browser */
/* eslint "no-console": "off" */
/* global$ */

var app = new Vue({
  el: "#app",
  data: {
    player: null,
    gameList: [],
    playerGames: [],
    otherGames: [],
    userName: "player_one@salvo.com",
    password: "password1",
    new_userName: "",
    new_password: "",
    current_game: [],
    current_players: []
  },
  created: function() {
    this.getData();
    this.getPlayer();
    if ((window.location.contains = "games")) {
      this.playMusic("introSong");
    }
  },
  methods: {
    playMusic(song) {
      console.log(song);
      // var x = document.getElementById(song);
      var media = document.getElementById(song);
      const playPromise = media.play();
      if (playPromise !== null) {
        playPromise.catch(() => {
          media.play();
        });
      }
    },

    muteMusic(song) {
      var media = document.getElementById(song);
      media.pause();
    },
    //------- GET ALL DATA -------------------------------------------------------------------------------------------
    getData() {
      console.log("checking data");
      // fetch games
      fetch("/api/games")
        .then(function(response) {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error("Unable to retrieve data");
          }
        })
        .then(function(jsonData) {
          for (var x = 0; x < jsonData.games.length; x++) {
            let date = new Date(jsonData.games[x].created);
            jsonData.games[x].created = date.toLocaleString();
          }
          app.gameList = jsonData;
          app.getPlayerGames();
        });
    },

    //------- GET LOGGED IN PLAYER GAMES -------------------------------------------------------------------------------------------
    getPlayerGames() {
      for (var x = 0; x < this.gameList.games.length; x++) {
        for (var y = 0; y < this.gameList.games[x].gamePlayers.length; y++) {
          if (
            this.gameList.games[x].gamePlayers[y].Player.Username ==
            this.gameList.player.Username
          ) {
            this.playerGames.push(this.gameList.games[x]);
            // var index = this.gameList.indexOf(this.gameList.games[x]);
            // console.log("index -> " + index);
            // this.openGames.splice(2, 1);
          } else if (
            this.gameList.games[x].gamePlayers[y].Player.Username !=
            this.gameList.player.Username
          ) {
            console.log(
              "2nd if --->> game id --->> " + this.gameList.games[x].id
            );
            this.otherGames.push(this.gameList.games[x]);
          }
        }
      }
    },

    //------- GET LOGGED IN PLAYER -------------------------------------------------------------------------------------------
    getPlayer() {
      // fetch games
      fetch("/api/games")
        .then(function(response) {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error("Unable to retrieve data");
          }
        })
        .then(function(jsonData) {
          app.player = jsonData.player;
        })
        .catch(function(error) {
          console.log("Request failed" + error.message);
        });
    },

    //------- LOGIN -------------------------------------------------------------------------------------------
    login() {
      var ourData = {
        userName: this.userName,
        password: this.password
      };

      fetch("/api/login", {
        credentials: "include",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        method: "POST",
        body: getBody(ourData)
      })
        .then(function(data) {
          console.log("Request success: ", data);
          location.href = "http://localhost:8080/web/games.html";
        })
        .catch(function(error) {
          console.log("Request failure: ", error);
        });

      function getBody(json) {
        var body = [];
        for (var key in json) {
          var encKey = encodeURIComponent(key);
          var encVal = encodeURIComponent(json[key]);
          body.push(encKey + "=" + encVal);
        }
        return body.join("&");
      }
    },

    //------- LOGOUT -------------------------------------------------------------------------------------------
    logout() {
      fetch("/api/logout");
      console.log("logout");
      location.href = "http://localhost:8080/web/login.html";
    },

    //------- SIGNUP -------------------------------------------------------------------------------------------
    signup() {
      var ourData = {
        userName: this.new_userName,
        password: this.new_password
      };

      fetch("/api/players", {
        credentials: "include",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        method: "POST",
        body: getBody(ourData)
      })
        .then(function(data) {
          console.log("Request success: ", data);

          this.userName = this.new_userName;
          this.password = this.new_password;

          app.login();
        })
        .catch(function(error) {
          window.alert("Wait for opponent");
        });

      function getBody(json) {
        var body = [];
        for (var key in json) {
          var encKey = encodeURIComponent(key);
          var encVal = encodeURIComponent(json[key]);
          body.push(encKey + "=" + encVal);
        }
        return body.join("&");
      }
    },

    //------- OPEN GAME VIEW PAGE BASED ON USER -------------------------------------------------------------------------------------------
    openGame(id) {
      console.log("opening game" + id);
      for (var x = 0; x < this.gameList.games.length; x++) {
        if (id == this.gameList.games[x].id) {
          this.current_game = this.gameList.games[x];
          console.log(this.current_game);
        }
      }

      for (var i = 0; i < this.current_game.gamePlayers.length; i++) {
        if (
          this.player.Username ==
          this.current_game.gamePlayers[i].Player.Username
        ) {
          var url =
            "http://localhost:8080/web/game.html?gp=" +
            this.current_game.gamePlayers[i].gpid;
          console.log(url);
          location.href = url;
        }
      }
    },

    //------- JOIN GAME -------------------------------------------------------------------------------------------
    join(id) {
      url = "http://localhost:8080/api/game/" + id + "/players";

      fetch(url)
        .then(function(response) {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error("Unable to retrieve data");
          }
        })
        .then(function(jsonData) {
          console.log(jsonData.gp);
          var url = "http://localhost:8080/web/game.html?gp=" + jsonData.gp;
          console.log(url);
          location.href = url;
        });
    },

    //------- NEW GAME -------------------------------------------------------------------------------------------
    newGame() {
      // var ourData = {
      //   userName: this.new_userName,
      //   password: this.new_password
      // };

      fetch("/api/games", {
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        method: "POST",
        body: ""

        // getBody(ourData)
      })
        .then(function(data) {
          console.log("Request success: ", data);
          if (data.status == 201) {
            app.getData();
          }
        })
        .catch(function(error) {
          console.log("Request failure: ", error);
        });

      function getBody(json) {
        var body = [];
        for (var key in json) {
          var encKey = encodeURIComponent(key);
          var encVal = encodeURIComponent(json[key]);
          body.push(encKey + "=" + encVal);
        }
        return body.join("&");
      }
    },
    //------- CAROUSEL TESTING -------------------------------------------------------------------------------------------

    shiftLeft(e) {
      console.log(e);
      const boxes = document.querySelectorAll(".box");
      const tmpNode = boxes[0];
      boxes[0].className = "box move-out-from-left";

      setTimeout(function() {
        if (boxes.length > 5) {
          tmpNode.classList.add("box--hide");
          boxes[5].className = "box move-to-position5-from-left";
        }
        boxes[1].className = "box move-to-position1-from-left";
        boxes[2].className = "box move-to-position2-from-left";
        boxes[3].className = "box move-to-position3-from-left";
        boxes[4].className = "box move-to-position4-from-left";
        boxes[0].remove();

        document.querySelector(".cards__container").appendChild(tmpNode);
      }, 500);
    },

    shiftRight() {
      const boxes = document.querySelectorAll(".box");
      boxes[4].className = "box move-out-from-right";
      setTimeout(function() {
        const noOfCards = boxes.length;
        if (noOfCards > 4) {
          boxes[4].className = "box box--hide";
        }

        const tmpNode = boxes[noOfCards - 1];
        tmpNode.classList.remove("box--hide");
        boxes[noOfCards - 1].remove();
        let parentObj = document.querySelector(".cards__container");
        parentObj.insertBefore(tmpNode, parentObj.firstChild);
        tmpNode.className = "box move-to-position1-from-right";
        boxes[0].className = "box move-to-position2-from-right";
        boxes[1].className = "box move-to-position3-from-right";
        boxes[2].className = "box move-to-position4-from-right";
        boxes[3].className = "box move-to-position5-from-right";
      }, 500);
    }
  }
});

window.onkeydown = function(e) {
  console.log(e);
  if (e.keyCode == 37) {
    app.shiftLeft();
  } else if (e.keyCode == 39) {
    app.shiftRight();
  }
};
