# YTSubtitleSpeaker

Kotlin + Jetpack Compose ile geliştirilen Android uygulamasıdır.

## Özellikler
- Google Sign-In (OAuth) ile giriş
- YouTube linkinden `videoId` çıkarma ve doğrulama
- YouTube Data API v3 ile caption track listeleme
- Track seçimi ve indirme denemesi
- İndirme başarısız olursa kullanıcıya açık hata mesajı gösterimi
- Cloud Translation API ile chunk bazlı çeviri
- Android TextToSpeech ile çeviri metnini seslendirme (Play / Pause / Stop / Resume)
- MVVM + ViewModel + StateFlow mimarisi

## Proje Yapısı
- `app/src/main/java/.../ui`: Compose ekranları + ViewModel
- `app/src/main/java/.../data/network`: Retrofit servisleri
- `app/src/main/java/.../data/repository`: İş akışı (caption + translation)
- `app/src/main/java/.../tts`: TextToSpeech kontrol katmanı

## Kurulum
1. Android Studio (Hedgehog+), JDK 17 ve Android SDK kurulu olmalı.
2. Google Cloud Console'da proje açın.
3. **APIs & Services > Library** bölümünden şunları enable edin:
   - YouTube Data API v3
   - Cloud Translation API
4. **OAuth consent screen** yapılandırın.
5. **Credentials > Create Credentials > OAuth client ID** oluşturun (Android).
6. Uygulamanın package adı: `com.example.ytsubtitlespeaker`.
7. SHA-1 almak için:
   ```bash
   ./gradlew signingReport
   ```
   veya debug keystore ile:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
8. API Key üretin ve gerekiyorsa app restriction / API restriction ekleyin.
9. `app/build.gradle.kts` içindeki aşağıdaki alanları güncelleyin:
   - `YOUTUBE_API_KEY`
   - `TRANSLATE_API_KEY`
   - `GOOGLE_WEB_CLIENT_ID`

## Güvenlik Notu
Bu MVP örneğinde API key değerleri local BuildConfig içinde tutulur. **Production** için API key'leri doğrudan mobil uygulamada saklamak yerine backend proxy/token exchange yaklaşımı önerilir.

## Çalıştırma
```bash
./gradlew assembleDebug
```

## Akış
1. Login ekranında Google ile giriş.
2. YouTube linki girilir ve videoId doğrulanır.
3. Caption track listesi gelir, track + hedef dil seçilir.
4. Caption indirilir, chunk chunk çevrilir.
5. Sonuç ekranında orijinal/çeviri metin ve TTS kontrolleri kullanılır.
