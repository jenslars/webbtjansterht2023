//Popup for spotify
function spotifyPopup(type, feature) {
  if (type == "createPlaylist") {
    var connectButton = document.getElementsByClassName("login-btn")[0];
    var spotifyPopupDiv = document.getElementById("spotifyPopupCreate");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.add("active");
    mainContainer.classList.add("blur");
    if (feature == "convertPlaylist") {
      connectButton.onclick = function () {
        authenticateSpotify("createPlaylist", "resultContainerConvert");
      };
    } else if (feature == "identifySong") {
      connectButton.onclick = function () {
        authenticateSpotify("createPlaylist", "resultContainerIdentifySong");
      };
    } else if (feature == "identifyAllSongs") {
      connectButton.onclick = function () {
        authenticateSpotify(
          "createPlaylist",
          "resultContainerIdentifyAllSongs"
        );
      };
    }
  } else if (type == "addToPlaylist") {
    var connectButton = document.getElementsByClassName("login-btn")[1];
    var spotifyPopupDiv2 = document.getElementById("spotifyPopupAdd");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv2.classList.add("active");
    mainContainer.classList.add("blur");
    if (feature == "convertPlaylist") {
      connectButton.onclick = function () {
        authenticateSpotify("addToPlaylist", "resultContainerConvert");
      };
    } else if (feature == "identifySong") {
      connectButton.onclick = function () {
        authenticateSpotify("addToPlaylist", "resultContainerIdentifySong");
      };
    } else if (feature == "identifyAllSongs") {
      connectButton.onclick = function () {
        authenticateSpotify("addToPlaylist", "resultContainerIdentifyAllSongs");
      };
    }
  }
}

function cancelSpotifyPopup(type) {
  if (type == "createPlaylist") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupCreate");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.remove("active");
    mainContainer.classList.remove("blur");
  } else if (type == "createPlaylistFinished") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupCreate");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.remove("active");
    mainContainer.classList.remove("blur");
    resetSpotifyPopup("createPlaylistFinished");
  } else if (type == "addToPlaylist") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupAdd");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.remove("active");
    mainContainer.classList.remove("blur");
  } else if (type == "addToPlaylistFinished") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupAdd");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.remove("active");
    mainContainer.classList.remove("blur");
    resetSpotifyPopup("addToPlaylistFinished");
  }
}

/* Metod för att validera url*/
var urlInput_1 = document.getElementsByClassName("URLinput")[0];

urlInput_1.addEventListener("input", function () {
  var inputValue = this.value;
  var errorMessage = document.getElementById("error-message1");
  var submitButton = document.getElementById("URLsubmit-btn1");

  if (
    validateYouTubeVideoTimestampUrl(inputValue) ||
    validateYouTubeVideoUrl(inputValue)
  ) {
    console.log("Valid YouTube URL");
    submitButton.classList.add("active");
    errorMessage.innerText = "";
    submitButton.onclick = function () {
      convertVideo();
    };
  } else {
    console.log("Invalid YouTube URL");
    errorMessage.innerText = "Invalid YouTube URL";
    submitButton.classList.remove("active");
  }
});

function validateYouTubeVideoTimestampUrl(urlToParse) {
  if (urlToParse) {
    var regExp =
      /^(https?:\/\/)?(www\.)?(youtube\.com\/(.*\/)?|youtu\.be\/)([\w-]{11})(\?.*t=([\dhms]+))?$/;
    if (urlToParse.match(regExp)) {
      return true;
    }
  }
  return false;
}

function validateYouTubeVideoUrl(urlToParse) {
  if (urlToParse) {
    var regExp =
      /^(https?:\/\/)?(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)([\w-]{11})(\?.*)?$/;
    if (urlToParse.match(regExp)) {
      return true;
    }
  }
  return false;
}

var urlInput_2 = document.getElementsByClassName("URLinput")[1];

