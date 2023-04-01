document.getElementById("IDbox").addEventListener("click", getAPI);

function openHomePage() {
    window.location.href = 'https:/';
}

function getAPI() {
    document.getElementById("resulttext").innerHTML = ""
    fetch("localhost:8000/WholeCart")
        .then(response => response.text())
        .then(data => myFunc(data));
}

function myFunc(Result) {
    document.getElementById("resulttext").innerHTML = Result
    console.log(Result)
}
