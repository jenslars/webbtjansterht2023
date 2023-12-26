document.getElementById("URLinput").addEventListener("change", function() {
    var inputValue = this.value;
    var errorMessage = document.getElementById("error-message")
    var submitButton = document.getElementById("URLsubmit-btn")

    if (validateYouTubeUrl(inputValue)) {
        console.log("Valid YouTube URL");
        submitButton.classList.add("active");
        errorMessage.innerText = "";
    } else {
        console.log("Invalid YouTube URL");
        errorMessage.innerText = "Invalid YouTube URL";
    }    
});

function validateYouTubeUrl(urlToParse) {
    if (urlToParse) {
        var regExp = /^(?:https?:\/\/)?(?:m\.|www\.)?(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))((\w|-){11})(?:\S+)?$/;
        if (urlToParse.match(regExp)) {
            return true;
        }
    }
    return false;
}

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
