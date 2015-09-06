function init() {
    document.getElementById("github").onclick = function () {
        location.href = "https://www.github.com/DenizUgur/ninegagsaver";
    };
    document.getElementById("playstore").onclick = function () {
        location.href = "https://play.google.com/store/apps/details?id=com.denizugur.ninegagsaver";
    };

    $('#device-screenshots').cycle({
        fx: 'fade',
        speed: 1500,
        timeout: 5000
    });
}