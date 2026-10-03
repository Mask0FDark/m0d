const copy={
ru:{
navFeatures:"Возможности",navCalls:"Звонки",navDownload:"Приложения",openWeb:"Открыть веб-версию",download:"Скачать",
eyebrow:"M0D · активная разработка",heroTitle:"Мессенджер, который остаётся простым.",
heroText:"Личные чаты, группы, каналы, файлы и звонки. Один интерфейс на компьютере, телефоне и в браузере.",
downloadApp:"Скачать M0D",featuresKicker:"Что внутри",featuresTitle:"Не только переписка",
featuresText:"M0D закрывает обычные сценарии мессенджера без десятка отдельных приложений.",
f1Title:"Чаты и каналы",f1Text:"Личные диалоги, группы, каналы, комментарии, ответы, реакции, закрепы и медиа.",
f2Title:"Звонки без привязки к одному устройству",f2Text:"Голос, видео, демонстрация экрана на ПК и выбор микрофона, наушников или колонок прямо во время звонка.",
f3Title:"Продолжай на другом устройстве",f3Text:"Windows, Android, Web и временный PWA-вариант для iPhone работают с одним аккаунтом.",
downloadKicker:"M0D на твоих устройствах",downloadTitle:"Выбери платформу",
downloadText:"Windows и Android устанавливаются как приложения. На iPhone пока используй веб-версию как PWA — она добавляется на главный экран через Safari.",
recommended:"Рекомендуется",windowsText:"Отдельное приложение для Windows 10/11. Звонки, демонстрация экрана, выбор аудиоустройств и автообновление внутри M0D.",
downloadWindows:"Скачать для Windows",androidText:"M0D для Android с уведомлениями, звонками и мобильным интерфейсом.",
downloadAndroid:"Скачать APK",pwaBadge:"Пока PWA",iosText:"Отдельной сборки App Store пока нет. M0D можно установить на главный экран как веб-приложение.",
ios1:"Открой M0D в Safari.",ios2:"Нажми «Поделиться».",ios3:"Выбери «На экран Домой».",openIphone:"Открыть M0D на iPhone",
webTitle:"Ничего не хочешь устанавливать?",webText:"Открой M0D прямо в браузере. Это тот же аккаунт и те же чаты."
},
en:{
navFeatures:"Features",navCalls:"Calls",navDownload:"Apps",openWeb:"Open web app",download:"Download",
eyebrow:"M0D · active development",heroTitle:"A messenger that stays simple.",
heroText:"Private chats, groups, channels, files and calls. One interface on desktop, phone and the web.",
downloadApp:"Download M0D",featuresKicker:"What's inside",featuresTitle:"More than messaging",
featuresText:"M0D covers everyday messenger workflows without forcing you into a stack of separate apps.",
f1Title:"Chats and channels",f1Text:"Direct chats, groups, channels, comments, replies, reactions, pins and media.",
f2Title:"Calls without being tied to one device",f2Text:"Voice, video, desktop screen sharing and live microphone or speaker selection during a call.",
f3Title:"Continue on another device",f3Text:"Windows, Android, Web and the temporary iPhone PWA option use the same account.",
downloadKicker:"M0D on your devices",downloadTitle:"Choose your platform",
downloadText:"Windows and Android install as apps. On iPhone, use the web version as a PWA for now and add it to your Home Screen from Safari.",
recommended:"Recommended",windowsText:"A dedicated Windows 10/11 app with calls, screen sharing, audio device selection and in-app updates.",
downloadWindows:"Download for Windows",androidText:"M0D for Android with notifications, calls and a mobile interface.",
downloadAndroid:"Download APK",pwaBadge:"PWA for now",iosText:"There is no App Store build yet. You can install M0D to the Home Screen as a web app.",
ios1:"Open M0D in Safari.",ios2:"Tap Share.",ios3:"Choose “Add to Home Screen”.",openIphone:"Open M0D on iPhone",
webTitle:"Don't want to install anything?",webText:"Open M0D directly in your browser with the same account and chats."
}};
let lang=localStorage.getItem("m0d-site-lang")||(navigator.language?.toLowerCase().startsWith("ru")?"ru":"en");
function applyLanguage(){
 document.documentElement.lang=lang;
 document.querySelectorAll("[data-i18n]").forEach(el=>{const value=copy[lang]?.[el.dataset.i18n];if(value)el.textContent=value});
 document.getElementById("langSwitch").textContent=lang==="ru"?"EN":"RU";
}
document.getElementById("langSwitch").addEventListener("click",()=>{lang=lang==="ru"?"en":"ru";localStorage.setItem("m0d-site-lang",lang);applyLanguage();applyDeviceRecommendation()});


