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

//Scope som skall hämtas
const scope =
  "user-read-private playlist-modify-public playlist-modify-private";

document.getElementById("login-btn").addEventListener("click", () => {
  const state = generateRandomString(16);
  let url = "https://accounts.spotify.com/authorize";
  url += "?response_type=code";
  url += "&client_id=" + encodeURIComponent(client_id);
  url += "&scope=" + encodeURIComponent(scope);
  url += "&redirect_uri=" + encodeURIComponent(redirect_uri);
  url += "&state=" + encodeURIComponent(state);

  window.location.href = url;
});
