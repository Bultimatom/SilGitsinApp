# Kaydır Gitsin Proje Kontrolü

## Hocanin istedikleri

- Mobil uygulama ozgun: Tinder benzeri kaydirma mantigi galeri temizleme problemine uyarlandi.
- Icerik konuya gore ozellestirilmis: Galeri izni, foto kartlari, saga sakla, sola sil, geri al, ilerleme ve temizlik ozeti var.
- En az 4 activity: `HomeActivity`, `MainActivity`, `GuideActivity`, `StatsActivity`.
- Activity gecisleri: Ana menuden temizleme, rehber ve ozet ekranlarina gecis var; temizleme ekranindan rehber ve ozete de gidiliyor.
- En az 7 component: `TextView`, `ImageView`, `MaterialButton`, `ProgressBar`, `LinearLayout`, `ConstraintLayout`, `FrameLayout`, `ScrollView`, `GridLayout`, `Space` kullaniliyor.
- Arayuz: Koyu tema, ayrik butonlar, bos durum ekranlari, istatistik kartlari ve rehber ekrani ile akisi daha anlasilir hale getirildi.

## Eklenen davranislar

- Incelenen fotograflar `SharedPreferences` ile kaydediliyor ve sonraki yuklemelerde tekrar gosterilmiyor.
- Toplam silinen, saklanan ve incelenen sayilari ozet ekraninda tutuluyor.
- Fotograflar sola kaydirilinca once silme listesine aliniyor; tur bitene kadar geri alinabiliyor ve en sonda toplu siliniyor.
