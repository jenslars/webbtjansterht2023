/* Metod för att validera url*/
var urlInput_1 = document.getElementsByClassName("URLinput")[0];

urlInput_1.addEventListener("change", function() {
    var inputValue = this.value;
    var errorMessage = document.getElementById("error-message1");
    var submitButton = document.getElementById("URLsubmit-btn1");

    if (validateYouTubeVideoTimestampUrl(inputValue)) {
        console.log("Valid YouTube URL");
        submitButton.classList.add("active");
        errorMessage.innerText = "";
    } else {
        console.log("Invalid YouTube URL");
        errorMessage.innerText = "Invalid YouTube URL";
    }
});

function validateYouTubeVideoTimestampUrl(urlToParse) {
    if (urlToParse) {
        var regExp = /^(https?:\/\/)?(www\.)?(youtube\.com\/(.*\/)?|youtu\.be\/)([\w-]{11})(\?.*t=([\dhms]+))?$/;
        if (urlToParse.match(regExp)) {
            return true;
        }
    }
    return false;
}

var urlInput_2 = document.getElementsByClassName("URLinput")[1];

urlInput_2.addEventListener("change", function() {
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
    }
});

function validateYouTubeVideoUrl(urlToParse) {
    if (urlToParse) {
        var regExp = /^(?:https?:\/\/)?(?:m\.|www\.)?(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))((\w|-){11})(?:\S+)?$/;
        if (urlToParse.match(regExp)) {
            return true;
        }
    }
    return false;
}

var urlInput_3 = document.getElementsByClassName("URLinput")[2];

urlInput_3.addEventListener("change", function() {
    var inputValue = this.value;
    var errorMessage = document.getElementById("error-message3");
    var submitButton = document.getElementById("URLsubmit-btn3");

    if (validateYouTubePlaylistUrl(inputValue)) {
        console.log("Valid YouTube URL");
        submitButton.classList.add("active");
        errorMessage.innerText = "";
    } else {
        console.log("Invalid YouTube URL");
        errorMessage.innerText = "Invalid YouTube URL";
    }
});

function validateYouTubePlaylistUrl(urlToParse) {
    if (urlToParse) {
        var regExp = /^(https?:\/\/)?(www\.)?(youtube\.com\/(.*\/)?|youtu\.be\/)playlist\?list=([\w-]+)(\?.*)?$/;
        if (urlToParse.match(regExp)) {
            return true;
        }
    }
    return false;
}

/* Metod för att visa rätt feature container*/
function toggleFeature(id) {
    var identifySongLink = document.getElementById("featureNavIdSong");
    var identifySongContainer = document.getElementById("identifySongContainer");

    var identifyPlaylistLink = document.getElementById("featureNavIdPlaylist");
    var identifyPlaylistContainer = document.getElementById("identifyPlaylistContainer");

    var convertPlaylistLink = document.getElementById("featureNavConvertPlaylist");
    var convertPlaylistContainer = document.getElementById("convertPlaylistContainer");

    if (id == "featureNavIdSong"){
        identifySongLink.classList.add("active")
        identifySongContainer.classList.add("active")

        identifyPlaylistLink.classList.remove("active")
        identifyPlaylistContainer.classList.remove("active")
        convertPlaylistLink.classList.remove("active")
        convertPlaylistContainer.classList.remove("active")
    }
    else if (id == "featureNavIdPlaylist"){
        identifyPlaylistLink.classList.add("active")
        identifyPlaylistContainer.classList.add("active")

        identifySongLink.classList.remove("active")
        identifySongContainer.classList.remove("active")
        convertPlaylistLink.classList.remove("active")
        convertPlaylistContainer.classList.remove("active")
    }
    else if (id == "featureNavConvertPlaylist"){
        convertPlaylistLink.classList.add("active")
        convertPlaylistContainer.classList.add("active")
        
        identifyPlaylistLink.classList.remove("active")
        identifyPlaylistContainer.classList.remove("active")
        identifySongLink.classList.remove("active")
        identifySongContainer.classList.remove("active")   
    }
}

/* Metod för att skicka spellista-url till backend*/
function convertPlaylist() {
    var url = document.getElementById("convertPlaylistInput").value;
    fetch('/convertPlaylist?url=' + encodeURIComponent(url), {
        method: 'GET',
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();  // Use response.text() for plain text
    })
    .then(data => {
        console.log('Backend response:', data);
        // Process the data as needed (e.g., update the UI)
    })
    .catch(error => {
        console.error('Error sending data to backend:', error);
    });
}