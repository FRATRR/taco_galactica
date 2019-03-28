/* eslint-env browser */
/* eslint "no-console": "off" */
/* global$ */

var app = new Vue({
  el: "#app",
  data: {
    test: [],
    selectedSalvoes: [],
    game: [],
    ships: [],
    salvoes: [],
    playerSalvoes: [],
    hits: [],
    shots: [],
    fleet: [],
    x_axis: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
    y_axis: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
    player: null,
    myParam: null,
    rotatingShip: null,
    ship: null,
    length: null,
    turn: null,
    horizontal: true,
    hide: true,
    vertical: false,
    placeShips: false,
    shooting: false,
    backgroundAudio: "song",
    span: document.getElementsByClassName("close")[0],
    modal: document.getElementById("myModal")
  },
  created: function() {
    this.getPlayer();
    this.getData();
    this.playMusic("song");

    // this.statusCheck();
  },

  watch: {
    player: function() {
      if (this.player.Howto == true) {
        console.log("yes modal");
        this.openModal();
      } else {
        console.log("no modal");
      }
    }
  },

  methods: {
    // URL based on game player
    getURL() {
      const urlParams = new URLSearchParams(window.location.search);
      this.myParam = urlParams.get("gp");
      return this.myParam;
    },
    playMusic(song) {
      // var x = document.getElementById(song);
      if (song == "pause") {
        var x = document.getElementById("song");
        x.pause();
      } else {
        var media = document.getElementById(song);
        const playPromise = media.play();
        if (playPromise !== null) {
          playPromise.catch(() => {
            media.play();
          });
        }
      }
    },

    //FETCHING DATA --------------------------------------------------------------------------------

    // all data
    getData() {
      setInterval(function() {
        fetch("http://localhost:8080/api/game_view/" + app.getURL())
          .then(function(response) {
            if (response.ok) {
              return response.json();
            } else {
              throw new Error("Unable to retrieve data");
            }
          })
          .then(function(jsonData) {
            app.game = jsonData;
            if (app.game.ships.length == 5) {
              app.placeShips = false;
            } else {
              app.placeShips = true;
            }
            app.checkShip();
            app.checkSalvoes();
          });
      }, 1000);
    },
    //current logged in player
    getPlayer() {
      fetch("http://localhost:8080/api/games")
        .then(function(response) {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error("Unable to retrieve data");
          }
        })
        .then(function(jsonData) {
          app.player = jsonData.player;
        });
    },
    //CHECKING GAME STATUS --------------------------------------------------------------------------------

    // statusCheck() {
    //   if ((this.ships.length = 5)) {
    //     this.placeShips = false;
    //   }
    // },

    //POST DATA --------------------------------------------------------------------------------
    postShips() {
      var shipType = this.ship;
      var locations = this.test;
      //var locations = new Array("E7", "E8", "E9");
      fetch("/api/games/players/" + this.myParam + "/ships", {
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        method: "POST",
        body: JSON.stringify([{ shipType: shipType, locations: locations }])
      })
        .then(function(data) {
          console.log("Request success: ", data);
          app.test = [];
        })
        .catch(function(error) {
          console.log("Request failure: ", error);
        });
      app.checkShip();
    },

    // salvoes
    postSalvoes() {
      this.turn += 1;
      var turn = +this.turn;
      var shotsLocations = this.selectedSalvoes;
      if (shotsLocations.length == 5) {
        fetch("/api/games/players/" + this.myParam + "/salvoes", {
          credentials: "include",
          headers: {
            "Content-Type": "application/json"
          },
          method: "POST",
          body: JSON.stringify({ shotsLocations: shotsLocations })
        })
          .then(function(data) {
            console.log("Request success: ", data.status);
            if (data.status == 403) {
              alert(
                "You cannot shoot yet. Wait for opponent to place the ships"
              );
              for (var i = 0; i < app.selectedSalvoes.length; i++) {
                console.log("removing -> " + app.selectedSalvoes[i] + "s");
                document
                  .getElementById(app.selectedSalvoes[i] + "s")
                  .classList.remove("salvo");
              }
            } else if (data.status == 200) {
              alert("You cannot shoot yet. Opponent still shooting.");
              for (var i = 0; i < app.selectedSalvoes.length; i++) {
                console.log("removing -> " + app.selectedSalvoes[i] + "s");
                document
                  .getElementById(app.selectedSalvoes[i] + "s")
                  .classList.remove("salvo");
              }
            }
            app.selectedSalvoes = [];
          })
          .catch(function(error) {
            console.log("Request failure: ", error);
          });
      } else {
        console.log("add more shots");
      }
    },
    //------- SALVOES-------------------------------------------------------------------------------------------

    addSalvoes(e) {
      var cell = e.target.id;
      var row = e.target.id.charAt(0);
      var column = e.target.id.charAt(1);

      var cellID = cell.substring(0, cell.length - 1);

      //adding salvo class
      if (this.selectedSalvoes.length < 5) {
        this.selectedSalvoes.push(cellID);
        document.getElementById(cell).classList.add("salvo");
      } else if ((this.selectedSalvoes.length = 5)) {
        alert("no more shots. shoot!");
      }
    },

    checkSalvoes() {
      console.log("checking salvoes");

      for (let gp of this.game.gamePlayers) {
        if (this.player.Username == gp.Player.Username) {
          this.shots = gp.salvos;
        }
      }

      for (var i = 0; i < this.shots.length; i++) {
        for (var s = 0; s < this.shots[i].shots.length; s++) {
          var cell = this.shots[i].shots[s] + "s";
          document.getElementById(cell).classList.add("salvo");
        }
      }
    },

    //------- HITS-------------------------------------------------------------------------------------------

    checkHits() {
      for (var i = 0; i < this.game.gamePlayers.length; i++) {
        if (this.player.Username == this.game.gamePlayers[i].Player.Username) {
          this.playerSalvoes = this.game.gamePlayers[i].salvos;
        }

        for (var x = 0; x < this.playerSalvoes.length; x++) {
          for (var y = 0; y < this.playerSalvoes[x].hits.length; y++) {
            var cell = this.playerSalvoes[x].hits[y] + "s";
            document.getElementById(cell).classList.add("hit");
          }
        }
      }
    },
    //SHIPS -> checking hits --------------------------------------------------------------------------------------------------------
    checkShip() {
      this.checkHits();

      // setting correct ships to logged in player
      for (var i = 0; i < this.game.gamePlayers.length; i++) {
        if (this.player.Username != this.game.gamePlayers[i].Player.Username) {
          this.salvoes = this.game.gamePlayers[i].salvos;
        }
        if (this.player.Username == this.game.gamePlayers[i].Player.Username) {
          this.playerSalvoes = this.game.gamePlayers[i].salvos[0].shots;
        }
      }

      //TABLE 1  player grid----------------------------------------------------------------------------------------------------

      // adding player ships
      this.ships = this.game.ships;
      for (var d = 0; d < this.ships.length; d++) {
        for (var g = 0; g < this.ships[d].Locations.length; g++) {
          document
            .getElementById(this.ships[d].Locations[g])
            .classList.add("drop");
        }
      }
      // adding hits and salvoes
      for (var x = 0; x < this.salvoes.length; x++) {
        for (var e = 0; e < this.salvoes[x].shots.length; e++) {
          for (var y = 0; y < this.ships.length; y++) {
            for (var i = 0; i < this.ships[y].Locations.length; i++) {
              if (this.ships[y].Locations[i] == this.salvoes[x].shots[e]) {
                document
                  .getElementById(this.ships[y].Locations[i])
                  .classList.remove("drop");

                document
                  .getElementById(this.ships[y].Locations[i])
                  .classList.add("hit");

                this.hits.push(this.ships[y].Locations[i]);
              } else {
                document
                  .getElementById(this.salvoes[x].shots[e])
                  .classList.add("salvo");
              }
            }
          }
        }
      }
    },

    //------- DRAG AND DROP + ROTATE-------------------------------------------------------------------------------------------
    // rotate
    rotation(ship) {
      this.vertical = !this.vertical;
      this.horizontal = !this.horizontal;
      if (this.horizontal) {
        document.getElementById(ship).classList.add("horizontal");
        document.getElementById(ship).classList.remove("vertical");
      } else {
        document.getElementById(ship).classList.add("vertical");
        document.getElementById(ship).classList.remove("horizontal");
      }
    },

    //set var ship to selected ship
    dragStart(e) {
      this.ship = e.path[0].id;
    },

    //turn cell green when dragging over them + sound
    dragOver(e) {
      this.playMusic("beep");
      var id = e.target.id;
      var row = id.charAt(0);
      var column = id.slice(1);

      //horizontal
      if (this.horizontal) {
        if (this.ship == "carrier") {
          this.length = +column + +5;
        } else if (this.ship == "battleship") {
          this.length = +column + +4;
        } else if (this.ship == "destroyer" || this.ship == "fighter") {
          this.length = +column + +3;
        } else if (this.ship == "patrol") {
          this.length = +column + +2;
        }
        if (this.length < 12) {
          document.getElementById(id).classList.add("ship");
          for (var i = column; i < this.length; i++) {
            var cell = row + i;
            document.getElementById(cell).classList.add("ship");
          }
        }
      } //vertical
      else if (this.vertical == true) {
        if (this.ship == "carrier") {
          this.length = 5;
        } else if (this.ship == "battleship") {
          this.length = 4;
        } else if (this.ship == "destroyer" || this.ship == "fighter") {
          this.length = 3;
        } else if (this.ship == "patrol") {
          this.length = 2;
        }
        if (
          (this.ship == "carrier" &&
            row != "G" &&
            row != "H" &&
            row != "I" &&
            row != "J") ||
          ((this.ship == "destroyer" || this.ship == "fighter") &&
            row != "I" &&
            row != "J") ||
          (this.ship == "battleship" &&
            row != "H" &&
            row != "I" &&
            row != "J") ||
          (this.ship == "patrol" && row != "J")
        ) {
          var a = this.y_axis.indexOf(row);
          document.getElementById(id).classList.add("ship");

          for (var i = a; i < this.length + a; i++) {
            var row = this.y_axis[i];
            var cell = row + column;
            document.getElementById(cell).classList.add("ship");
          }
        }
      }
    },

    // remove style when leaving cell
    dragLeave(e) {
      var id = e.target.id;
      var row = id.charAt(0);
      var column = id.slice(1);
      document.getElementById(id).classList.remove("ship");
      if (this.horizontal) {
        if (this.ship == "carrier") {
          this.length = +column + +5;
        } else if (this.ship == "battleship") {
          this.length = +column + +4;
        } else if (this.ship == "destroyer" || this.ship == "fighter") {
          this.length = +column + +3;
        } else if (this.ship == "patrol") {
          this.length = +column + +2;
        }

        for (var i = column; i < this.length; i++) {
          var cell = row + i;
          document.getElementById(cell).classList.remove("ship");
        }
      } //vertical
      else if (this.vertical == true) {
        if (this.ship == "carrier") {
          this.length = 5;
        } else if (this.ship == "battleship") {
          this.length = 4;
        } else if (this.ship == "destroyer" || this.ship == "fighter") {
          this.length = 3;
        } else if (this.ship == "patrol") {
          this.length = 2;
        }
        if (
          (this.ship == "carrier" &&
            row != "G" &&
            row != "H" &&
            row != "I" &&
            row != "J") ||
          ((this.ship == "destroyer" || this.ship == "fighter") &&
            row != "I" &&
            row != "J") ||
          (this.ship == "battleship" &&
            row != "H" &&
            row != "I" &&
            row != "J") ||
          (this.ship == "patrol" && row != "J")
        ) {
          var a = this.y_axis.indexOf(row);
          document.getElementById(id).classList.remove("ship");

          for (var i = a; i < this.length + a; i++) {
            var row = this.y_axis[i];
            var cell = row + column;
            document.getElementById(cell).classList.remove("ship");
          }
        }
      }
    },

    // adding ship to grid + to array which will be used to post ships
    drop(e) {
      this.playMusic("land");
      var id = e.target.id;
      var row = id.charAt(0);
      var column = id.slice(1);
      document.getElementById(this.ship + "Div").style.visibility = "hidden";

      //horizontal
      if (this.horizontal) {
        if (this.ship == "carrier") {
          this.length = +column + +5;
        } else if (this.ship == "battleship") {
          this.length = +column + +4;
        } else if (this.ship == "destroyer" || this.ship == "fighter") {
          this.length = +column + +3;
        } else if (this.ship == "patrol") {
          this.length = +column + +2;
        }
        if (this.length < 12) {
          document.getElementById(id).classList.add("drop");

          for (var i = column; i < this.length; i++) {
            var cell = row + i;
            document.getElementById(cell).classList.add("drop");
            this.test.push(cell);
          }
          var x = document.getElementsByClassName("drop");
          this.postShips();

          this.hide = false;
        }
      }
      //vertical
      else if (this.vertical == true) {
        if (this.ship == "carrier") {
          this.length = 5;
        } else if (this.ship == "battleship") {
          this.length = 4;
        } else if (this.ship == "destroyer" || this.ship == "fighter") {
          this.length = 3;
        } else if (this.ship == "patrol") {
          this.length = 2;
        }
        if (
          (this.ship == "carrier" &&
            row != "G" &&
            row != "H" &&
            row != "I" &&
            row != "J") ||
          ((this.ship == "destroyer" || this.ship == "fighter") &&
            row != "I" &&
            row != "J") ||
          (this.ship == "battleship" &&
            row != "H" &&
            row != "I" &&
            row != "J") ||
          (this.ship == "patrol" && row != "J")
        ) {
          var a = this.y_axis.indexOf(row);
          document.getElementById(id).classList.add("drop");

          for (var i = a; i < this.length + a; i++) {
            var row = this.y_axis[i];
            var cell = row + column;
            document.getElementById(cell).classList.add("drop");
            this.test.push(cell);
          }
          this.hide = false;
        }
        this.postShips();
      }

      // setting horizontal as set position
      this.horizontal = true;
      this.vertical = false;
      console.log("hello");
    },
    //------- MODAL-------------------------------------------------------------------------------------------

    openModal(selectedModal) {
      console.log("opening modal ->" + selectedModal);
      document.getElementById(selectedModal).style.visibility = "visible";
      this.modal.style.display = "block";
    },
    closeModal(selectedModal) {
      console.log("closing modal");
      document.getElementById(selectedModal).style.visibility = "hidden";
    },
    goHome() {
      location.href = "http://localhost:8080/web/games.html";
    },

    removeModal() {
      fetch("/api/howto", {
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        method: "POST",
        body: ""
      })
        .then(function(data) {
          console.log("Request success: ", data);
        })
        .catch(function(error) {
          console.log("Request failure: ", error);
        });
    }
    //------- WAIT FOR OPPONENT-------------------------------------------------------------------------------------------
  }

  //------- END OF METHODS -------------------------------------------------------------------------------------------
});