urlInput_2.addEventListener("input", function () {
  var inputValue = this.value;
  var errorMessage = document.getElementById("error-message2");
  var submitButton2 = document.getElementById("URLsubmit-btn2");

  if (validateYouTubeVideoUrl(inputValue)) {
    console.log("Valid YouTube URL");
    submitButton2.classList.add("active");
    errorMessage.innerText = "";
    submitButton2.onclick = function () {
      identifyAllSongs();
    };
  } else {
    console.log("Invalid YouTube URL");
    errorMessage.innerText = "Invalid YouTube URL";
    submitButton2.classList.remove("active");
  }
});

var urlInput_3 = document.getElementsByClassName("URLinput")[2];

urlInput_3.addEventListener("input", function () {
  var inputValue = this.value;
  var errorMessage3 = document.getElementById("error-message3");
  var submitButton3 = document.getElementById("URLsubmit-btn3");

  if (validateYouTubePlaylistUrl(inputValue)) {
    console.log("Valid YouTube URL");
    submitButton3.classList.add("active");
    errorMessage3.innerText = "";
    submitButton3.onclick = function () {
      convertPlaylist();
    };
  } else {
    console.log("Invalid YouTube URL");
    errorMessage3.innerText = "Invalid YouTube URL";
    submitButton3.classList.remove("active");
    submitButton3.onclick = null;
  }
});

function validateYouTubePlaylistUrl(urlToParse) {
  if (urlToParse) {
    var regExp =
      /^(https?:\/\/)?(www\.)?(youtube\.com\/(.*\/)?|youtu\.be\/)playlist\?list=([\w-]+)(\?.*)?$/;
    if (urlToParse.match(regExp)) {
      return true;
    }
  }
  return false;
}

function toggleCheckbox(checkbox) {
  checkbox.classList.toggle("active");
}

