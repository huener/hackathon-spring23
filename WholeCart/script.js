document.getElementById("resulttext").innerHTML = ""
fetch("https://huener.github.io/hackathon-spring23/WholeCart/")
    .then(response => response.text())
    .then(data => myFunc(data));

function myFunc(Result) {
    document.getElementById("resulttext").innerHTML = Result
    console.log(Result)
}
