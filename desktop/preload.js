const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("M0DDesktop", {
  isDesktop: true,
  platform: process.platform,
  notify: payload => ipcRenderer.invoke("m0d:notify", payload),
  appInfo: () => ipcRenderer.invoke("m0d:app-info"),
  checkForUpdates: () => ipcRenderer.invoke("m0d:update-check"),
  installUpdate: () => ipcRenderer.invoke("m0d:update-install"),
  onUpdateStatus: callback => {
    if (typeof callback !== "function") return () => {};
    const handler = (_event, payload) => callback(payload);
    ipcRenderer.on("m0d:update-status", handler);
    return () => ipcRenderer.removeListener("m0d:update-status", handler);
  },
  onDeepLink: callback => {
    if (typeof callback !== "function") return () => {};
    const handler = (_event, url) => callback(url);
    ipcRenderer.on("m0d:deep-link", handler);
    return () => ipcRenderer.removeListener("m0d:deep-link", handler);
  }
});

window.addEventListener("DOMContentLoaded", () => {
  document.documentElement.classList.add("desktop-app");
});
