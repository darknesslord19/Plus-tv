# Suka Film

Based on SukaFilm by **OliveraStudio** (Celine Olivia Clarissa).
Source listing: https://web.sketchub.in/p/33826 - the listing asks for credit if you customize the code.
The source file headers prohibit sale/commercial redistribution; get the author's written permission before any such use.

Bu proje Sketchware yedeğinden Gradle projesine dönüştürüldü.

## APK derleme (GitHub Actions)
1. Bu klasörün **içindekileri** deponun köküne yükleyin (`.github` klasörü dahil).
2. **Actions > Build APK > Run workflow** (her push'ta da çalışır).
3. İşlem bitince **Artifacts > SukaFilm-apk** dosyasını indirin.

## Firebase
**Settings > Secrets and variables > Actions** altına ekleyin:
`FIREBASE_APP_ID`, `FIREBASE_API_KEY`, `FIREBASE_DATABASE_URL`, `FIREBASE_STORAGE_BUCKET`

Android uygulaması paket adı: `com.oliverastudio.sukafilm`.
Örnek veri: `docs/firebase_database_structure.json`. Veritabanı kurallarında herkese yazma izni vermeyin.
Bu değerler APK içine gömülür; güvenlik, veritabanı kurallarıyla sağlanmalıdır.
Secrets eklenmezse uygulama film verisi yüklemez.

APK, `signing/` altındaki herkese açık AOSP test anahtarıyla imzalanır. Yalnızca test içindir: bu anahtarla herkes imzalayabilir. Yayın için kendi anahtarınızı kullanın ve depoya yüklemeyin.
