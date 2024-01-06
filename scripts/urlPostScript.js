//Popup for spotify
function spotifyPopup(type) {
  if (type == "createPlaylist") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupCreate");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.add("active");
    mainContainer.classList.add("blur");
  } else if (type == "addToPlaylist") {
    var spotifyPopupDiv2 = document.getElementById("spotifyPopupAdd");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv2.classList.add("active");
    mainContainer.classList.add("blur");
  }
}

function cancelSpotifyPopup(type) {
  if (type == "createPlaylist") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupCreate");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.remove("active");
    mainContainer.classList.remove("blur");
  } else if (type == "addToPlaylist") {
    var spotifyPopupDiv = document.getElementById("spotifyPopupAdd");
    var mainContainer = document.getElementById("main");
    spotifyPopupDiv.classList.remove("active");
    mainContainer.classList.remove("blur");
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
  var submitButton = document.getElementById("URLsubmit-btn2");

  if (validateYouTubeVideoUrl(inputValue)) {
    console.log("Valid YouTube URL");
    submitButton.classList.add("active");
    errorMessage.innerText = "";
  } else {
    console.log("Invalid YouTube URL");
    errorMessage.innerText = "Invalid YouTube URL";
    submitButton.classList.remove("active");
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
    var identifyPlaylistContainer = document.getElementById("identifyPlaylistContainer");

    var convertPlaylistLink = document.getElementById("featureNavConvertPlaylist");
    var convertPlaylistContainer = document.getElementById("convertPlaylistContainer");
    var serviceContainer = document.getElementById('serviceContainer')

    if (id == "featureNavIdSong"){
        identifySongLink.classList.add("active")
        identifySongContainer.classList.add("active")

        identifyPlaylistLink.classList.remove("active")
        identifyPlaylistContainer.classList.remove("active")
        convertPlaylistLink.classList.remove("active")
        convertPlaylistContainer.classList.remove("active")
        
        const element = document.getElementById('resultContainerConvert');
        if (element) { element.classList.add('hide')
        serviceContainer.classList.remove('active')
        var expandedConvertPlaylistContainer = document.getElementById('expandedConvertPlaylistContainer')
        expandedConvertPlaylistContainer.classList.remove('active')
        }
    }
    else if (id == "featureNavIdPlaylist"){
        identifyPlaylistLink.classList.add("active")
        identifyPlaylistContainer.classList.add("active")

        identifySongLink.classList.remove("active")
        identifySongContainer.classList.remove("active")
        convertPlaylistLink.classList.remove("active")
        convertPlaylistContainer.classList.remove("active")

        const element = document.getElementById('resultContainerConvert');
        if (element) { element.classList.add('hide')
        serviceContainer.classList.remove('active')
        var expandedConvertPlaylistContainer = document.getElementById('expandedConvertPlaylistContainer')
        expandedConvertPlaylistContainer.classList.remove('active')
        }
    }
    else if (id == "featureNavConvertPlaylist"){
        convertPlaylistLink.classList.add("active")
        convertPlaylistContainer.classList.add("active")
        
        identifyPlaylistLink.classList.remove("active")
        identifyPlaylistContainer.classList.remove("active")
        identifySongLink.classList.remove("active")
        identifySongContainer.classList.remove("active")   
        var resultContainerConvert = document.getElementById('resultContainerConvert');
        if (resultContainerConvert) {
            resultContainerConvert.classList.remove('hide');
            var expandedConvertPlaylistContainer = document.getElementById('expandedConvertPlaylistContainer');
            expandedConvertPlaylistContainer.classList.add('active');
            serviceContainer.classList.add('active')
        }
    }
}

function connectToSpotify() {
  authenticateSpotify();
  // Delay fetch call by 5 seconds (5000 milliseconds)

  setTimeout(() => {
    //addSongToSpotify();
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
          throw new Error(
            "Network response was not ok: " + response.statusText
          );
        }
        return response.text().then((text) => (text ? JSON.parse(text) : {}));
      })
      .then((data) => {
        console.log(data);
        selectedTrackUris = [];
      })
      .catch((error) => {
        console.error("Error:", error);
      });
  }, 5000); // 5000 milliseconds = 5 seconds
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

  function authenticateSpotify() {
    const state = generateRandomString(16);
    let url = "https://accounts.spotify.com/authorize";
    url += "?response_type=code";
    url += "&client_id=" + encodeURIComponent(client_id);
    url += "&scope=" + encodeURIComponent(scope);
    url += "&redirect_uri=" + encodeURIComponent(redirect_uri);
    url += "&state=" + encodeURIComponent(state);
  
    const authWindow = window.open(url, "SpotifyAuthenticationWindow", "width=600,height=600");

  // Optional: You can focus on the new window
  if (authWindow) {
    authWindow.focus();
  }
  window.addEventListener('message', (event) => {
    if (event.data === 'authenticationComplete') {
        // Authentication is complete, proceed to create playlist
        createPlaylist();
    }
}, { once: true });
}

