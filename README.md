# Sil Gitsin

<p align="center">
  <img src="assets/omer-uzunsoy-btv-logo.jpg" alt="Ömer Uzunsoy BTV" width="160" />
</p>

<p align="center">
  <strong>Galerini kart kart gez, kararını kaydırarak ver, gereksiz medyaları Android onayıyla güvenle temizle.</strong>
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Language" src="https://img.shields.io/badge/language-Java-f89820?style=for-the-badge" />
  <img alt="Build" src="https://img.shields.io/badge/build-Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue?style=for-the-badge" />
</p>

## Proje Özeti

Google Play Store adı: **Sil Gitsin:Galeri Temizleyici**

Sil Gitsin, telefon galerisini hızlı ve kontrollü temizlemek için geliştirilmiş native Android uygulamasıdır. Uygulama, medya dosyalarını Tinder tarzı kart akışıyla gösterir: sağa kaydırılanlar saklanır, sola kaydırılanlar silme listesine alınır. Gerçek silme işlemi hemen yapılmaz; kullanıcı seçilenleri gözden geçirdikten sonra Android'in resmi sistem onayıyla tamamlanır.

Amaç basit: karar yorgunluğunu azaltmak, galeri temizliğini eğlenceli hale getirmek ve kullanıcıya son ana kadar kontrol bırakmak.

## Öne Çıkan Özellikler

- Kart tabanlı fotoğraf ve video inceleme akışı
- Sağa kaydırarak saklama, sola kaydırarak silme listesine alma
- Silmeden önce seçilen medyaları tekrar gözden geçirme
- Android'in güvenli toplu silme onayıyla gerçek silme işlemi
- Son işlemi geri alma
- Favorilere ekleme ve favori koleksiyonu
- Fotoğraf/video boyutuna göre kazanılabilecek alan sayacı
- Daha önce incelenen medyaları tekrar göstermeme
- Kısa rehber ve onboarding ekranı
- Açık/koyu tema desteği
- Tamamen cihaz üzerinde çalışan medya akışı

## Güvenlik ve Gizlilik

Sil Gitsin, galeri temizliği için gereken medya erişimini cihaz üzerinde kullanır. Uygulamada hesap sistemi, bulut senkronizasyonu veya uzaktan medya işleme mekanizması yoktur. Fotoğraf ve videolar kullanıcının cihazında kalır.

Silme işlemi iki aşamalıdır:

1. Kullanıcı sola kaydırarak medyayı silme listesine ekler.
2. Kullanıcı listeyi kontrol eder ve Android'in resmi silme onayından sonra işlem tamamlanır.

Bu yaklaşım, yanlışlıkla silme riskini azaltır ve hassas medya işlemlerinde sistem güvenlik akışını korur.

## Kullanılan Teknolojiler

| Alan | Teknoloji |
| --- | --- |
| Dil | Java |
| Platform | Native Android |
| Arayüz | Android Views, XML Layout |
| UI Bileşenleri | Material Components, AppCompat, ConstraintLayout |
| Medya Erişimi | Android MediaStore |
| Yerel Veri | SharedPreferences |
| Build Sistemi | Gradle Kotlin DSL |
| Minimum SDK | 24 |
| Target SDK | 34 |
| Compile SDK | 36 |

## Proje Yapısı

```text
SilGitsinApp/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/AndroidManifest.xml
│   ├── src/main/java/com/example/myapplication/
│   │   ├── MainActivity.java
│   │   ├── SwipeCardView.java
│   │   ├── MediaRepository.java
│   │   ├── MediaDeleteHelper.java
│   │   ├── ReviewStore.java
│   │   ├── FavoritesActivity.java
│   │   ├── GuideActivity.java
│   │   └── StatsActivity.java
│   └── src/main/res/
├── assets/
├── LICENSE
└── README.md
```

## Kurulum

Projeyi klonla:

```bash
git clone https://github.com/Bultimatom/SilGitsinApp.git
cd SilGitsinApp/app
```

Android Studio ile `app` klasörünü açabilir veya terminalden Gradle komutlarını çalıştırabilirsin.

Windows:

```powershell
.\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

Debug APK çıktısı:

```text
app/build/outputs/apk/debug/
```

## Kontrol Komutları

Debug build ve unit test hedeflerini çalıştırmak için:

```powershell
.\gradlew.bat test assembleDebug
```

Lint kontrolü için:

```powershell
.\gradlew.bat lintDebug
```

## İzinler

Uygulama Android sürümüne göre galeri erişimi ister:

- Android 13 ve üzeri: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`
- Android 12 ve altı: `READ_EXTERNAL_STORAGE`
- Eski Android sürümleri için sınırlı yazma/silme desteği: `WRITE_EXTERNAL_STORAGE`

Android 11 ve üzeri cihazlarda silme işlemleri `MediaStore.createDeleteRequest` ile sistem onayına taşınır.

## Durum

Proje geliştirme sürümündedir ve GitHub üzerinde açık kaynak olarak paylaşılmaya hazır hale getirilmektedir. Katkı, öneri ve hata bildirimleri memnuniyetle karşılanır.

## Lisans

Bu proje MIT lisansı ile yayınlanmıştır. Ayrıntılar için [LICENSE](LICENSE) dosyasını inceleyebilirsin.

## Geliştirici

Ömer Uzunsoy BTV