/* Metod för att visa rätt feature container*/
function toggleFeature(id) {
  var identifySongLink = document.getElementById("featureNavIdSong");
  var identifySongContainer = document.getElementById("identifySongContainer");

  var identifyPlaylistLink = document.getElementById("featureNavIdPlaylist");
  var identifyPlaylistContainer = document.getElementById(
    "identifyPlaylistContainer"
  );

  var convertPlaylistLink = document.getElementById(
    "featureNavConvertPlaylist"
  );
  var convertPlaylistContainer = document.getElementById(
    "convertPlaylistContainer"
  );

  var serviceContainer = document.getElementById("serviceContainer");

  if (id == "featureNavIdSong") {
    identifySongLink.classList.add("active");
    identifySongContainer.classList.add("active");

    identifyPlaylistLink.classList.remove("active");
    identifyPlaylistContainer.classList.remove("active");
    convertPlaylistLink.classList.remove("active");
    convertPlaylistContainer.classList.remove("active");

    const resultContainerConvert = document.getElementById(
      "resultContainerConvert"
    );
    if (resultContainerConvert) {
      resultContainerConvert.classList.add("hide");
      serviceContainer.classList.remove("active");
      var expandedConvertPlaylistContainer = document.getElementById(
        "expandedConvertPlaylistContainer"
      );
      expandedConvertPlaylistContainer.classList.remove("active");
    }

    var resultContainerIdentifySong = document.getElementById(
      "resultContainerIdentifySong"
    );
    if (resultContainerIdentifySong) {
      resultContainerIdentifySong.classList.remove("hide");
      var expandedIdentifySongContainer = document.getElementById(
        "expandedIdentifySongContainer"
      );
      expandedIdentifySongContainer.classList.add("active");
      serviceContainer.classList.add("active");
    }
  } else if (id == "featureNavIdPlaylist") {
    identifyPlaylistLink.classList.add("active");
    identifyPlaylistContainer.classList.add("active");

    identifySongLink.classList.remove("active");
    identifySongContainer.classList.remove("active");
    convertPlaylistLink.classList.remove("active");
    convertPlaylistContainer.classList.remove("active");

    const resultContainerConvert = document.getElementById(
      "resultContainerConvert"
    );
    if (resultContainerConvert) {
      resultContainerConvert.classList.add("hide");
      serviceContainer.classList.remove("active");
      var expandedConvertPlaylistContainer = document.getElementById(
        "expandedConvertPlaylistContainer"
      );
      expandedConvertPlaylistContainer.classList.remove("active");
    }

    const resultContainerIdentifySong = document.getElementById(
      "resultContainerIdentifySong"
    );
    if (resultContainerIdentifySong) {
      resultContainerIdentifySong.classList.add("hide");
      serviceContainer.classList.remove("active");
      var expandedIdentifySongContainer = document.getElementById(
        "expandedIdentifySongContainer"
      );
      expandedIdentifySongContainer.classList.remove("active");
    }

    var resultContainerIdentifyAllSongs = document.getElementById(
      "resultContainerIdentifyAllSongs"
    );
    if (resultContainerIdentifyAllSongs) {
      resultContainerIdentifyAllSongs.classList.remove("hide");
      var expandedIdentifyAllSongsContainer = document.getElementById(
        "expandedIdentifyAllSongsContainer"
      );
      expandedIdentifyAllSongsContainer.classList.add("active");
      serviceContainer.classList.add("active");
    }
  } else if (id == "featureNavConvertPlaylist") {
    convertPlaylistLink.classList.add("active");
    convertPlaylistContainer.classList.add("active");
    identifyPlaylistLink.classList.remove("active");
    identifyPlaylistContainer.classList.remove("active");
    identifySongLink.classList.remove("active");
    identifySongContainer.classList.remove("active");

    const resultContainerIdentifySong = document.getElementById(
      "resultContainerIdentifySong"
    );
    if (resultContainerIdentifySong) {
      resultContainerIdentifySong.classList.add("hide");
      serviceContainer.classList.remove("active");
      var expandedIdentifySongContainer = document.getElementById(
        "expandedIdentifySongContainer"
      );
      expandedIdentifySongContainer.classList.remove("active");
    }

    const resultContainerIdentifyAllSongs = document.getElementById(
      "resultContainerIdentifyAllSongs"
    );
    if (resultContainerIdentifyAllSongs) {
      resultContainerIdentifyAllSongs.classList.add("hide");
      serviceContainer.classList.remove("active");
      var expandedIdentifyAllSongsContainer = document.getElementById(
        "expandedIdentifyAllSongsContainer"
      );
      expandedIdentifyAllSongsContainer.remove("active");
    }

    var resultContainerConvert = document.getElementById(
      "resultContainerConvert"
    );
    if (resultContainerConvert) {
      resultContainerConvert.classList.remove("hide");
      var expandedConvertPlaylistContainer = document.getElementById(
        "expandedConvertPlaylistContainer"
      );
      expandedConvertPlaylistContainer.classList.add("active");
      serviceContainer.classList.add("active");
    }
  }
}

function fetchSelectedTrackUris(type) {
  console.log(type);
  var selectedResultContainer = document.getElementById(type);
  var selectedTrackUris = [];
  if (!selectedResultContainer) {
    console.error(`Element with id "${type}" not found.`);
    return;
  }

  selectedResultContainer
    .querySelectorAll(".tableTrackRow")
    .forEach((trackRow) => {
      const checkbox = trackRow.querySelector(".customCheckbox");
      if (checkbox.classList.contains("active")) {
        const trackUri = trackRow.querySelector(".trackUri").textContent;
        selectedTrackUris.push(trackUri);
      }
    });
  console.log(selectedTrackUris);
  return selectedTrackUris;
}

