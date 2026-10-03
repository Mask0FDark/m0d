import type { CapacitorConfig } from '@capacitor/cli';

const appUrl = process.env.M0D_APP_URL || 'https://m0d-dev.mask-0f-darkness.ru';
const host = new URL(appUrl).host;

const config: CapacitorConfig = {
  appId: 'site.m0d.messenger',
  appName: 'M0D',
  webDir: 'www',
  server: {
    url: appUrl,
    cleartext: false,
    allowNavigation: [host],
    androidScheme: 'https'
  },
  android: {
    backgroundColor: '#0e1621',
    allowMixedContent: false,
    webContentsDebuggingEnabled: process.env.M0D_DEBUG === '1'
  }
};

export default config;