/* Metod för att skicka spellista-url till backend*/
function convertPlaylist() {
  console.log("In convert playlist");
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

        console.log("Extracted URIs:", selectedTrackUris);
      } else {
        console.error("Data.tracks is not an array");
      }

      var serviceContainer = document.getElementById("serviceContainer");
      var expandedConvertPlaylistContainer = document.getElementById(
        "expandedConvertPlaylistContainer"
      );

      expandedConvertPlaylistContainer.classList.add("active");
      serviceContainer.classList.add("active");
      createPlaylistElements(data);
    })
    .catch((error) => {
      console.error("Error sending data to backend:", error);
    });
}

function convertVideo() {
  console.log("In convert video");
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
          .slice(0, 50)
          .map((track) => track.uri)
          .filter((uri) => uri);

        console.log("Extracted URIs:", selectedTrackUris);
      } else {
        console.error("Data.tracks is not an array");
      }
    })
    .catch((error) => {
      console.error("Error sending data to backend:", error);
    });
}

function createPlaylistElements(data) {
  var expandedConvertPlaylistContainer = document.getElementById(
    "expandedConvertPlaylistContainer"
  );

  var resultContainerConvert = document.getElementById("resultContainer");
  if (!resultContainerConvert) {
    resultContainerConvert = document.createElement("div");
    resultContainerConvert.id = "resultContainerConvert";
    expandedConvertPlaylistContainer.appendChild(resultContainerConvert);
  }

  var resultDivider = document.createElement("div");
  resultDivider.className = "resultDivider";
  resultContainerConvert.appendChild(resultDivider);

  var resultHeader = document.createElement("div");
  resultHeader.className = "resultHeader";
  resultContainerConvert.appendChild(resultHeader);

  var h3Element = document.createElement("h3");
  h3Element.textContent = "Songs identified on Spotify";
  resultHeader.appendChild(h3Element);

  var resultSpotifyButtons = document.createElement("div");
  resultSpotifyButtons.className = "resultSpotifyButtons";
  resultHeader.appendChild(resultSpotifyButtons);

  var addToPlaylistBtn = document.createElement("button");
  addToPlaylistBtn.className = "addToPlaylist-btn";
  addToPlaylistBtn.textContent = "Add to playlist";
  addToPlaylistBtn.onclick = function () {
    spotifyPopup("addToPlaylist");
  };
  resultSpotifyButtons.appendChild(addToPlaylistBtn);

  var createPlaylistBtn = document.createElement("button");
  createPlaylistBtn.className = "createPlaylist-btn";
  createPlaylistBtn.textContent = "Create new playlist";
  createPlaylistBtn.onclick = function () {
    spotifyPopup("createPlaylist");
  };
  resultSpotifyButtons.appendChild(createPlaylistBtn);

  var scrollContainer = document.createElement("div");
  scrollContainer.className = "scrollContainer";
  resultContainerConvert.appendChild(scrollContainer);

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
    spanTrackId.textContent = track.id;
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