function createPlaylist(type) {
  selectedTrackUris = fetchSelectedTrackUris(type);
  console.log("selected URIS:", selectedTrackUris);
  fetch("http://localhost:5000/createPlaylist", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ trackUris: selectedTrackUris }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok: " + response.statusText);
      }
      return response.json();
    })
    .then((data) => {
      console.log("Playlist ID:", data.playlistId);
      selectedTrackUris = [];

      const spotifyPlaylistUrl = `https://open.spotify.com/playlist/${data.playlistId}`;
      console.log("Spotify Playlist URL:", spotifyPlaylistUrl);
      createPlaylistUrlElements(spotifyPlaylistUrl);
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

function getUserPlaylists(feature) {
  fetch("http://localhost:5000/getUserPlaylists", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      console.log(data);
      displayUserPlaylists(data, feature);
    })
    .catch((error) => {
      console.error("Fetch error:", error);
    });
}

function displayUserPlaylists(data, feature) {
  var selectedTrackUris = fetchSelectedTrackUris(feature);
  var spotifyPopupAdd = document.getElementById("spotifyPopupAdd");
  var containerDiv = document.querySelector(".lds-ring-container");
  if (containerDiv) {
    containerDiv.remove();
  }

  var pElement = spotifyPopupAdd.querySelector("p");
  pElement.textContent =
    "Please select the playlist that you wish to add the songs to";

  var playlistContainerScroll = document.createElement("div");
  playlistContainerScroll.className = "playlistContainerScroll";
  spotifyPopupAdd.appendChild(playlistContainerScroll);

  data.forEach(function (playlist) {
    var playlistContainer = document.createElement("div");
    playlistContainer.className = "playlistContainer";
    playlistContainerScroll.appendChild(playlistContainer);

    playlistContainer.onclick = function () {
      addToPlaylist(playlist.id, selectedTrackUris);
    };

    var playlistImage = document.createElement("img");
    playlistImage.className = "playlistImage";
    playlistImage.src = playlist.imageUrl;
    playlistContainer.appendChild(playlistImage);

    var playlistParagraphElements = document.createElement("div");
    playlistParagraphElements.className = "playlistParagraphs";
    playlistContainer.appendChild(playlistParagraphElements);

    var playlistName = document.createElement("p");
    playlistName.className = "playlistName";
    playlistName.textContent = playlist.name;
    playlistParagraphElements.appendChild(playlistName);

    var playlistTracks = document.createElement("p");
    playlistTracks.className = "playlistTracks";
    playlistTracks.textContent = playlist.trackCount + " songs";
    playlistParagraphElements.appendChild(playlistTracks);
  });
}

function addToPlaylist(selectedPlaylistId, selectedTrackUris) {
  loadAddToPlaylist();
  fetch("http://localhost:5000/addTracksToPlaylist", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      playlistId: selectedPlaylistId,
      trackUris: selectedTrackUris,
    }),
  })
    .then((response) => response.json())
    .then((data) => {
      console.log(data);
      confirmAddToPlaylist(selectedPlaylistId);
    })
    .catch((error) => {
      console.error("Error adding tracks to playlist:", error);
    });
}

function loadAddToPlaylist() {
  var playlistContainerScroll = document.getElementsByClassName(
    "playlistContainerScroll"
  )[0];
  playlistContainerScroll.remove();
  spotifyAuthenticateLoadingAnimation("addToPlaylist");
}

function confirmAddToPlaylist(playlistId) {
  var containerDiv = document.querySelector(".lds-ring-container");

  if (containerDiv) {
    containerDiv.remove();
  }
  var spotifyPopupAdd = document.getElementById("spotifyPopupAdd");
  var h1Element = spotifyPopupAdd.querySelector("h3");
  h1Element.textContent = "Songs added to playlist";
  var pElement = spotifyPopupAdd.querySelector("p");
  pElement.textContent = "Visit playlist here:";
  var spotifyPlaylistUrl = `https://open.spotify.com/playlist/${playlistId}`;
  var linkElement = document.createElement("a");
  linkElement.textContent = spotifyPlaylistUrl;
  linkElement.href = spotifyPlaylistUrl;
  spotifyPopupAdd.appendChild(linkElement);

  var resetButton = document.createElement("button");
  resetButton.classList.add("cancelSpotifyPopup");
  resetButton.textContent = "Close";
  resetButton.setAttribute("id", "resetButton");

  resetButton.onclick = function () {
    cancelSpotifyPopup("addToPlaylistFinished");
  };
  spotifyPopupAdd.appendChild(resetButton);
}

