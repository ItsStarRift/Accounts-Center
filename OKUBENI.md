# Hesap Yöneticisi - Aşama 1

Artık GitHub Actions ile bulutta derleme yöntemini kullanıyoruz.
Kurulum için **GITHUB_KURULUM.md** dosyasını aç ve sırasıyla uygula.
Android Studio kurmana GEREK YOK.

## Bu aşamada neler var
- Şifreli Room veritabanı (SQLCipher + Android Keystore ile korunan rastgele anahtar).
  Kaynak kodu herkese açık olsa bile, veritabanı dosyası bu anahtar olmadan okunamaz.
- Ana ekran: arama çubuğu (dokununca tam ekran arama moduna geçer, eşleşen harf
  turuncu renkte vurgulanır), A-Z gruplu uygulama/oyun listesi, sağ altta (+) ile
  ekleme, alt kısımda oval/yüzen navigasyon çubuğu (Uygulamalar / Ayarlar).
- Uygulama/oyun ekleme: isim + zorunlu kategori seçimi (Uygulama/Oyun) + opsiyonel
  ikon (internet varsa Clearbit'ten otomatik arama + onay penceresi, yoksa/reddedilirse
  galeri veya kameradan manuel seçim).

## Sırada ne var (2. ve 3. Aşama)
- Bir uygulamaya tıklanınca açılan hesap listesi ekranı
- Hesap ekleme (isim + ikon, kategori yok)
- Hesap içindeki "terim" (alan) ekleme sistemi (hazır terimler + özel terim akışı)
- Ayarlar ekranı (dil, dışa/içe aktar, geri bildirim, github, hakkında)
- Uygulama kilidi (desen/pin/şifre, ilk açılış sorgusu)

APK'yı kurup çalıştırdıktan sonra bana haber ver, devam edelim.
