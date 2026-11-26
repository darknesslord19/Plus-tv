# Suka Film

Based on SukaFilm by **OliveraStudio** (Celine Olivia Clarissa).
Source listing: https://web.sketchub.in/p/33826 - the listing asks for credit if you customize the code.
The source file headers prohibit sale/commercial redistribution; get the author's written permission before any such use.

Bu proje Sketchware yedeğinden Gradle projesine dönüştürüldü.

## APK derleme (GitHub Actions)
1. Bu klasörün **içindekileri** deponun köküne yükleyin (`.github` klasörü dahil).
2. **Actions > Build APK > Run workflow** (her push'ta da çalışır).
3. İşlem bitince **Artifacts > SukaFilm-apk** dosyasını indirin.

## M3U playlist
Uygulamada üstteki **+ M3U** düğmesiyle kendi liste linkinizi ekleyebilirsiniz (**Varsayılan** ile geri dönülür). Oynatıcıda **⛶** tam ekran açar.

Uygulama listeyi `playlist.m3u` dosyasından (GitHub raw bağlantısı) okur. Dosyayı düzenlemek yeterli; yeni APK gerekmez. Uygulamayı kapatıp açın. GitHub önbelleği yüzünden değişiklik birkaç dakika gecikebilir.

```
#EXTM3U
#EXTINF:-1 group-title="Kategori" tvg-logo="https://.../poster.jpg" year="2024" rating="7.5",Film Adı
https://.../video.mp4
```
- `group-title` kategori olur. İsteğe bağlı: `tvg-logo`, `year`, `rating`, `tvg-country`, `quality`, `duration`, `description`.
- Her satır ayrı bir videodur (dizi/sezon gruplaması yok). `#EXTVLCOPT` gibi özel HTTP başlıkları desteklenmez.
- `.m3u8` yayınları indirilemez. Üç kalite seçeneği aynı bağlantıyı kullanır.
- Başka liste kullanmak için: Settings > Secrets and variables > Actions > Variables > `PLAYLIST_URL`, sonra yeniden derleyin.
- Yalnızca kullanım hakkınız olan içerikleri ekleyin.

APK, `signing/` altındaki herkese açık AOSP test anahtarıyla imzalanır. Yalnızca test içindir: bu anahtarla herkes imzalayabilir. Yayın için kendi anahtarınızı kullanın ve depoya yüklemeyin.
