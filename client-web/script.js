const API_URI = "http://localhost:8080";

window.onload = globalInit();
async function globalInit() {
    const idElement = document.getElementById("userId");
    const unsetUserIdElement = document.getElementById("unsetUserId");

    idElement.addEventListener("change", setUserIdLS);
    unsetUserIdElement.addEventListener("click", unsetUserId);

    if(localStorage.getItem("userId")) idElement.value = localStorage.getItem("userId");
}

function setUserIdLS() {
    const idElement = document.getElementById("userId");
    const userId = idElement.value;
    
    localStorage.setItem("userId", userId);
}

function unsetUserId() {
    if(localStorage.getItem("userId")) localStorage.removeItem("userId");
    location.reload();
}