const downloadState={
 windows:"https://github.com/Mask0FDark/m0d/releases/download/v0.2.6/M0D-Setup-0.2.6-x64.exe",
 android:"https://github.com/Mask0FDark/m0d/releases/download/v0.2.6/M0D-0.2.6-android.apk"
};

function detectPlatform(){
 const ua=(navigator.userAgent||"").toLowerCase();
 const hint=String(navigator.userAgentData?.platform||navigator.platform||"").toLowerCase();
 if(ua.includes("android")) return "android";
 if(hint.includes("win")||ua.includes("windows nt")) return "windows";
 return "other";
}

const detectedPlatform=detectPlatform();

function applyDeviceRecommendation(){
 const box=document.getElementById("deviceRecommendation");
 const hero=document.getElementById("smartDownloadButton");
 const header=document.getElementById("smartHeaderDownload");
 document.querySelectorAll("[data-platform-card]").forEach(card=>{
   card.classList.toggle("device-match",card.dataset.platformCard===detectedPlatform);
 });
 if(!box||!hero||!header||detectedPlatform==="other"){
   box?.classList.add("hidden");
   return;
 }
 const isAndroid=detectedPlatform==="android";
 const href=downloadState[detectedPlatform];
 hero.href=href;
 header.href=href;
 box.classList.remove("hidden");
 if(lang==="ru"){
   hero.textContent=isAndroid?"Скачать APK для Android":"Скачать M0D для Windows";
   header.textContent=isAndroid?"Скачать APK":"Скачать EXE";
   box.innerHTML=isAndroid
     ? "<strong>У тебя Android.</strong> Лучше скачать приложение M0D — звонки и уведомления будут удобнее, чем в браузере."
     : "<strong>У тебя Windows.</strong> Лучше установить M0D для ПК — звонки, демонстрация экрана и уведомления работают удобнее, чем в браузере.";
 }else{
   hero.textContent=isAndroid?"Download Android APK":"Download M0D for Windows";
   header.textContent=isAndroid?"Download APK":"Download EXE";
   box.innerHTML=isAndroid
     ? "<strong>You're on Android.</strong> The M0D app is the better option for calls and notifications."
     : "<strong>You're on Windows.</strong> The desktop M0D app is better for calls, screen sharing and notifications.";
 }
}

async function refreshLatestDownloads(){
 try{
   const response=await fetch("https://api.github.com/repos/Mask0FDark/m0d/releases/latest",{headers:{"Accept":"application/vnd.github+json"}});
   if(!response.ok) throw new Error("release_fetch_failed");
   const release=await response.json();
   const assets=Array.isArray(release.assets)?release.assets:[];
   const windows=assets.find(asset=>/^M0D-Setup-.*-x64\.exe$/i.test(asset.name));
   const android=assets.find(asset=>/^M0D-.*-android\.apk$/i.test(asset.name));
   if(windows?.browser_download_url) downloadState.windows=windows.browser_download_url;
   if(android?.browser_download_url) downloadState.android=android.browser_download_url;
   document.querySelectorAll('[data-download-platform="windows"]').forEach(node=>node.href=downloadState.windows);
   document.querySelectorAll('[data-download-platform="android"]').forEach(node=>node.href=downloadState.android);
 }catch{}
 applyDeviceRecommendation();
}

applyLanguage();
refreshLatestDownloads();