function addLoaderToButton(buttonId) {
  var button = document.getElementById(buttonId);
  if (!button) {
    console.error("Button not found:", buttonId);
    return;
  }

  var loaderDiv = document.createElement("div");
  loaderDiv.className = "lds-ring2";

  for (var i = 0; i < 4; i++) {
    var childDiv = document.createElement("div");
    loaderDiv.appendChild(childDiv);
  }

  button.appendChild(loaderDiv);
}

function removeLoaderFromButton(buttonId) {
  var button = document.getElementById(buttonId);
  if (!button) {
    console.error("Button not found:", buttonId);
    return;
  }

  // Find the loader div inside the button
  var loaderDiv = button.querySelector(".lds-ring2");
  if (loaderDiv) {
    button.removeChild(loaderDiv);
  } else {
    console.error("Loader not found in button:", buttonId);
  }
}

function createPlaylistUrlElements(playlistUrl) {
  var spotifyPopupCreate = document.getElementById("spotifyPopupCreate");
  var containerDiv = document.querySelector(".lds-ring-container");

  if (containerDiv) {
    containerDiv.remove();
  }

  var h3Element = spotifyPopupCreate.querySelector("h3");
  h3Element.textContent = "Playlist created";

  var pElement = spotifyPopupCreate.querySelector("p");
  pElement.textContent = "Visit playlist here:";

  var linkElement = document.createElement("a");
  linkElement.textContent = playlistUrl;
  linkElement.href = playlistUrl;
  spotifyPopupCreate.appendChild(linkElement);

  var resetButton = document.createElement("button");
  resetButton.classList.add("cancelSpotifyPopup");
  resetButton.textContent = "Close";
  resetButton.setAttribute("id", "resetButton");

  resetButton.onclick = function () {
    cancelSpotifyPopup("createPlaylistFinished");
  };

  spotifyPopupCreate.appendChild(resetButton);
}

function spotifyAuthenticateLoadingAnimation(type) {
  if (type == "createPlaylist") {
    var chosenPopup = document.getElementById("spotifyPopupCreate");
    var loadingElements = document.getElementsByClassName("loadingElements")[0];
  } else if (type == "addToPlaylist") {
    var chosenPopup = document.getElementById("spotifyPopupAdd");
    var loadingElements = document.getElementsByClassName("loadingElements")[1];
  }

  loadingElements.classList.add("hide");

  const containerDiv = document.createElement("div");
  containerDiv.classList.add("lds-ring-container");

  const parentDiv = document.createElement("div");
  parentDiv.classList.add("lds-ring");

  for (let i = 0; i < 4; i++) {
    const childDiv = document.createElement("div");
    parentDiv.appendChild(childDiv);
  }

  containerDiv.appendChild(parentDiv);

  chosenPopup.appendChild(containerDiv);
}

function resetSpotifyPopup(type) {
  if (type == "createPlaylistFinished") {
    var spotifyPopup = document.getElementById("spotifyPopupCreate");
  } else if (type == "addToPlaylistFinished") {
    var spotifyPopup = document.getElementById("spotifyPopupAdd");
  }

  var containerDiv = document.querySelector(".lds-ring-container");

  if (containerDiv) {
    containerDiv.remove();
  }

  var h3Element = spotifyPopup.querySelector("h3");
  var pElement = spotifyPopup.querySelector("p");

  if (type == "createPlaylistFinished") {
    h3Element.textContent = "Creating playlist";
    pElement.textContent =
      "In order to create a playlist you need to connect to Spotify";
  } else if (type == "addToPlaylistFinished") {
    h3Element.textContent = "Adding to playlist";
    pElement.textContent =
      "In order to add these songs to a playlist you need to connect to Spotify";
  }

  var linkElement = spotifyPopup.querySelector("a");
  if (linkElement) {
    linkElement.remove();
  }

  var resetButton = document.getElementById("resetButton");
  if (resetButton) {
    resetButton.remove();
  }

  if (type == "createPlaylistFinished") {
    var loadingElements = document.getElementsByClassName("loadingElements")[0];
  } else if (type == "addToPlaylistFinished") {
    var loadingElements = document.getElementsByClassName("loadingElements")[1];
  }
  loadingElements.classList.remove("hide");
}

