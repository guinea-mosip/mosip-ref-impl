$buoop = {
    required:{e:-4,f:-1,o:-3,s:-1,c:-3},insecure:true,unsupported:true,style:"corner",api:2020.12,
    reminder: 24,
    reminderClosed: 150,
    onshow: function(infos){},
    onclick: function(infos){},
    onclose: function(infos){},
    l: false,
    text: "Vous utilisez une version de navigateur ({brow_name}) non supportée. <div class='button-center'> <a{up_but}> Mise à jour </a> <a{ignore_but}>Annuler</a> </div>",
    newwindow: true,
    url: null,
    noclose:false,
    nomessage: false,
    jsshowurl: "https://guinea-sandbox.mosip.net/pre-registration-ui/assets/js/browser-outdate/update.show.min.js",
    //jsshowurl: "http://localhost:4200/assets/js/browser-outdate/update.show.min.js",
    container: document.body,
    no_permanent_hide: false
};

function $buo_f(){
    var e = document.createElement("script");
    e.src = "//browser-update.org/update.min.js";
    document.body.appendChild(e);
};
try {document.addEventListener("DOMContentLoaded", $buo_f,false)}
catch(e){window.attachEvent("onload", $buo_f)}
