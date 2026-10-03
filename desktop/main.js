const { app, BrowserWindow, desktopCapturer, dialog, ipcMain, Notification, session, shell } = require("electron");
const { autoUpdater } = require("electron-updater");
const path = require("path");

const APP_URL = process.env.M0D_DESKTOP_URL || "https://m0d-dev.mask-0f-darkness.ru";
const APP_ORIGIN = new URL(APP_URL).origin;
let mainWindow = null;

function isAllowed(url) {
  try {
    const parsed = new URL(url);
    return parsed.origin === APP_ORIGIN || parsed.protocol === "m0d:";
  } catch {
    return false;
  }
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 840,
    minWidth: 960,
    minHeight: 640,
    backgroundColor: "#090b0f",
    show: false,
    autoHideMenuBar: true,
    icon: path.join(__dirname, "build", "icon.svg"),
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
      spellcheck: true
    }
  });

  win.once("ready-to-show", () => win.show());

  win.webContents.setWindowOpenHandler(({ url }) => {
    if (isAllowed(url)) {
      win.loadURL(url);
      return { action: "deny" };
    }
    shell.openExternal(url).catch(() => {});
    return { action: "deny" };
  });

  win.webContents.on("will-navigate", (event, url) => {
    if (!isAllowed(url)) {
      event.preventDefault();
      shell.openExternal(url).catch(() => {});
    }
  });

  win.webContents.on("render-process-gone", () => {
    if (!win.isDestroyed()) win.reload();
  });

  win.loadURL(APP_URL);
  return win;
}

function configurePermissions() {
  const ses = session.defaultSession;

  ses.setPermissionCheckHandler((_webContents, permission, requestingOrigin) => {
    if (requestingOrigin !== APP_ORIGIN) return false;
    return ["media", "display-capture", "notifications", "fullscreen", "clipboard-sanitized-write"].includes(permission);
  });

  ses.setPermissionRequestHandler((webContents, permission, callback) => {
    let origin = "";
    try { origin = new URL(webContents.getURL()).origin; } catch {}
    if (origin !== APP_ORIGIN) return callback(false);
    callback(["media", "display-capture", "notifications", "fullscreen", "clipboard-sanitized-write"].includes(permission));
  });

  ses.setDisplayMediaRequestHandler(async (_request, callback) => {
    try {
      const sources = await desktopCapturer.getSources({
        types: ["screen", "window"],
        thumbnailSize: { width: 320, height: 180 },
        fetchWindowIcons: true
      });
      const primaryScreen = sources.find(source => source.id.startsWith("screen:")) || sources[0];
      if (!primaryScreen) return callback({});
      callback({ video: primaryScreen });
    } catch {
      callback({});
    }
  }, { useSystemPicker: true });
}


function sendUpdateStatus(payload) {
  if (!mainWindow || mainWindow.isDestroyed()) return;
  mainWindow.webContents.send("m0d:update-status", payload);
}

function configureAutoUpdates() {
  if (!app.isPackaged) return;

  autoUpdater.autoDownload = true;
  autoUpdater.autoInstallOnAppQuit = true;

  autoUpdater.on("checking-for-update", () => sendUpdateStatus({ state: "checking" }));
  autoUpdater.on("update-available", info => {
    sendUpdateStatus({ state: "available", version: info.version });
  });
  autoUpdater.on("update-not-available", info => {
    sendUpdateStatus({ state: "current", version: info?.version || app.getVersion() });
  });
  autoUpdater.on("download-progress", progress => {
    sendUpdateStatus({
      state: "downloading",
      percent: Math.round(progress.percent || 0),
      version: progress.version || null
    });
  });
  autoUpdater.on("error", error => {
    sendUpdateStatus({ state: "error", message: String(error?.message || error || "update_failed") });
  });
  autoUpdater.on("update-downloaded", async info => {
    sendUpdateStatus({ state: "ready", version: info.version });
    const answer = await dialog.showMessageBox(mainWindow, {
      type: "info",
      title: "Обновление M0D готово",
      message: `M0D ${info.version} уже загружен.`,
      detail: "Перезапустить приложение и установить обновление сейчас?",
      buttons: ["Перезапустить и обновить", "Позже"],
      defaultId: 0,
      cancelId: 1,
      noLink: true
    });
    if (answer.response === 0) {
      setImmediate(() => autoUpdater.quitAndInstall(false, true));
    }
  });

  const check = () => autoUpdater.checkForUpdates().catch(() => {});
  setTimeout(check, 5000);
  setInterval(check, 4 * 60 * 60 * 1000).unref();
}

function restoreWindow() {
  if (!mainWindow || mainWindow.isDestroyed()) {
    mainWindow = createWindow();
    return;
  }
  if (mainWindow.isMinimized()) mainWindow.restore();
  mainWindow.show();
  mainWindow.focus();
}

app.setAppUserModelId("site.m0d.messenger.desktop");

const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
} else {
  app.on("second-instance", (_event, argv) => {
    restoreWindow();
    const deepLink = argv.find(value => value.startsWith("m0d://"));
    if (deepLink) mainWindow?.webContents.send("m0d:deep-link", deepLink);
  });

  app.whenReady().then(() => {
    configurePermissions();
    if (process.defaultApp && process.argv[1]) {
      app.setAsDefaultProtocolClient("m0d", process.execPath, [path.resolve(process.argv[1])]);
    } else {
      app.setAsDefaultProtocolClient("m0d");
    }
    mainWindow = createWindow();
    configureAutoUpdates();
  });
}

app.on("open-url", (event, url) => {
  event.preventDefault();
  restoreWindow();
  mainWindow?.webContents.send("m0d:deep-link", url);
});

app.on("activate", restoreWindow);

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});

ipcMain.handle("m0d:notify", (_event, payload = {}) => {
  const title = String(payload.title || "M0D").slice(0, 120);
  const body = String(payload.body || "").slice(0, 500);
  if (!Notification.isSupported()) return false;
  new Notification({
    title,
    body,
    icon: path.join(__dirname, "build", "icon.svg"),
    silent: Boolean(payload.silent)
  }).show();
  return true;
});

ipcMain.handle("m0d:app-info", () => ({
  version: app.getVersion(),
  platform: process.platform,
  appUrl: APP_URL
}));

ipcMain.handle("m0d:update-check", async () => {
  if (!app.isPackaged) return { ok: false, reason: "development" };
  try {
    const result = await autoUpdater.checkForUpdates();
    return { ok: true, version: result?.updateInfo?.version || null };
  } catch (error) {
    return { ok: false, reason: String(error?.message || error || "update_failed") };
  }
});

ipcMain.handle("m0d:update-install", () => {
  if (!app.isPackaged) return false;
  setImmediate(() => autoUpdater.quitAndInstall(false, true));
  return true;
});
