# Kaydır Gitsin

Kaydır Gitsin, galerini Tinder tarzı kartlarla hızlıca temizlemek için geliştirilmiş açık kaynak Android uygulamasıdır. Fotoğrafları tek tek karar yorgunluğuna sokmadan ayırırsın: sağa kaydır tut, sola kaydır silme listesine gönder, en sonda Android'in resmi onayıyla temizle.

## Neden Güvenilir?

- Fotoğraflar cihazında kalır; uygulama medya dosyalarını bir sunucuya göndermez.
- Silme işlemi anında yapılmaz. Sola kaydırılan fotoğraflar önce seçilenler listesine alınır.
- Gerçek silme, yalnızca seçilenleri gördükten sonra Android'in sistem onayıyla gerçekleşir.
- Proje açık kaynaklıdır; kodu herkes inceleyebilir.

Açık kaynak deposu: [Bultimatom/KaydirGitsinApp](https://github.com/Bultimatom/KaydirGitsinApp)

## Özellikler

- Sağa kaydırarak fotoğrafı tutma
- Sola kaydırarak silme listesine ekleme
- Seçilenleri yarıda inceleme ve toplu silme
- Son işlemi geri alma
- Favorilere kalp butonu veya çift dokunma ile ekleme
- Favoriler koleksiyonu
- Yinelenen medya akışı
- Hafıza kazanımı sayacı
- Modern onboarding tutorial overlay
- Gece modu

## Teknoloji

- Dil: Java
- Platform: Native Android
- UI: Android Views, XML layout, Material Components
- Depolama: SharedPreferences
- Medya: Android MediaStore
- Build: Gradle Kotlin DSL
- Minimum SDK: 24
- Target SDK: 34
- Compile SDK: 36

## Kurulum

Projeyi Android Studio ile aç:

```bash
git clone https://github.com/Bultimatom/KaydirGitsinApp.git
cd KaydirGitsinApp/app
```

Debug APK oluştur:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

APK çıktısı:

```text
app/build/outputs/apk/debug/
```

## Kullanım Akışı

1. Uygulamayı aç ve galeri iznini ver.
2. Fotoğrafı tutmak için sağa kaydır.
3. Silme listesine almak için sola kaydır.
4. Yanlış karar verirsen geri al.
5. İstersen tur bitmeden "Seçilenleri İncele" ile listeye gir.
6. Son ekranda kontrol et ve Android onayıyla sil.

## Gizlilik Yaklaşımı

Kaydır Gitsin, galeri temizliği için gereken medya erişimini cihaz üzerinde kullanır. Uygulama bir bulut senkronizasyonu, hesap sistemi veya uzaktan veri işleme mekanizması üzerine kurulmamıştır. Silme gibi hassas işlemler Android'in kendi güvenlik/onay akışıyla yapılır.

## Lisans

Bu proje açık kaynak olarak geliştirilmektedir. Lisans bilgisi için depodaki lisans dosyasını kontrol edebilirsin.
