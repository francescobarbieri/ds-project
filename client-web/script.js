const API_URI = "http://localhost:8080";

window.onload = init();
async function init() {
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

/*
async function init() {
  const mainForm = document.getElementById("main-form");
  if (mainForm) mainForm.addEventListener("submit", reserveDomain);
}
 
async function reserveDomain(event) {
    event.preventDefault();
    
    const form = document.getElementById("main-form");
    const formData = new FormData(form);
    const domain  = formData.get("domain");

    const endpoint = `${API_URI}/reserveDomain/`;

    /*
    const response = await fetch(endpoint, {
        method: "POST",
        headers: {
            "Content-type": "application/json",
            "Access-Control-Request-Method": "PUT",
            "Access-Control-Request-Headers": "content-type",
            "Host": "localhost:8080",
            "Sec-Fetch-Dest": "empty",
            "Sec-Fetch-Mode": "cors",
            "Sec-Fetch-Site": "same-site"
        },
        body: JSON.stringify(),
    });
    

    if(false) { // !response.ok
        if(true) { // response.status === 409
            const errorBox = document.getElementById("error-box");
            errorBox.style.display = "block";
            return;
        } else {
            throw new Error(`Generic error. ${response.status} ${response.statusText}`);
        }
    }
    // const data = await response.json();
    window.location.href = `buy.html?domain=${domain}`;
}

*/