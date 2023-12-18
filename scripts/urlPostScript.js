document.getElementById("URLinput").addEventListener("change", function() {
    var inputValue = this.value;
    var errorMessage = document.getElementById("error-message")

    if (validateYouTubeUrl(inputValue)) {
        console.log("Valid YouTube URL");
        errorMessage.innerText = "Valid YouTube URL";
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