const client_id = "c32d1829b55d4c5eac178bc34fdd6728";
const redirect_uri = "http://localhost:5000/callback";

// random sträng
function generateRandomString(length) {
  let text = "";
  const possible =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  for (let i = 0; i < length; i++)
    text += possible.charAt(Math.floor(Math.random() * possible.length));

  return text;
}

// Scope som skall hämtas
const scope =
  "user-read-private playlist-modify-public playlist-modify-private";

function authenticateSpotify(type, feature) {
  if (type == "createPlaylist") {
    spotifyAuthenticateLoadingAnimation("createPlaylist");
  } else if (type == "addToPlaylist") {
    spotifyAuthenticateLoadingAnimation("addToPlaylist");
  }

  const state = generateRandomString(16);
  let url = "https://accounts.spotify.com/authorize";
  url += "?response_type=code";
  url += "&client_id=" + encodeURIComponent(client_id);
  url += "&scope=" + encodeURIComponent(scope);
  url += "&redirect_uri=" + encodeURIComponent(redirect_uri);
  url += "&state=" + encodeURIComponent(state);

  const authWindow = window.open(
    url,
    "SpotifyAuthenticationWindow",
    "width=600,height=600"
  );

  if (authWindow) {
    authWindow.focus();
  }
  window.addEventListener(
    "message",
    (event) => {
      if (event.data === "authenticationComplete") {
        if (type == "createPlaylist") {
          createPlaylist(feature);
        } else if (type == "addToPlaylist") {
          getUserPlaylists(feature);
        }
      }
    },
    { once: true }
  );
}

/* Metod för att skicka spellista-url till backend*/
function convertPlaylist() {
  addLoaderToButton("URLsubmit-btn3");
  var url = document.getElementById("convertPlaylistInput").value;
  fetch("/convertPlaylist?url=" + encodeURIComponent(url), {
    method: "GET",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      console.log("Backend response:", data);
      if (data.tracks && Array.isArray(data.tracks)) {
        // Extract the URIs from the array and filter out empty ones
        selectedTrackUris = data.tracks
          .slice(0, 50)
          .map((track) => track.uri)
          .filter((uri) => uri);
        var serviceContainer = document.getElementById("serviceContainer");
        var expandedConvertPlaylistContainer = document.getElementById(
          "expandedConvertPlaylistContainer"
        );

        expandedConvertPlaylistContainer.classList.add("active");
        serviceContainer.classList.add("active");
        removeLoaderFromButton("URLsubmit-btn3");
        console.log("Extracted URIs:", selectedTrackUris);
      } else {
        console.error("Data.tracks is not an array");
      }

      createPlaylistElements(data, "convertPlaylist");
    })
    .catch((error) => {
      console.error("Error sending data to backend:", error);
    });
}

function identifyAllSongs() {
  console.log("In identifyAllSongs");
  addLoaderToButton("URLsubmit-btn2");
  var url = document.getElementById("convertPlaylistInput").value;
  fetch("/identifyAllSongsInVideo?url=" + encodeURIComponent(url), {
    method: "GET",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      console.log("Backend response:", data);
      if (data.tracks && Array.isArray(data.tracks)) {
        // Extract the URIs from the array and filter out empty ones
        selectedTrackUris = data.tracks
          .slice(0, 50)
          .map((track) => track.uri)
          .filter((uri) => uri);
        var serviceContainer = document.getElementById("serviceContainer");
        var expandedConvertPlaylistContainer = document.getElementById(
          "expandedIdentifyAllSongsContainer"
        );

        expandedConvertPlaylistContainer.classList.add("active");
        serviceContainer.classList.add("active");
        createPlaylistElements(data, "identifyAllSongs");
        removeLoaderFromButton("URLsubmit-btn2");
        console.log("Extracted URIs:", selectedTrackUris);
      } else {
        console.error("Data.tracks is not an array");
      }
    })
    .catch((error) => {
      console.error("Error sending data to backend:", error);
    });
}

