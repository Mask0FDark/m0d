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
downloadWindows:"Скачать для Windows",androidText:"M0D для Android с уведомлениями, звонками и встроенной проверкой обновлений.",
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
downloadWindows:"Download for Windows",androidText:"M0D for Android with notifications, calls and built-in update checks.",
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
document.getElementById("langSwitch").addEventListener("click",()=>{lang=lang==="ru"?"en":"ru";localStorage.setItem("m0d-site-lang",lang);applyLanguage()});
applyLanguage();
