# GitHub Actions ile Bulutta APK Derleme - Adım Adım

Bu yöntemde bilgisayarına Android Studio KURMUYORSUN. Sadece bir tarayıcı ve
(varsa) Git yeterli.

## 1) GitHub hesabı aç
github.com adresine git, ücretsiz hesap oluştur (yoksa).

## 2) Yeni bir repo (depo) oluştur
- Sağ üstteki (+) işaretine bas > "New repository"
- Repository name: `hesap-yoneticisi`
- **Private** seç (istersen Public da olur, ikisi de bu iş için ücretsiz çalışır;
  Private seçersen sadece sen görürsün)
- "Add a README file" kutucuğunu İŞARETLEME (biz zaten dosyaları yükleyeceğiz)
- "Create repository"

## 3) Proje dosyalarını yükle
Bu zip'in İÇİNDEKİ TÜM DOSYA VE KLASÖRLERİ (zip'in kendisini değil, açtıktan
sonraki içeriğini) yükleyeceksin. İki yöntem var:

### Yöntem A - Tarayıcıdan sürükle-bırak (Git bilmiyorsan bunu kullan)
1. Bu zip dosyasını bilgisayarında bir klasöre çıkar (sağ tık > "Ayıkla" / "Extract")
2. GitHub'da az önce oluşturduğun repo sayfasında "uploading an existing file"
   linkine tıkla (ya da "Add file" > "Upload files")
3. Çıkardığın klasörün İÇİNDEKİ her şeyi (settings.gradle.kts, build.gradle.kts,
   app klasörü, .github klasörü, .gitignore — hepsini birden) sürükleyip
   GitHub'ın yükleme alanına bırak
   - ÖNEMLİ: `.github` klasörü gizli göründüğü için bazı dosya yöneticilerinde
     görünmeyebilir. Görünmüyorsa dosya yöneticinde "gizli dosyaları göster"
     seçeneğini aç (Windows'ta: Görünüm > Gizli öğeler).
4. Altta "Commit changes" > "Commit changes" ile onayla

### Yöntem B - Git ile (komut satırına aşinaysan daha hızlı)
```
git clone <senin-repo-linkin>
cd hesap-yoneticisi
# zip içeriğini bu klasöre kopyala
git add .
git commit -m "ilk surum"
git push
```

## 4) Derlemenin başlamasını izle
- Repo sayfasında üstteki **"Actions"** sekmesine tıkla
- "APK Derle" adında bir çalışma göreceksin, üzerine tıkla, birkaç dakika sürer
  (turuncu = çalışıyor, yeşil tik = başarılı, kırmızı X = hata var)
- Otomatik başlamadıysa: Actions sekmesinde soldan "APK Derle" seç, sağda
  "Run workflow" butonuna bas

## 5) APK'yı indir
- Yeşil tik olan çalışmanın üzerine tıkla
- Sayfanın altında **"Artifacts"** bölümünde `hesap-yoneticisi-debug-apk`
  yazan bir zip göreceksin, ona tıklayıp indir
- İndirdiğin zip'i aç, içinde `app-debug.apk` var

## 6) Telefona kur
- `app-debug.apk` dosyasını telefonuna aktar (USB, WhatsApp'a kendine
  gönder, Google Drive, hangisi kolaysa)
- Telefonda dosyaya dokun, "Bilinmeyen kaynaklardan yüklemeye izin ver"
  çıkarsa onayla, kur

## Hata alırsan
Actions sekmesinde kırmızı X olan çalışmaya tıkla, üzerinde hangi adımda
("step") kırmızı olduğunu gör, o adımın loglarını genişlet ve hata metnini
bana yapıştır — birlikte çözeriz. En sık çıkabilecek hatalar genelde bir
dosyanın yanlış klasöre yüklenmesinden kaynaklanır (klasör yapısı bozulursa).
