# M0D Mobile

Android-клиент M0D построен на Capacitor и использует тот же интерфейс и E2EE-логику, что и web-клиент.

## Debug

По умолчанию приложение подключается к:
`https://m0d-dev.mask-0f-darkness.ru`.

Адрес можно заменить во время sync/build через переменную `M0D_APP_URL`.

```powershell
npm ci
$env:M0D_APP_URL="https://m0d-dev.mask-0f-darkness.ru"
npx cap sync android
cd android
.\gradlew.bat assembleDebug
```

## Deep links

- `m0d://user/<username>`
- `m0d://chat/<numeric-id>`
- HTTPS-ссылки dev-домена также объявлены в Android manifest.

Package ID: `site.m0d.messenger`.
