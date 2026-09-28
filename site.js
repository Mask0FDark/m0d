const copy = {
  ru: {
    navFeatures:"Возможности",navCalls:"Звонки",navDownload:"Скачать",download:"Скачать",
    eyebrow:"M0D · Message Over Distance",heroTitle:"Общение без лишнего шума",
    heroText:"Личные чаты, группы, каналы и звонки в одном лёгком мессенджере.",
    downloadApp:"Скачать приложение",learnMore:"Узнать больше",heroNote:"Сейчас M0D находится в активной разработке.",
    mockName:"M0D",mockOnline:"в сети",mockToday:"Сегодня",mockMsg1:"Созвонимся вечером?",
    mockMsg2:"Да, напиши когда будешь свободна",mockVideo:"Видео · 0:18",mockMsg3:"Уже здесь 👋",mockPlaceholder:"Сообщение",
    featuresKicker:"Возможности",featuresTitle:"Всё нужное для обычного общения",
    f1Title:"Чаты без перегруза",f1Text:"Личные диалоги, группы, каналы, ответы, реакции, закрепы и медиа.",
    f2Title:"Голос и видео",f2Text:"WebRTC-звонки с P2P-соединением и резервным TURN-маршрутом.",
    f3Title:"Между устройствами",f3Text:"Web/PWA и Android с единым интерфейсом и историей общения.",
    f4Title:"Приватность",f4Text:"Email не используется как публичный идентификатор для поиска людей.",
    designKicker:"Интерфейс",designTitle:"Привычно с первого экрана",
    designText:"M0D делает ставку на плотный, спокойный интерфейс: чаты всегда на виду, сообщения читаются легко, а звонки не мешают переписке.",
    point1:"Быстрый поиск",point2:"Компактные диалоги",point3:"Тёмная тема",
    downloadKicker:"M0D на устройствах",downloadTitle:"Скачай и оставайся на связи",
    downloadText:"Публичные сборки будут появляться в GitHub Releases по мере готовности.",
    androidSmall:"Для Android",releases:"GitHub Releases",desktopSmall:"Для компьютера",comingSoon:"Скоро"
  },
  en: {
    navFeatures:"Features",navCalls:"Calls",navDownload:"Download",download:"Download",
    eyebrow:"M0D · Message Over Distance",heroTitle:"Communication without the noise",
    heroText:"Private chats, groups, channels and calls in one lightweight messenger.",
    downloadApp:"Download app",learnMore:"Learn more",heroNote:"M0D is currently in active development.",
    mockName:"M0D",mockOnline:"online",mockToday:"Today",mockMsg1:"Call tonight?",
    mockMsg2:"Sure, text me when you're free",mockVideo:"Video · 0:18",mockMsg3:"I'm here 👋",mockPlaceholder:"Message",
    featuresKicker:"Features",featuresTitle:"Everything you need for everyday communication",
    f1Title:"Focused chats",f1Text:"Private chats, groups, channels, replies, reactions, pins and media.",
    f2Title:"Voice and video",f2Text:"WebRTC calls with direct P2P connections and TURN fallback.",
    f3Title:"Across devices",f3Text:"Web/PWA and Android with one familiar interface and shared history.",
    f4Title:"Privacy",f4Text:"Email is not used as a public identifier for finding people.",
    designKicker:"Interface",designTitle:"Familiar from the first screen",
    designText:"M0D focuses on a dense, calm interface: chats stay visible, messages are easy to read and calls don't get in the way.",
    point1:"Fast search",point2:"Compact conversations",point3:"Dark theme",
    downloadKicker:"M0D on your devices",downloadTitle:"Download and stay connected",
    downloadText:"Public builds will appear in GitHub Releases as they become ready.",
    androidSmall:"For Android",releases:"GitHub Releases",desktopSmall:"For desktop",comingSoon:"Coming soon"
  }
};
let lang = localStorage.getItem("m0d-site-lang") || (navigator.language?.toLowerCase().startsWith("ru") ? "ru" : "en");
function applyLanguage(){
  document.documentElement.lang = lang;
  document.querySelectorAll("[data-i18n]").forEach(el=>{ const value=copy[lang]?.[el.dataset.i18n]; if(value) el.textContent=value; });
  document.getElementById("langSwitch").textContent = lang === "ru" ? "EN" : "RU";
  document.title = lang === "ru" ? "M0D — Message Over Distance" : "M0D — Message Over Distance";
}
document.getElementById("langSwitch").addEventListener("click",()=>{lang=lang==="ru"?"en":"ru";localStorage.setItem("m0d-site-lang",lang);applyLanguage();});
applyLanguage();