function convertVideo() {
  console.log("In convert video");

  addLoaderToButton("URLsubmit-btn1");
  var url = document.getElementById("convertVideoInput").value;
  fetch("/convertVideo?url=" + encodeURIComponent(url), {
    method: "GET",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      console.log("Backend response:", data);
      if (data.tracks && Array.isArray(data.tracks)) {
        // Extract the URIs from the array and filter out empty ones
        selectedTrackUris = data.tracks
          .slice(0, 100)
          .map((track) => track.uri)
          .filter((uri) => uri);

        console.log("Extracted URIs:", selectedTrackUris);
        var serviceContainer = document.getElementById("serviceContainer");
        var expandedIdentifySongContainer = document.getElementById(
          "expandedIdentifySongContainer"
        );

        expandedIdentifySongContainer.classList.add("active");
        serviceContainer.classList.add("active");
        createPlaylistElements(data, "identifySong");
        removeLoaderFromButton("URLsubmit-btn1");
      } else {
        console.error("Data.tracks is not an array");
      }
    })
    .catch((error) => {
      console.error("Error sending data to backend:", error);
    });
}

function createPlaylistElements(data, feature) {
  if (feature == "identifySong") {
    var expandedContainer = document.getElementById(
      "expandedIdentifySongContainer"
    );
    var resultContainer = document.getElementById(
      "resultContainerIdentifySong"
    );
  } else if (feature == "convertPlaylist") {
    var expandedContainer = document.getElementById(
      "expandedConvertPlaylistContainer"
    );
    var resultContainer = document.getElementById("resultContainerConvert");
  } else if (feature == "identifyAllSongs") {
    var expandedContainer = document.getElementById(
      "expandedIdentifyAllSongsContainer"
    );
    var resultContainer = document.getElementById(
      "resultContainerIdentifyAllSongs"
    );
  }

  // Check if the resultContainerConvert element exists
  if (resultContainer) {
    // Remove the existing resultContainerConvert and its children
    resultContainer.parentNode.removeChild(resultContainer);
  }

  // Create a new resultContainerConvert element
  resultContainer = document.createElement("div");
  if (feature == "identifySong") {
    resultContainer.id = "resultContainerIdentifySong";
  } else if (feature == "convertPlaylist") {
    resultContainer.id = "resultContainerConvert";
  } else if (feature == "identifyAllSongs") {
    resultContainer.id = "resultContainerIdentifyAllSongs";
  }
  expandedContainer.appendChild(resultContainer);

  var resultDivider = document.createElement("div");
  resultDivider.className = "resultDivider";
  resultContainer.appendChild(resultDivider);

  var resultHeader = document.createElement("div");
  resultHeader.className = "resultHeader";
  resultContainer.appendChild(resultHeader);

  var h3Element = document.createElement("h3");
  h3Element.textContent = "Songs identified on Spotify";
  resultHeader.appendChild(h3Element);

  var resultSpotifyButtons = document.createElement("div");
  resultSpotifyButtons.className = "resultSpotifyButtons";
  resultHeader.appendChild(resultSpotifyButtons);

  var addToPlaylistBtn = document.createElement("button");
  addToPlaylistBtn.className = "addToPlaylist-btn";
  addToPlaylistBtn.textContent = "Add to playlist";

  if (feature == "identifySong") {
    addToPlaylistBtn.onclick = function () {
      spotifyPopup("addToPlaylist", "convertVideo");
    };
  } else if (feature == "convertPlaylist") {
    addToPlaylistBtn.onclick = function () {
      spotifyPopup("addToPlaylist", "convertPlaylist");
    };
  } else if (feature == "identifyAllSongs") {
    addToPlaylistBtn.onclick = function () {
      spotifyPopup("addToPlaylist", "identifyAllSongs");
    };
  }

  resultSpotifyButtons.appendChild(addToPlaylistBtn);

  var createPlaylistBtn = document.createElement("button");
  createPlaylistBtn.className = "createPlaylist-btn";
  createPlaylistBtn.textContent = "Create new playlist";
  if (feature == "identifySong") {
    createPlaylistBtn.onclick = function () {
      spotifyPopup("createPlaylist", "convertVideo");
    };
  } else if (feature == "convertPlaylist") {
    createPlaylistBtn.onclick = function () {
      spotifyPopup("createPlaylist", "convertPlaylist");
    };
  } else if (feature == "identifyAllSongs") {
    createPlaylistBtn.onclick = function () {
      spotifyPopup("createPlaylist", "identifyAllSongs");
    };
  }
  resultSpotifyButtons.appendChild(createPlaylistBtn);

  var scrollContainer = document.createElement("div");
  scrollContainer.className = "scrollContainer";
  resultContainer.appendChild(scrollContainer);

  var spotifyTable = document.createElement("table");
  spotifyTable.className = "spotifyTable";
  scrollContainer.appendChild(spotifyTable);

  var theadElement = document.createElement("thead");
  spotifyTable.appendChild(theadElement);

  var trHead = document.createElement("tr");
  trHead.className = "tableHead";
  theadElement.appendChild(trHead);

  var thInclude = document.createElement("th");
  thInclude.textContent = "Include";
  trHead.appendChild(thInclude);

  var thTitle = document.createElement("th");
  thTitle.textContent = "Title";
  trHead.appendChild(thTitle);

  var thAlbum = document.createElement("th");
  thAlbum.textContent = "Album";
  trHead.appendChild(thAlbum);

  var tbodyElement = document.createElement("tbody");
  spotifyTable.appendChild(tbodyElement);

  var trDivider = document.createElement("tr");
  trDivider.className = "tableDivider";
  tbodyElement.appendChild(trDivider);

  for (var i = 0; i < data.tracks.length; i++) {
    var track = data.tracks[i];

    var trTrack = document.createElement("tr");
    trTrack.className = "tableTrackRow";
    tbodyElement.appendChild(trTrack);

    var tdCheckbox = document.createElement("td");
    trTrack.appendChild(tdCheckbox);

    var divCheckbox = document.createElement("div");
    divCheckbox.className = "customCheckbox active";
    divCheckbox.onclick = function () {
      toggleCheckbox(this);
    };
    tdCheckbox.appendChild(divCheckbox);

    var tdTitle = document.createElement("td");
    trTrack.appendChild(tdTitle);

    var divTitleRow = document.createElement("div");
    divTitleRow.className = "titleRow";
    tdTitle.appendChild(divTitleRow);

    var spanTrackId = document.createElement("span");
    spanTrackId.className = "trackUri";
    spanTrackId.textContent = track.uri;
    spanTrackId.style.display = "none";
    divTitleRow.appendChild(spanTrackId);

    var imgTitle = document.createElement("img");
    imgTitle.src = track.imageUrl;
    imgTitle.alt = track.title;
    divTitleRow.appendChild(imgTitle);

    var divTitleRowText = document.createElement("div");
    divTitleRowText.className = "titleRowText";
    divTitleRow.appendChild(divTitleRowText);

    var pTitleSong = document.createElement("p");
    pTitleSong.className = "titleSong";
    pTitleSong.textContent = track.title;
    divTitleRowText.appendChild(pTitleSong);

    var pTitleArtist = document.createElement("p");
    pTitleArtist.className = "titleArtist";
    pTitleArtist.textContent = track.artist;
    divTitleRowText.appendChild(pTitleArtist);

    var tdAlbum = document.createElement("td");
    tdAlbum.className = "titleAlbum";
    tdAlbum.textContent = track.album;
    trTrack.appendChild(tdAlbum);
  }
}
